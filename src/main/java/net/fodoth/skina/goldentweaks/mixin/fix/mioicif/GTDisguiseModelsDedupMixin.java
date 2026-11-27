package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Shares one disguise wrapper between every block state drawn by the same model.
 *
 * <p>{@code DisguiseModels.modifyBakingResult} builds a {@code Wrapped} per state, but the wrapper
 * holds only the model it delegates to and a wire flag — nothing per-state. Foam and wire blocks
 * therefore allocate one wrapper per state variant, which came to 10754 objects here, most of them
 * duplicates of the same handful of models.</p>
 *
 * <p><b>Why the hook is on {@code Map.put} rather than on the constructor.</b> The wrapper type is
 * package-private in the other mod, so a handler that returns one has to name it — and a mixin class
 * that names it has to live in that mod's package. Hooking the constructor instead would need the
 * same name. The {@code put} that stores each wrapper takes and returns {@code Object}, so a handler
 * there is free to live in this mod's own package. The delegate and the flag are read off the wrapper
 * with reflection, which is enough to know whether an equal one already exists.</p>
 */
@Mixin(targets = "com.miophas.singularity_iteration.common.client.render.DisguiseModels", remap = false)
public abstract class GTDisguiseModelsDedupMixin {

    /** One wrapper per distinct (delegate, wire) pair, rebuilt with each baking pass. */
    @Unique
    private static final Map<Object, Map<Boolean, Object>> gt$wrappers = new IdentityHashMap<>();

    /** The wrapper's two fields, looked up once on first use. */
    @Unique
    private static Field gt$delegateField;

    @Unique
    private static Field gt$wireField;

    @Unique
    private static boolean gt$fieldsResolved;

    /**
     * Clears the cache at the start of a bake.
     *
     * <p>Baking hands out new model instances, so a cache kept across reloads would hold wrappers
     * around models that are no longer in use.</p>
     */
    @Inject(method = "modifyBakingResult", at = @At("HEAD"))
    private static void gt$resetWrappersForBake(ModelEvent.ModifyBakingResult event,
                                                CallbackInfo callback) {
        gt$wrappers.clear();
    }

    /**
     * Swaps a freshly built wrapper for an identical one already stored, when there is one.
     *
     * <p>The model map is keyed by block state, so every state still gets its own entry; only the
     * value behind it is shared. A state that shares a model keeps behaving exactly as before — it
     * simply points at the same immutable wrapper the other states do.</p>
     */
    @WrapOperation(method = "modifyBakingResult",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object gt$shareWrapper(Map<Object, Object> models, Object key, Object wrapper,
                                          Operation<Object> original) {
        Object[] parts = gt$parts(wrapper);
        if (parts == null) {
            // Not a disguise wrapper, or its shape changed. Store it as the mod intended.
            return original.call(models, key, wrapper);
        }
        Object delegate = parts[0];
        Boolean wire = (Boolean) parts[1];

        Map<Boolean, Object> byFlag =
                gt$wrappers.computeIfAbsent(delegate, ignored -> new HashMap<>(2));
        Object existing = byFlag.putIfAbsent(wire, wrapper);
        return original.call(models, key, existing == null ? wrapper : existing);
    }

    /**
     * Reads a wrapper's delegate and wire flag, or {@code null} when either cannot be found.
     *
     * <p>Fields are discovered by type rather than by name: the delegate lives on the mod's own class
     * or on NeoForge's wrapper base, and naming it would bind this fix to one of those. Walking the
     * hierarchy for "the first field that holds a {@code BakedModel}" and "the only boolean" finds
     * both without assuming which class declares them — and a shape change shows up as a
     * {@code null}, which makes the hook fall back to storing the wrapper untouched.</p>
     */
    @Unique
    private static Object[] gt$parts(Object wrapper) {
        try {
            if (!gt$fieldsResolved) {
                gt$fieldsResolved = true;
                BakedModel delegate = null;
                Boolean wire = null;
                for (Class<?> type = wrapper.getClass(); type != null; type = type.getSuperclass()) {
                    for (Field field : type.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (delegate == null && BakedModel.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            gt$delegateField = field;
                            delegate = (BakedModel) field.get(wrapper);
                        } else if (wire == null && field.getType() == boolean.class) {
                            field.setAccessible(true);
                            gt$wireField = field;
                            wire = (Boolean) field.get(wrapper);
                        }
                    }
                }
                return delegate == null || wire == null ? null : new Object[]{delegate, wire};
            }
            if (gt$delegateField == null || gt$wireField == null) {
                return null;
            }
            return new Object[]{gt$delegateField.get(wrapper), gt$wireField.getBoolean(wrapper)};
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return null;
        }
    }
}

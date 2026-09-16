package net.fodoth.skina.goldentweaks.mixin.feature.thaumichorizons;

import com.kentington.thaumichorizons.common.planar.VortexBlockEntity;
import net.fodoth.skina.goldentweaks.compat.thaumichorizons.GTRiftRecipe;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the JSON-driven rift crafting API ({@code goldentweaks:rift_crafting}) to Thaumic Horizons'
 * planar vortex.
 * <p>
 * Thaumic Horizons resolves its rift recipes with a hard-coded chain at the end of
 * {@code craft}; anything it does not know is simply ignored. This mixin runs the same crafting
 * pass once more at the normal exit of {@code craft} and feeds every leftover offering that
 * matches a registered recipe through the vanilla helpers, so the outputs land in the vortex's
 * list exactly like the built-in recipes do.
 */
@Mixin(value = VortexBlockEntity.class, remap = false)
public abstract class VortexBlockEntityMixin {

    /** Mirrors the output cap Thaumic Horizons applies to its own recipes. */
    @Unique
    private static final int gt$MAX_OUTPUTS = 128;

    /** Entities considered per pass, same as {@code craft}. */
    @Unique
    private static final int gt$MAX_ENTITIES = 16;

    @Accessor("outputs")
    protected abstract ListTag gt$outputs();

    @Invoker("inputValid")
    protected abstract boolean gt$inputValid(ServerLevel level, ItemEntity entity, ItemStack stack, ItemStack copy);

    @Invoker("consume")
    static void gt$consume(ItemEntity entity, ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "craft", at = @At("TAIL"))
    private void gt$craftJsonRecipes(ServerLevel level, CallbackInfo ci) {
        if (GTRiftRecipe.isEmpty()) {
            return;
        }

        VortexBlockEntity vortex = (VortexBlockEntity) (Object) this;
        ListTag outputs = gt$outputs();
        if (outputs.size() >= gt$MAX_OUTPUTS) {
            return;
        }

        List<ItemEntity> entities = new ArrayList<>();
        level.getEntities(
                EntityTypeTest.forClass(ItemEntity.class),
                new AABB(vortex.getBlockPos()).inflate(1.0D),
                entity -> !entity.getItem().isEmpty(),
                entities,
                gt$MAX_ENTITIES
        );

        for (ItemEntity entity : entities) {
            if (outputs.size() >= gt$MAX_OUTPUTS) {
                return;
            }

            ItemStack stack = entity.getItem();
            ItemStack result = GTRiftRecipe.find(stack);
            if (result.isEmpty()) {
                continue;
            }

            // The vanilla gate also re-checks that the rift is still valid and that the entity
            // really holds the stack we looked at.
            if (!gt$inputValid(level, entity, stack, stack.copy())) {
                continue;
            }

            outputs.add(result.save(level.registryAccess()));
            gt$consume(entity, stack);
            vortex.setChanged();
        }
    }
}

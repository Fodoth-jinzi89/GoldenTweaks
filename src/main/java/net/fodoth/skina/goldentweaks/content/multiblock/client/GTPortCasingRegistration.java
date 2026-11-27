package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.simibubi.create.foundation.block.connected.CTModel;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTCasingProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps the models of every block that draws a casing plate in Create's connected-texture model.
 *
 * <p>That model is the mechanism: at draw time it asks {@link GTPortCasingBehaviour} which sheet a face
 * belongs to and how the neighbouring cells compare, then points that face at the right tile. One
 * authored model serves every casing.</p>
 *
 * <p><b>Which models:</b> the ones belonging to a block that implements {@link GTCasingProvider}. The
 * set of block ids is read from the block registry, so a new machine or casing is wrapped by being
 * registered — its id never appears here, which is the point of the interface.</p>
 *
 * <p>A model key is a block id with its state appended ({@code goldentweaks:shell_pp#casing=0}), and
 * that block id is the whole test. The model's own name is not usable: the controller block is
 * {@code chemical_plant_controller} while its models are {@code controller_idle} and siblings, and a
 * port block {@code gt_port_item_input_lv} is drawn by {@code gt_port_item_input_t2}.</p>
 *
 * <p>The models are deliberately not identified by asking them for a sprite: the atlas is not built
 * yet at this point, and a wrapped model from another mod throws
 * {@code Tried to lookup sprite, but atlas is not initialized} when asked for its particle.</p>
 *
 * <p>Guarded on Create: this mod does not require it, and without it there is no CT model to wrap. The
 * plates then draw their authored sheet, which is the same fallback as before.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortCasingRegistration {

    private static final String CREATE = "create";

    private GTPortCasingRegistration() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        if (!ModList.get().isLoaded(CREATE)) {
            return;
        }

        Set<ResourceLocation> blocks = casingBlockIds();
        if (blocks.isEmpty()) {
            return;
        }

        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        GTPortCasingBehaviour behaviour = new GTPortCasingBehaviour();

        int wrapped = 0;
        int created = 0;
        // One wrapper per distinct model, not per entry. The map is keyed by block id with the state
        // appended, so one block arrives many times — a port has twelve states, all of them drawn by
        // the same model instance. A `CTModel` carries no per-state data, so a single wrapper can
        // serve every state; wrapping each entry separately allocated thousands of identical objects
        // for nothing.
        //
        // Identity, not equality: two separately-baked models that happen to compare equal still need
        // their own wrapper, because the underlying model instance is what gets delegated to.
        Map<BakedModel, CTModel> wrappers = new IdentityHashMap<>();
        // Collected first: the map must not be modified while it is being iterated.
        Map<ModelResourceLocation, BakedModel> replacements = new HashMap<>();
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : models.entrySet()) {
            if (!blocks.contains(entry.getKey().id())) {
                continue;
            }
            // A state variant key and the inventory key both arrive here, each holding the same model
            // instance until it is replaced; wrapping twice would stack two CT layers over one model.
            if (entry.getValue() instanceof CTModel) {
                continue;
            }
            CTModel wrapper = wrappers.get(entry.getValue());
            if (wrapper == null) {
                wrapper = new CTModel(entry.getValue(), behaviour);
                wrappers.put(entry.getValue(), wrapper);
                created++;
            }
            replacements.put(entry.getKey(), wrapper);
            wrapped++;
        }
        models.putAll(replacements);

        if (wrapped > 0) {
            GoldenTweaks.LOGGER.info("[GT] casing plates wrapped for connected textures: {} entries, {} wrappers",
                    wrapped, created);
        }
    }

    /**
     * The id of every block that draws a casing plate.
     *
     * <p>Read from the blocks themselves so a new machine or casing needs no edit here.</p>
     */
    private static Set<ResourceLocation> casingBlockIds() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof GTCasingProvider)
                .map(BuiltInRegistries.BLOCK::getKey)
                .collect(Collectors.toSet());
    }
}

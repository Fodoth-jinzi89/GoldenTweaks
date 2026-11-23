"""Every port model is wrapped so its plate can be re-skinned.

The hook runs after baking, so it sees the finished model and wraps it. Only port models are
touched — matched by the block state's block, which is what a model is registered against.

One wrapper per model, shared by every block using it: the wrapper is stateless apart from its
plate cache, so there is nothing to keep per position.
"""
import io

path = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"

content = '''package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlock;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

/**
 * Wraps every port model so its plate can follow the casing it is set into.
 *
 * <p>Wrapping at modelload time is what keeps this out of the 150 model files: the wrapper reads
 * the plate from the baked model and swaps only those quads, so a port's own artwork and the casing
 * set can both change without either being duplicated.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortModelWrapping {

    private GTPortModelWrapping() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        // Collected first: the map must not be modified while it is being iterated.
        Map<ModelResourceLocation, BakedModel> wrapped = new java.util.HashMap<>();
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : models.entrySet()) {
            if (isPortModel(entry.getKey())) {
                wrapped.put(entry.getKey(), new GTPortCasingModel(entry.getValue()));
            }
        }
        models.putAll(wrapped);
    }

    /**
     * Whether a model belongs to a port block.
     *
     * <p>Asked of the id's path rather than of a registry lookup: every port model is registered
     * under its block's own id, and the path test cannot fail on a registry that is still loading.</p>
     */
    private static boolean isPortModel(ModelResourceLocation id) {
        return id.id().getPath().startsWith("gt_port_");
    }
}
'''

io.open(path, "w", encoding="utf-8", newline="\n").write(content)
print("model wrapping hook written")

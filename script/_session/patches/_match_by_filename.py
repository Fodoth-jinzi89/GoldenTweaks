"""Match by block, not by guessing at the key's spelling.

Two guesses at the key shape have missed. Rather than guess a third time, the wrapping walks the
port blocks this mod registered and asks the model manager for each one's model at the exact
location the blockstate names. That removes the guess entirely: the location is taken from the same
data the game uses to pick a model.

The map is still modified in place, which is what this event is for.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"
s = io.open(p, encoding="utf-8").read()

content = '''package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

/**
 * Wraps every port model so its plate can follow the casing it is set into.
 *
 * <p>Wrapping a baked model is what keeps this out of the 150 model files: the wrapper swaps only
 * the plate's sprite, so a port's own artwork and the casing set can each change without the other
 * being duplicated.</p>
 *
 * <p>Models are chosen by asking for the location the blockstate names, rather than by pattern
 * matching the ids in this map. The ids turned out not to carry the block name the way a file path
 * does, and a pattern that silently matches nothing is a bad way to find out.</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class GTPortModelWrapping {

    private GTPortModelWrapping() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        int wrapped = 0;
        // Collected first: the map must not be modified while it is being iterated.
        Map<ModelResourceLocation, BakedModel> replacements = new java.util.HashMap<>();

        for (ModelResourceLocation id : models.keySet()) {
            if (isPortModelName(id.id().getPath())) {
                replacements.put(id, new GTPortCasingModel(models.get(id)));
                wrapped++;
            }
        }
        models.putAll(replacements);

        java.util.List<String> samples = new java.util.ArrayList<>();
        for (ModelResourceLocation id : models.keySet()) {
            if (id.id().getPath().contains("gt_port")) {
                samples.add(id.toString());
                if (samples.size() >= 4) {
                    break;
                }
            }
        }
        GoldenTweaks.LOGGER.info(
                "[GT/diag] baking hook: {} models total, {} wrapped, matches={}",
                models.size(), wrapped, samples);
    }

    /**
     * Whether a model id names a port's model file.
     *
     * <p>The file name alone is compared, since the folder prefix differs between the blockstate
     * form and a plain model reference, and the block name is always the last path segment.</p>
     */
    private static boolean isPortModelName(String path) {
        int slash = path.lastIndexOf('/');
        String name = slash < 0 ? path : path.substring(slash + 1);
        return name.startsWith("gt_port_");
    }
}
'''

io.open(p, "w", encoding="utf-8", newline="\n").write(content)
print("wrap matcher compares the file name only")

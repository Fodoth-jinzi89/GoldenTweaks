package net.fodoth.skina.goldentweaks.mixin.fix.avaritia_more_items;

import com.google.gson.JsonElement;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTMultiblockStructures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPattern;
import net.weibai.avaritia_more_items.common.blocks.abstracts.multiblock.pattern.AMiBlockPatternReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Adds our own multiblock structures to {@code avaritia_more_items}' pattern registry.
 *
 * <p>AMI reads {@code data/<ns>/multiblock_patterns/*.json} into a private map keyed by
 * {@code <ns>:<file>}. That format indexes {@code [depth][height][width]} with depth counted
 * northward and height counted downward, which makes a hand-written file almost impossible to
 * check against the structure it describes, so ours are authored bottom-up instead
 * ({@link GTMultiblockStructures}) and injected here, right after AMI swaps in the freshly
 * parsed set.</p>
 *
 * <p>The field is shadowed rather than reached through {@code getPatterns()}, because that
 * getter wraps the map in {@code Collections.unmodifiableMap}. Injecting at the tail of
 * {@code apply} keeps both sources consistent for the whole reload, so {@code /reload}
 * refreshes both.</p>
 */
@Mixin(value = AMiBlockPatternReloadListener.class, remap = false)
public class GTMultiblockPatternsMixin {

    @Shadow
    private Map<ResourceLocation, AMiBlockPattern> patterns;

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;"
                    + "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void goldentweaks$addOwnStructures(Map<ResourceLocation, JsonElement> parsed,
                                               ResourceManager manager,
                                               ProfilerFiller profiler,
                                               CallbackInfo ci) {
        Map<ResourceLocation, AMiBlockPattern> extra = GTMultiblockStructures.load(manager);
        if (extra.isEmpty()) {
            return;
        }

        patterns.putAll(extra);
        GoldenTweaks.LOGGER.info(
                "[GT] Added {} multiblock structure(s) to avaritia_more_items: {}",
                extra.size(), extra.keySet());
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.advanced_infusion;

import com.likeazsua2.advancedinfusion.AdvancedInfusionResearch;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ResearchCache;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;

import java.util.Set;

@Mixin(value = AdvancedInfusionResearch.class, remap = false)
public abstract class AdvancedInfusionResearchMixin {

    @Unique
    private static final Set<String> gt$disabledResearch = Set.of(
            "ADVANCED_INFUSION.CONTROLLER",
            "ADVANCED_INFUSION.THAUMIUM_UPGRADE",
            "ADVANCED_INFUSION.VOID_UPGRADE",
            "ADVANCED_INFUSION.PRIMORDIAL_UPGRADE",
            "ADVANCED_INFUSION.PURIFYING_TEAR",
            "ADVANCED_INFUSION.AURA_NODE_FABRICATOR"
    );

    @Inject(method = "register", at = @At("RETURN"))
    private static void gt$disableDuplicateContent(CallbackInfo ci) {
        if (!GoldenTweaksCommonConfig.disableAdvancedInfusionItems()) {
            return;
        }

        ResearchCategoryList category = ResearchCategories.getResearchList("PRACTICAL_THAUMATURGY");
        if (category != null) {
            category.research.keySet().removeAll(gt$disabledResearch);
            ResearchCache.clear();
        }

        ThaumcraftApi.getCraftingRecipes().removeIf(recipe -> {
            String research = null;
            if (recipe instanceof IArcaneRecipe arcaneRecipe) {
                research = arcaneRecipe.getResearch();
            } else if (recipe instanceof InfusionRecipe infusionRecipe) {
                research = infusionRecipe.getResearch();
            }
            return research != null && gt$disabledResearch.contains(research);
        });
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.mekanism;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Removes Mekanism's ethene + oxygen substrate reaction from the recipe manager
 * so it no longer appears in JEI/EMI or runs in the Pressurized Reaction Chamber.
 * Also drops the old HDPE rod/stick crafting recipes and the two machines' original
 * crafting recipes so the stick-based replacements can take over.
 */
@Mixin(RecipeManager.class)
public class RemoveEtheneOxygenRecipeMixin {

    private static final Set<ResourceLocation> TARGETS = Set.of(
            ResourceLocation.fromNamespaceAndPath("mekanism", "reaction/substrate/ethene_oxygen"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "hdpe_rod"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "hdpe_stick"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "control_circuit/advanced"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "enriching/hdpe_sheet"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "pigment_mixer"),
            ResourceLocation.fromNamespaceAndPath("mekmm", "large_pigment_mixer")
    );

    @ModifyVariable(method = "replaceRecipes", at = @At("HEAD"), argsOnly = true)
    private Iterable<RecipeHolder<?>> gt$removeRecipes(Iterable<RecipeHolder<?>> recipes) {
        List<RecipeHolder<?>> filtered = new ArrayList<>();
        for (RecipeHolder<?> holder : recipes) {
            if (!TARGETS.contains(holder.id())) {
                filtered.add(holder);
            }
        }
        return filtered;
    }
}

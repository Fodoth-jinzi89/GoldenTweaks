package net.fodoth.skina.goldentweaks.mixin.fix.mekanismextras;

import com.jerry.mekextras.common.integration.mekmm.registries.ExtraMoreMachineBlocks;
import com.jerry.mekextras.common.tier.ExtraFactoryTier;
import com.jerry.mekextras.common.util.ExtraEnumUtils;
import com.jerry.mekmm.client.recipe_viewer.emi.MoreMachineEMI;
import com.jerry.mekmm.common.block.attribute.MoreMachineAttributeFactoryType;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import mekanism.common.block.attribute.Attribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = MoreMachineEMI.class, remap = false)
public class MoreMachineEMIMixin {

    @Inject(method = "addWorkstations", at = @At("TAIL"))
    private static void goldentweaks$addExtraFactories(EmiRegistry registry, EmiRecipeCategory category,
                                                       List<ItemLike> workstations, CallbackInfo ci) {
        for (ItemLike workstation : workstations) {
            Item item = workstation.asItem();
            if (item instanceof BlockItem blockItem) {
                MoreMachineAttributeFactoryType factoryType = Attribute.get(blockItem.getBlock(), MoreMachineAttributeFactoryType.class);
                if (factoryType != null) {
                    for (ExtraFactoryTier tier : ExtraEnumUtils.EXTRA_FACTORY_TIERS) {
                        registry.addWorkstation(category, EmiStack.of(ExtraMoreMachineBlocks.getExtraMoreMachineFactory(
                                tier, factoryType.getMoreMachineFactoryType())));
                    }
                }
            }
        }
    }
}

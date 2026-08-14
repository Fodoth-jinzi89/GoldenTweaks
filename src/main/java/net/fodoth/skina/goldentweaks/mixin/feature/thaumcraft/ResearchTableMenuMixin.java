package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import thaumcraft.common.menu.ResearchTableMenu;

@Mixin(value = ResearchTableMenu.class, remap = false)
public class ResearchTableMenuMixin {

    @ModifyConstant(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            constant = @Constant(intValue = 14)
    )
    private int gt$toolsX(int value) {
        return 91;
    }

    @ModifyConstant(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            constant = @Constant(intValue = 70)
    )
    private int gt$notesX(int value) {
        return 235;
    }

    @ModifyConstant(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            constant = @Constant(intValue = 48)
    )
    private int gt$inventoryX(int value) {
        return 91;
    }

    @ModifyConstant(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            constant = @Constant(intValue = 175)
    )
    private int gt$inventoryY(int value) {
        return 198;
    }

    @ModifyConstant(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
            constant = @Constant(intValue = 233)
    )
    private int gt$hotbarY(int value) {
        return 256;
    }
}
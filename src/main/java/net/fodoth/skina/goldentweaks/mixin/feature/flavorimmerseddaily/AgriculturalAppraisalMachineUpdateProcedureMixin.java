package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.fodoth.skina.goldentweaks.compat.flavorimmerseddaily.RootCropProcedure;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModBlocks;
import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.mcreator.flavorimmerseddaily.procedures.农业鉴定器更新Procedure;
import net.mcreator.flavorimmerseddaily.procedures.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(农业鉴定器更新Procedure.class)
public class AgriculturalAppraisalMachineUpdateProcedureMixin {

    @Inject(method = "execute", at = @At("RETURN"), remap = false)
    private static void onExecute(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        // 获取第一个物品槽的物品
        ItemStack slotItem = getSlotItem(world, x, y, z);

        if (slotItem.isEmpty()) return;

        // 判断并调用对应的子处理器
        if (slotItem.getItem() == FlavorImmersedDailyModBlocks.WILDTUBERPLANTS.get().asItem()) {
            RootCropProcedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModBlocks.WILDGRAINPLANT.get().asItem()) {
            农牧园谷物Procedure.execute(world, x + 0.5,y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModBlocks.WILDFLOWERANDLEAF.get().asItem()) {
            农牧园花叶Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModBlocks.WILDSEEDPLANT.get().asItem()) {
            农牧园籽叶Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModBlocks.WILDMUSHROOMPLANT.get().asItem()) {
            农牧园菌类Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModItems.TEMPERATEWILDFRUIT.get()) {
            农牧园温带Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModItems.WILDFRUITINCOLDZONE.get()) {
            农牧园寒带Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        } else if (slotItem.getItem() == FlavorImmersedDailyModItems.TROPICALWILD_FRUIT.get()) {
            农牧园热带Procedure.execute(world, x + 0.5, y + 0.5, z + 0.5);
        }
    }

    private static ItemStack getSlotItem(LevelAccessor world, double x, double y, double z) {
        if (world instanceof ILevelExtension _ext) {
            var handler = _ext.getCapability(ItemHandler.BLOCK, BlockPos.containing(x, y, z), null);
            if (handler != null) {
                return handler.getStackInSlot(0).copy();
            }
        }
        return ItemStack.EMPTY;
    }
}

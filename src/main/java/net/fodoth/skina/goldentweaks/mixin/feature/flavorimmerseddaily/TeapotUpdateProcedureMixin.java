package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.init.FlavorImmersedDailyModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.mcreator.flavorimmerseddaily.procedures.茶壶更新Procedure")
public class TeapotUpdateProcedureMixin {

    /**
     * @reason 重写茶壶配方更新，支持批量堆叠
     * 槽位0 = 输出，槽位1-3 = 输入
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);

        // 获取所有槽位的物品
        ItemStack slot0 = getSlotItem(world, pos, 0);
        ItemStack slot1 = getSlotItem(world, pos, 1);
        ItemStack slot2 = getSlotItem(world, pos, 2);
        ItemStack slot3 = getSlotItem(world, pos, 3);

        // 如果输入槽位有空，直接返回
        if (slot1.isEmpty() || slot2.isEmpty() || slot3.isEmpty()) {
            return;
        }

        // 检查所有配方
        RecipeResult result = checkRecipes(world, pos);

        if (result.matched) {
            // 消耗输入
            consumeSlot(world, pos, 1, 1);
            consumeSlot(world, pos, 2, 1);
            consumeSlot(world, pos, 3, 1);

            // 产出到槽位0（堆叠）
            addToSlot(world, pos, 0, result.outputItem, 1);
        }
    }

    /**
     * 检查所有配方
     */
    @Unique
    private static RecipeResult checkRecipes(LevelAccessor world, BlockPos pos) {
        ItemStack slot0 = getSlotItem(world, pos, 0);
        ItemStack slot1 = getSlotItem(world, pos, 1);
        ItemStack slot2 = getSlotItem(world, pos, 2);
        ItemStack slot3 = getSlotItem(world, pos, 3);

        // 配方1: 阿萨姆奶茶 = 奶瓶 + 干红茶 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.MILKBOTTLE.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.DRYREDTEA.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            // 检查输出槽是否为空或者是阿萨姆奶茶且未满
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.ASSAMMILK_TEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.ASSAMMILK_TEA.get());
            }
        }

        // 配方2: 绿茶拿铁 = 奶瓶 + 干绿茶 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.MILKBOTTLE.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.DRYGREENTEA.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.GREEN_TEA_LATTES.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.GREEN_TEA_LATTES.get());
            }
        }

        // 配方3: 荞麦茶 = 荞麦 + 荞麦 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.BUCKWHEAT.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.BUCKWHEAT.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.BUCKWHEATTEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.BUCKWHEATTEA.get());
            }
        }

        // 配方4: 红茶 = 干红茶 + 干红茶 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.DRYREDTEA.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.DRYREDTEA.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.REDTEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.REDTEA.get());
            }
        }

        // 配方5: 绿茶 = 干绿茶 + 干绿茶 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.DRYGREENTEA.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.DRYGREENTEA.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.GREENTEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.GREENTEA.get());
            }
        }

        // 配方6: 柠檬红茶 = 干红茶 + 柠檬片 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.DRYREDTEA.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.SLICEDLEMON.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.LEMONREDTEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.LEMONREDTEA.get());
            }
        }

        // 配方7: 糯米柠檬茶 = 糯米 + 柠檬片 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.POLISHEDGLUTINOUSRICE_2.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.SLICEDLEMON.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.LEMONTEAWITHGLUTINOUSRICEFLAVOR.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.LEMONTEAWITHGLUTINOUSRICEFLAVOR.get());
            }
        }

        // 配方8: 青梅绿茶 = 干绿茶 + 青梅 + 清水
        if (slot1.getItem() == FlavorImmersedDailyModItems.DRYGREENTEA.get() &&
                slot2.getItem() == FlavorImmersedDailyModItems.GREENPLUM.get() &&
                slot3.getItem() == FlavorImmersedDailyModItems.TIDYWATER.get()) {
            if (canAddToSlot(slot0, FlavorImmersedDailyModItems.GREENPLUMTEA.get())) {
                return new RecipeResult(true, FlavorImmersedDailyModItems.GREENPLUMTEA.get());
            }
        }

        return new RecipeResult(false, null);
    }

    /**
     * 检查是否可以添加到输出槽
     */
    @Unique
    private static boolean canAddToSlot(ItemStack currentSlot, ItemLike outputItem) {
        if (currentSlot.isEmpty()) {
            return true;
        }
        // 检查是否是同一种物品且未满64
        return currentSlot.getItem() == outputItem.asItem() && currentSlot.getCount() < 64;
    }

    /**
     * 获取指定槽位的物品
     */
    @Unique
    private static ItemStack getSlotItem(LevelAccessor world, BlockPos pos, int slot) {
        if (!(world instanceof ILevelExtension ext)) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler != null) {
            return handler.getStackInSlot(slot).copy();
        }
        return ItemStack.EMPTY;
    }

    /**
     * 消耗指定槽位的物品
     */
    @Unique
    private static void consumeSlot(LevelAccessor world, BlockPos pos, int slot, int amount) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            ItemStack stack = modifiable.getStackInSlot(slot).copy();
            if (!stack.isEmpty()) {
                stack.shrink(amount);
                if (stack.getCount() <= 0) {
                    modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                } else {
                    modifiable.setStackInSlot(slot, stack);
                }
            }
        }
    }

    /**
     * 添加物品到指定槽位（堆叠）
     */
    @Unique
    private static void addToSlot(LevelAccessor world, BlockPos pos, int slot, ItemLike item, int amount) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            ItemStack current = modifiable.getStackInSlot(slot).copy();
            if (current.isEmpty()) {
                ItemStack newStack = new ItemStack(item);
                newStack.setCount(amount);
                modifiable.setStackInSlot(slot, newStack);
            } else if (current.getItem() == item.asItem()) {
                int newCount = Math.min(current.getCount() + amount, 64);
                current.setCount(newCount);
                modifiable.setStackInSlot(slot, current);
            }
        }
    }

        @Unique
        private record RecipeResult(boolean matched, ItemLike outputItem) {
    }
}
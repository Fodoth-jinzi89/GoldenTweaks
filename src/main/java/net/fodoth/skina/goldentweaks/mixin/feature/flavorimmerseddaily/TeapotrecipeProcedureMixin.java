package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(targets = "net.mcreator.flavorimmerseddaily.procedures.TeapotrecipeProcedure")
public class TeapotrecipeProcedureMixin {

    /**
     * @reason 重写茶壶配方匹配
     * 槽位0 = 输出（产物），槽位1-3 = 输入（原料）
     * 支持批量堆叠
     * @author Fodoth_jinzi89
     */
    @Overwrite
    public static void execute(LevelAccessor world, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);

        // 检查方块状态是否为1（有盖子）
        int currentState = getBlockState(world, pos);
        if (currentState != 1) {
            return;
        }

        // 获取槽位1-3的输入物品
        ItemStack slot1 = getSlotItem(world, pos, 1);
        ItemStack slot2 = getSlotItem(world, pos, 2);
        ItemStack slot3 = getSlotItem(world, pos, 3);

        // 如果有任何输入槽为空，直接返回
        if (slot1.isEmpty() || slot2.isEmpty() || slot3.isEmpty()) {
            return;
        }

        // 检查所有配方
        String matchedOutput = checkRecipes(slot1, slot2, slot3);

        if (matchedOutput != null) {
            // 检查输出槽（槽位0）是否可容纳
            ItemStack outputSlot = getSlotItem(world, pos, 0);
            ItemStack resultItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(matchedOutput)));

            if (!canAddToSlot(outputSlot, resultItem)) {
                return;
            }

            // 消耗输入（槽位1-3各减1）
            consumeSlot(world, pos, 1, 1);
            consumeSlot(world, pos, 2, 1);
            consumeSlot(world, pos, 3, 1);

            // 产出到槽位0（堆叠）
            addToSlot(world, pos, 0, resultItem, 1);

            // 播放合成音效
            playSound(world, x, y, z, "entity.player.levelup");
        }
    }

    /**
     * 检查所有配方，返回匹配的输出物品ID
     */
    @Unique
    private static String checkRecipes(ItemStack slot1, ItemStack slot2, ItemStack slot3) {
        String id1 = BuiltInRegistries.ITEM.getKey(slot1.getItem()).toString();
        String id2 = BuiltInRegistries.ITEM.getKey(slot2.getItem()).toString();
        String id3 = BuiltInRegistries.ITEM.getKey(slot3.getItem()).toString();

        // 配方映射：按顺序 [槽位1, 槽位2, 槽位3] -> 输出
        Map<List<String>, String> recipes = new HashMap<>();

        // 茶壶配方
        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:milkbottle",
                "flavor_immersed_daily:dryredtea"
        ), "flavor_immersed_daily:assammilk_tea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:milkbottle",
                "flavor_immersed_daily:drygreentea"
        ), "flavor_immersed_daily:green_tea_lattes");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:buckwheat",
                "flavor_immersed_daily:buckwheat"
        ), "flavor_immersed_daily:buckwheattea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:dryredtea",
                "flavor_immersed_daily:dryredtea"
        ), "flavor_immersed_daily:redtea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:drygreentea",
                "flavor_immersed_daily:drygreentea"
        ), "flavor_immersed_daily:greentea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:dryredtea",
                "flavor_immersed_daily:slicedlemon"
        ), "flavor_immersed_daily:lemonredtea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:drygreentea",
                "flavor_immersed_daily:greenplum"
        ), "flavor_immersed_daily:greenplumtea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:polishedglutinousrice_2",
                "flavor_immersed_daily:slicedlemon"
        ), "flavor_immersed_daily:greentea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:tidywater",
                "flavor_immersed_daily:salt",
                "flavor_immersed_daily:lemonjam"
        ), "flavor_immersed_daily:electrolytebeverage");

        recipes.put(Arrays.asList(
                "minecraft:sugar",
                "flavor_immersed_daily:lemonjam",
                "flavor_immersed_daily:redteapowder"
        ), "flavor_immersed_daily:icedblacktea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:salt",
                "flavor_immersed_daily:lemonjam",
                "flavor_immersed_daily:redteapowder"
        ), "flavor_immersed_daily:sugarfreicedtea");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:sorbet",
                "flavor_immersed_daily:lemonjam",
                "flavor_immersed_daily:redteapowder"
        ), "flavor_immersed_daily:icedblackteablue");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:walnutkinnel",
                "flavor_immersed_daily:walnutpowder",
                "minecraft:sugar"
        ), "flavor_immersed_daily:healthwalnutdew");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:peanutjam",
                "flavor_immersed_daily:peanutpowder",
                "minecraft:sugar"
        ), "flavor_immersed_daily:healthpeanutmilk");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:concentratedsyrup",
                "flavor_immersed_daily:nahco_3",
                "minecraft:sugar"
        ), "flavor_immersed_daily:cola");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:whitesugarsyrup",
                "flavor_immersed_daily:nahco_3",
                "flavor_immersed_daily:lemonjam"
        ), "flavor_immersed_daily:sprite");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:milkbottle",
                "minecraft:sugar",
                "flavor_immersed_daily:oat"
        ), "flavor_immersed_daily:wheatmilk");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:whitesugarsyrup",
                "flavor_immersed_daily:milkbottle",
                "minecraft:sugar"
        ), "flavor_immersed_daily:sweetmilk");

        recipes.put(Arrays.asList(
                "flavor_immersed_daily:loquatjam",
                "flavor_immersed_daily:brownsugarsyrup",
                "minecraft:sugar"
        ), "flavor_immersed_daily:healthloquatcream");

        // 匹配（忽略顺序）
        List<String> inputList = Arrays.asList(id1, id2, id3);
        Collections.sort(inputList);

        for (Map.Entry<List<String>, String> entry : recipes.entrySet()) {
            List<String> patternList = new ArrayList<>(entry.getKey());
            Collections.sort(patternList);
            if (inputList.equals(patternList)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 获取茶壶方块状态
     */
    @Unique
    private static int getBlockState(LevelAccessor world, BlockPos pos) {
        net.minecraft.world.level.block.state.BlockState state = world.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("blockstate");
        if (property instanceof IntegerProperty intProp) {
            return state.getValue(intProp);
        }
        return -1;
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
     * 检查是否可以添加到输出槽
     */
    @Unique
    private static boolean canAddToSlot(ItemStack currentSlot, ItemStack resultItem) {
        if (currentSlot.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItem(currentSlot, resultItem) && currentSlot.getCount() < 64;
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
    private static void addToSlot(LevelAccessor world, BlockPos pos, int slot, ItemStack item, int amount) {
        if (!(world instanceof ILevelExtension ext)) {
            return;
        }
        IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        if (handler instanceof IItemHandlerModifiable modifiable) {
            ItemStack current = modifiable.getStackInSlot(slot).copy();
            if (current.isEmpty()) {
                ItemStack newStack = item.copy();
                newStack.setCount(amount);
                modifiable.setStackInSlot(slot, newStack);
            } else if (ItemStack.isSameItem(current, item)) {
                int newCount = Math.min(current.getCount() + amount, 64);
                current.setCount(newCount);
                modifiable.setStackInSlot(slot, current);
            }
        }
    }

    /**
     * 播放音效
     */
    @Unique
    private static void playSound(LevelAccessor world, double x, double y, double z, String soundId) {
        if (!(world instanceof Level level)) {
            return;
        }
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId));
        if (sound == null) {
            return;
        }
        if (!level.isClientSide()) {
            level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else {
            level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }
    }
}
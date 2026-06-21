package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import net.mcreator.flavorimmerseddaily.procedures.Jar_roundProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Jar_roundProcedure.class)
public class Jar_roundProcedureMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void onExecute(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        // 获取坛子的方块状态
        var blockState = world.getBlockState(BlockPos.containing(x, y, z));
        var property = blockState.getBlock().getStateDefinition().getProperty("blockstate");

        if (!(property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty)) {
            return;
        }

        // 获取物品栏
        IItemHandler itemHandler = null;

        if (world instanceof ILevelExtension _ext) {
            itemHandler = _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), Direction.UP);
        }
        if (!(itemHandler instanceof IItemHandlerModifiable handler)) {
            return;
        }

        // 读取4个输入槽位的物品
        ItemStack[] inputStacks = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            inputStacks[i] = handler.getStackInSlot(i).copy();
        }

        // 构建输入字符串（用于配方匹配）
        StringBuilder inputBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (!inputStacks[i].isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(inputStacks[i].getItem()).toString();
                inputBuilder.append(itemId).append("+");
            }
        }

        String inputStr = inputBuilder.toString();
        if (!inputStr.endsWith("+")) {
            return;
        }
        inputStr = inputStr.substring(0, inputStr.length() - 1); // 移除末尾的+

        // 配方列表（从原代码复制）
        List<String> recipeList = getRecipeList();

        for (String recipeEntry : recipeList) {
            if (!recipeEntry.contains("=") || !recipeEntry.contains("}")) {
                continue;
            }

            String[] parts = recipeEntry.split("=");
            if (parts.length < 2) {
                continue;
            }

            String inputPattern = parts[0].trim();
            String outputId = parts[1].substring(0, parts[1].indexOf("}")).trim().toLowerCase(Locale.ENGLISH);

            // 检查输入是否匹配
            if (!matchInput(inputStr, inputPattern)) {
                continue;
            }

            // 计算最大可制作数量
            int maxCrafts = calculateMaxCrafts(inputStacks, inputPattern);
            if (maxCrafts <= 0) {
                continue;
            }

            // 获取产物槽位的当前物品
            ItemStack outputSlot = handler.getStackInSlot(4);
            ItemStack resultItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(outputId)));

            // 检查产物槽位是否能容纳这么多物品
            int currentCount = outputSlot.isEmpty() ? 0 : outputSlot.getCount();
            int maxStack = resultItem.getMaxStackSize();

            // 如果产物槽不是空的且物品不匹配，跳过
            if (!outputSlot.isEmpty()) {
                // 自定义物品比较 - 只比较物品ID，忽略NBT标签
                String outputId1 = BuiltInRegistries.ITEM.getKey(outputSlot.getItem()).toString();
                String outputId2 = BuiltInRegistries.ITEM.getKey(resultItem.getItem()).toString();
                if (!outputId1.equals(outputId2)) {
                    continue; // 产物槽有不同物品，不能合成
                }
            }

            int totalCount = currentCount + maxCrafts;
            if (totalCount > maxStack) {
                maxCrafts = maxStack - currentCount;
                if (maxCrafts <= 0) {
                    continue;
                }
            }

            // 消耗原料（按比例从各个槽位扣除）
            consumeIngredients(handler, inputStacks, inputPattern, maxCrafts);

            // 设置产物
            resultItem.setCount(totalCount);
            handler.setStackInSlot(4, resultItem);

            // 播放音效
            if (world instanceof net.minecraft.world.level.Level level) {
                var sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.levelup"));
                if (!level.isClientSide()) {
                    if (sound != null) {
                        level.playSound(null, BlockPos.containing(x, y, z), sound,
                                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }
                }
            }

            break; // 找到匹配的配方后停止
        }

        ci.cancel();
    }

    @Unique
    private static @NotNull List<String> getRecipeList() {
        String recipe = "【注意！配方可以在引号范围内任意行列添加，但是请不要输入回车！！请确保配方文本在本引号的内部，更新模组时请备份您添加的配方，避免被覆盖】 【在下方可以复制您按照格式写好的配方】 【在上方可以复制您按照格式写好的配方】 烟火凡人心 坛子配方 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:tidywater=flavor_immersed_daily:beansprout} 豆芽 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:soy} 酱油 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:garlicpowder+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:thickbroadbeansauce} 豆瓣酱 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:groundpowder+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:thickbroadbeansauce} 豆瓣酱 {flavor_immersed_daily:chineseleaves+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt+flavor_immersed_daily:salt=flavor_immersed_daily:pickledvegetable} 酸菜 {flavor_immersed_daily:cabbage+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt+flavor_immersed_daily:salt=flavor_immersed_daily:pickledvegetable} 酸菜 {flavor_immersed_daily:eggwrappedingravel+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:raw_preservedegg} 松花蛋 {minecraft:egg+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:rawsaltyegg} 咸蛋 {flavor_immersed_daily:shreddercucumber+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:slicedsaltycucumber} 腌黄瓜丝 {flavor_immersed_daily:slicedcucumber+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:saltydicedcurumber} 腌黄瓜片 {flavor_immersed_daily:cookedsausage+flavor_immersed_daily:salt=flavor_immersed_daily:curedsausage} 腊肠 {minecraft:cooked_porkchop+flavor_immersed_daily:salt=flavor_immersed_daily:curedmeat} 腊肉 {flavor_immersed_daily:cookedpigtenderloin+flavor_immersed_daily:salt=flavor_immersed_daily:curedmeat} 腊肉 {flavor_immersed_daily:dicedradish+flavor_immersed_daily:tidywater+flavor_immersed_daily:salt=flavor_immersed_daily:saltyraddish} 腌萝卜块 {flavor_immersed_daily:grape+flavor_immersed_daily:grape+flavor_immersed_daily:grape=flavor_immersed_daily:grapewine} 葡萄酒 {flavor_immersed_daily:greengrape+flavor_immersed_daily:greengrape+flavor_immersed_daily:greengrape=flavor_immersed_daily:grapewine} 葡萄酒 {minecraft:wheat+flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:tidywater=flavor_immersed_daily:kweichow_moutai} 粗酿白酒 {minecraft:wheat+minecraft:wheat_seeds+flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:tidywater=flavor_immersed_daily:tsingtao_beer} 大麦啤酒 {flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:kaolianggarin+flavor_immersed_daily:tidywater=flavor_immersed_daily:kaoliangwine} 高粱老酒 {flavor_immersed_daily:cookedchickenfeet+flavor_immersed_daily:salt+flavor_immersed_daily:greenpepper+flavor_immersed_daily:tidywater=flavor_immersed_daily:chickenfeetwithpeppers} 泡椒凤爪 {flavor_immersed_daily:cookedchickenfeet+flavor_immersed_daily:salt+flavor_immersed_daily:slicedlemon+flavor_immersed_daily:tidywater=flavor_immersed_daily:bonelesslemonchickenfeet} 无骨柠檬凤爪 {flavor_immersed_daily:milkbottle+flavor_immersed_daily:tidywater=flavor_immersed_daily:probiotics} 益生菌落 {flavor_immersed_daily:milkbottle+flavor_immersed_daily:probiotics+minecraft:sugar=flavor_immersed_daily:yogurt} 酸奶 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:tidywater+flavor_immersed_daily:paddygrain=flavor_immersed_daily:vinegar} 食醋 {flavor_immersed_daily:soybean_0+flavor_immersed_daily:tidywater+flavor_immersed_daily:polishedglutinousrice_2=flavor_immersed_daily:vinegar} 食醋";

        // 解析配方
        return new ArrayList<>(Arrays.asList(recipe.split("\\{")));
    }

    /**
     * 检查输入是否匹配配方模式
     */
    @Unique
    private static boolean matchInput(String inputStr, String pattern) {
        String[] patternItems = pattern.split("\\+");
        String[] inputItems = inputStr.split("\\+");

        if (patternItems.length != inputItems.length) {
            return false;
        }

        // 排序后比较（忽略顺序）
        Arrays.sort(patternItems);
        Arrays.sort(inputItems);
        return Arrays.equals(patternItems, inputItems);
    }

    /**
     * 计算最大可制作数量
     */
    @Unique
    private static int calculateMaxCrafts(ItemStack[] inputStacks, String pattern) {
        String[] patternItems = pattern.split("\\+");
        Map<String, Integer> requiredCount = new HashMap<>();

        // 统计配方需要的每种物品数量
        for (String item : patternItems) {
            requiredCount.put(item, requiredCount.getOrDefault(item, 0) + 1);
        }

        // 计算每个输入槽位能提供的数量
        int maxCrafts = Integer.MAX_VALUE;
        for (String requiredItem : requiredCount.keySet()) {
            int available = 0;
            for (ItemStack stack : inputStacks) {
                if (!stack.isEmpty()) {
                    String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    if (stackId.equals(requiredItem)) {
                        available += stack.getCount();
                    }
                }
            }
            int possible = available / requiredCount.get(requiredItem);
            maxCrafts = Math.min(maxCrafts, possible);
        }

        return maxCrafts;
    }

    /**
     * 消耗原料
     */
    @Unique
    private static void consumeIngredients(IItemHandlerModifiable handler, ItemStack[] inputStacks, String pattern, int amount) {
        String[] patternItems = pattern.split("\\+");
        Map<String, Integer> toConsume = new HashMap<>();

        // 计算每种物品需要消耗的总数量
        for (String item : patternItems) {
            toConsume.put(item, toConsume.getOrDefault(item, 0) + amount);
        }

        // 从各个槽位依次扣除
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            String stackId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            Integer need = toConsume.get(stackId);
            if (need != null && need > 0) {
                int toTake = Math.min(need, stack.getCount());
                stack.shrink(toTake);
                toConsume.put(stackId, need - toTake);

                if (stack.getCount() <= 0) {
                    handler.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}

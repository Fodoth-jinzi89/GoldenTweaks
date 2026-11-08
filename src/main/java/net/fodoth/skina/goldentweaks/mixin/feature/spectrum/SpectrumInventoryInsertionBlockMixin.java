package net.fodoth.skina.goldentweaks.mixin.feature.spectrum;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.dafuqs.spectrum.helpers.SpectrumEnchantmentHelper;
import de.dafuqs.spectrum.registries.SpectrumEnchantmentKeys;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * 光谱「自动收集」（{@code spectrum:inventory_insertion}）的挖掘掉落改走 GoldenTweaks 的统一拾取。
 * <p>
 * 光谱的直接入包由 {@link SpectrumInsertionTagMixin} 关掉，这里在 {@code Block#getDrops} 的返回点
 * 接住掉落列表，逐份生成掉落物后交给 {@link ItemPickupUtil#collect}（触发拾取事件、统计，满包时
 * 余量飞向玩家），并返回空列表。
 * <p>
 * {@code priority = 1500} 高于光谱默认的 1000：同一返回点的注入按 mixin 优先级升序应用，低优先级
 * 先应用、代码更靠左、先执行，因此光谱自己的处理（重击/自动烧炼等前置变换）先跑，本处理拿到的是
 * 变换后的结果。
 */
@Mixin(value = Block.class, priority = 1500)
public class SpectrumInventoryInsertionBlockMixin {

    @ModifyReturnValue(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN")
    )
    private static List<ItemStack> gt$collectIntoInventory(
            List<ItemStack> drops,
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            BlockEntity blockEntity,
            Entity breaker,
            ItemStack tool
    ) {

        if (drops.isEmpty() || !(breaker instanceof Player player)) {
            return drops;
        }

        // 效果标签已被 SpectrumInsertionTagMixin 关掉，这里改用附魔 key 判定
        if (!SpectrumEnchantmentHelper.hasEnchantment(
                level.registryAccess(), SpectrumEnchantmentKeys.INVENTORY_INSERTION, tool)) {
            return drops;
        }

        Vec3 center = Vec3.atCenterOf(pos);

        for (ItemStack drop : drops) {
            ItemPickupUtil.collect(player, drop, level, center);
        }

        return List.of();
    }
}

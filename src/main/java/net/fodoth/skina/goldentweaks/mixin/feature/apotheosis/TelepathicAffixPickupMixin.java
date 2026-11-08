package net.fodoth.skina.goldentweaks.mixin.feature.apotheosis;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.effect.TelepathicAffix;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 灾变（Apotheosis）的「时空」词缀（{@code telepathic}，掉落物传送至你身边）原本只把已生成的
 * 掉落物 {@code ItemEntity.setPos(玩家位置)} 并清零拾取延迟，交给原版 {@code playerTouch} 拾取，
 * 与 GoldenTweaks 的拾取口径不一致。
 * <p>
 * 这里在 {@link TelepathicAffix} 的两个事件处理入口接管：仍然沿用灾变自己的词缀判定
 * （{@link AffixHelper#streamAffixes} + {@link AffixInstance#enablesTelepathy}），把命中的掉落物
 * 改走 GoldenTweaks 的统一拾取（{@link ItemPickupUtil#pickup}）——触发拾取事件、统计，并在背包
 * 放不下时把余量飞向玩家。击杀者不是玩家时（例如带词缀的生物）不接管，仍走灾变原逻辑。
 */
@Mixin(value = TelepathicAffix.class, remap = false)
public class TelepathicAffixPickupMixin {

    @Inject(
            method = "drops(Lnet/neoforged/neoforge/event/entity/living/LivingDropsEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gt$collectLivingDrops(LivingDropsEvent event, CallbackInfo ci) {

        Player player = gt$telepathicPlayer(event.getSource());

        if (player == null) {
            return;
        }

        ci.cancel();

        for (ItemEntity drop : event.getDrops()) {
            ItemPickupUtil.pickup(player, drop);
        }
    }

    @Inject(
            method = "drops(Lnet/neoforged/neoforge/event/level/BlockDropsEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gt$collectBlockDrops(BlockDropsEvent event, CallbackInfo ci) {

        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        if (!gt$hasTelepathy(player.getMainHandItem())) {
            return;
        }

        ci.cancel();

        for (ItemEntity drop : event.getDrops()) {
            ItemPickupUtil.pickup(player, drop);
        }

        // BlockDropsEvent 的掉落物此时还没有加入世界（事件结束才加入）：
        // 已整份入包的物品被 pickup 标记为移除，这里同步从列表剔除，避免它们随后又被放回世界。
        event.getDrops().removeIf(Entity::isRemoved);
    }

    /** 与灾变一致地找出「造成这次击杀、且带时空词缀」的玩家；非玩家则不接管。 */
    private static Player gt$telepathicPlayer(DamageSource source) {

        Entity direct = source.getDirectEntity();

        if (direct instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof Player player && gt$hasTelepathy(arrow)) {
                return player;
            }
        } else if (direct instanceof Player player && gt$hasTelepathy(player.getMainHandItem())) {
            return player;
        }

        return null;
    }

    private static boolean gt$hasTelepathy(ItemStack stack) {
        return AffixHelper.streamAffixes(stack).anyMatch(AffixInstance::enablesTelepathy);
    }

    private static boolean gt$hasTelepathy(AbstractArrow arrow) {
        return AffixHelper.streamAffixes(arrow).anyMatch(AffixInstance::enablesTelepathy);
    }
}

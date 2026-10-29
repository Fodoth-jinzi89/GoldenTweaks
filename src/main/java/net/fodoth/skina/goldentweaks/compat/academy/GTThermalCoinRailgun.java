package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.world.AcademyEntities;
import com.mohistmc.academy.world.AcademySounds;
import com.mohistmc.academy.world.entity.CoinEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 让热力（Thermal）的硬币也能像学院币一样「右键抛出」。
 *
 * <p>热力硬币本身没有任何使用行为，无法走学院的 {@code Coin#use}；这里改用右键物品事件，
 * 在服务端按与学院币完全一致的参数抛出一枚 {@link CoinEntity}，并把硬币物品 id 记到实体上
 * （见 {@link GTCoinVariant}）。抛起之后即可用超电磁炮（{@code railgun}）技能把它当作弹药发射，
 * 伤害/破坏/光束按 {@link GTThermalRailgunCoins} 的倍率缩放。</p>
 */
public final class GTThermalCoinRailgun {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!GTThermalRailgunCoins.isThermalCoin(stack)) {
            return;
        }

        Player player = event.getEntity();
        // 接管本次右键：不再走原版物品使用流程
        event.setCanceled(true);

        // 与学院币一致：空中已有一枚硬币时抛出失败
        if (CoinEntity.hasPlayerCoinInAir(player)) {
            return;
        }

        CoinEntity coin = new CoinEntity(AcademyEntities.COIN_ENTITY.get(), level);
        coin.setPos(player.getX(), player.getY(), player.getZ());
        coin.setThrower(player);
        ((GTCoinVariant) coin).gt$setCoinVariant(GTThermalRailgunCoins.variantOf(stack));
        coin.setDeltaMovement(0.0D, player.getDeltaMovement().y + 0.92D, 0.0D);
        coin.setYRot(level.random.nextFloat() * 360.0F);
        coin.setXRot(level.random.nextFloat() * 360.0F);
        if (!level.addFreshEntity(coin) || !coin.isAlive()) {
            coin.discard();
            return;
        }

        player.playSound(AcademySounds.ENTITY_FLIPCOIN.value(), 0.5F, 1.0F);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.swing(event.getHand());
    }

    private GTThermalCoinRailgun() {
    }
}

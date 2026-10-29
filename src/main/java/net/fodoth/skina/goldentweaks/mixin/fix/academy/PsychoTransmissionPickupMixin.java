package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.skill.ability.telekinesis.PsychoTransmissionEffect;
import net.fodoth.skina.goldentweaks.util.ItemPickupUtil;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 念动力·隔空取物（{@code psycho_transmission}）原本直接调用 {@code ItemEntity.playerTouch(player)}，
 * 走的是原版拾取流程。这里改为走 GoldenTweaks 的 {@link ItemPickupUtil#pickup}，与右键/快捷拾取共用同一套
 * 物品拾取事件（{@code ItemEntityPickupEvent.Pre/Post}）、统计与满包处理逻辑。
 */
@Mixin(PsychoTransmissionEffect.class)
public class PsychoTransmissionPickupMixin {

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;playerTouch(Lnet/minecraft/world/entity/player/Player;)V"))
    private static void gt$useGoldenTweaksPickup(ItemEntity item, Player player) {
        ItemPickupUtil.pickup(player, item);
    }
}

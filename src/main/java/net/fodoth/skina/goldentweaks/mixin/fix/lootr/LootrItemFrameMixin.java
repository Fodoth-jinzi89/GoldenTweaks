package net.fodoth.skina.goldentweaks.mixin.fix.lootr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.entity.LootrItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 过滤 Lootr 物品展示框中关于“潜行才能取出战利品矿车”的提示消息。
 * <p>
 * 当展示框内还有物品时，Lootr 会反复发送 cart_should_sneak / cart_should_sneak2 提示，
 * 这里直接吞掉这两条消息，避免刷屏；其余消息正常放行。
 */
@Mixin(LootrItemFrame.class)
public class LootrItemFrameMixin {

    @WrapOperation(
            method = "maybeMessagePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void gt$filterCartMessage(
            Player instance, Component chatComponent, boolean actionBar, Operation<Void> original
    ) {
        LootrItemFrame frame = (LootrItemFrame) (Object) this;

        if (chatComponent.getContents() instanceof TranslatableContents contents
                && (contents.getKey().equals("lootr.message.cart_should_sneak")
                || contents.getKey().equals("lootr.message.cart_should_sneak2"))
                && !frame.getItem().isEmpty()) {
            return;
        }

        original.call(instance, chatComponent, actionBar);
    }
}

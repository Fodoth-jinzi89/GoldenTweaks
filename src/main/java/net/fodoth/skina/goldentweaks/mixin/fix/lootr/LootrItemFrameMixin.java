package net.fodoth.skina.goldentweaks.mixin.fix.lootr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.entity.LootrItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootrItemFrame.class)
public class LootrItemFrameMixin {


    @WrapOperation(
            method = "maybeMessagePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void lootr$filterCartMessage(
            Player instance, Component chatComponent, boolean actionBar, Operation<Void> original
    ) {

        LootrItemFrame frame = (LootrItemFrame)(Object)this;

        if (
                chatComponent.getContents() instanceof TranslatableContents contents
                        && (
                        contents.getKey().equals("lootr.message.cart_should_sneak")
                                || contents.getKey().equals("lootr.message.cart_should_sneak2")
                )
                        && !frame.getItem().isEmpty()
        ) {
            return;
        }

        original.call(instance, chatComponent, actionBar);
    }
}

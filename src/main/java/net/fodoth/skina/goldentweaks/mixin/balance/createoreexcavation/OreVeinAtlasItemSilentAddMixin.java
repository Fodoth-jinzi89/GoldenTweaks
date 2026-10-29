package net.fodoth.skina.goldentweaks.mixin.balance.createoreexcavation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tom.createores.item.OreVeinAtlasItem;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.GTVeinAtlasMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 黄金探矿杖一次会往图鉴里写很多条矿脉，而 {@code addVein} 每调用一次都会发一句
 * 「矿脉已添加至矿脉图鉴」，成批写入时会刷屏。这里在成批写入期间吞掉这句话，
 * 其它来源（样本钻井等）的调用保持不变。
 */
@Mixin(value = OreVeinAtlasItem.class, remap = false)
public abstract class OreVeinAtlasItemSilentAddMixin {

    @WrapOperation(
            method = "addVein",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            ),
            remap = false
    )
    private void gt$silenceBatchAddMessage(Player player, Component message, boolean actionBar,
                                           Operation<Void> original) {
        if (!GTVeinAtlasMessages.isBatch()) {
            original.call(player, message, actionBar);
        }
    }
}

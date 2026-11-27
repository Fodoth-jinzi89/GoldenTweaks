package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.block.generator.SolarFootprint;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SolarFootprint.class, remap = false)
public abstract class MioIcifSolarFootprintMessageMixin {

    @Redirect(
            method = {"place", "onPlace"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private static void gt$silenceSolarFootprintMessage(Player player, Component message, boolean actionBar) {
    }
}

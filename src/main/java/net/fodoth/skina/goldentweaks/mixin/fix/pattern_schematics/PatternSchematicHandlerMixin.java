package net.fodoth.skina.goldentweaks.mixin.fix.pattern_schematics;

import com.cak.pattern_schematics.foundation.mirror.PatternSchematicHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PatternSchematicHandler.class)
public abstract class PatternSchematicHandlerMixin {

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
            )
    )
    private GameType goldentweaks$preventNullGameMode(
            MultiPlayerGameMode instance, Operation<GameType> original
    ) {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return GameType.SURVIVAL;
        }

        return original.call(gameMode);
    }
}

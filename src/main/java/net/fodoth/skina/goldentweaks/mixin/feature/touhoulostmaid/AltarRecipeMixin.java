package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import net.fodoth.skina.goldentweaks.compat.touhoulostmaid.AltarSummonGuard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 祭坛召唤女仆时开启 {@link AltarSummonGuard}，让迷失模组的加入世界事件跳过迷失判定。
 * <p>
 * spawn_box 配方走 {@code spawnBoxMaid}，把女仆挂到箱子上再
 * {@code tryAddFreshEntityWithPassengers}；reborn_maid 配方走 {@code rebornMaid}，
 * 直接 {@code addFreshEntity}。两条路径都在这里用 try/finally 包住，避免异常时标记残留。
 */
@Mixin(AltarRecipe.class)
public class AltarRecipeMixin {

    @Redirect(
            method = "rebornMaid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean goldenTweaks$guardRebornMaid(ServerLevel level, Entity entity) {
        AltarSummonGuard.begin();
        try {
            return level.addFreshEntity(entity);
        } finally {
            AltarSummonGuard.end();
        }
    }

    @Redirect(
            method = "spawnBoxMaid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean goldenTweaks$guardBoxMaid(ServerLevel level, Entity entity) {
        AltarSummonGuard.begin();
        try {
            return level.tryAddFreshEntityWithPassengers(entity);
        } finally {
            AltarSummonGuard.end();
        }
    }
}

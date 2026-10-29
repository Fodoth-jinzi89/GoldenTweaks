package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.config.DynamicSkillRules;
import com.mohistmc.academy.skill.PlayerAbilityData;
import com.mohistmc.academy.skill.ability.vecmanip.StormWingEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 风暴之翼（storm_wing）的飞行在两个阶段表现异常：
 *
 * <p>1. 蓄力阶段就开始飞行：{@link StormWingEffect#onChargingStart} 一进入蓄力就把
 * {@code abilities.mayfly} 置为 true，于是还没蓄满就能飞。这里把「开始蓄力时授予飞行」这一步
 * 直接跳过，飞行改为在 {@link StormWingEffect#performActivate}（蓄满激活）时授予，
 * 于是蓄力阶段无法飞行、蓄满后才获得飞行能力。</p>
 *
 * <p>2. 蓄满后不断上升、无法控制：{@link StormWingEffect#performFlightEffects} 每刻都用
 * {@code setDeltaMovement} 直接改写玩家速度，且无方向输入时走「悬停」分支，每刻给 Y 速度再加
 * 0.078，导致只会上飘、按跳跃/潜行也无法下降。这里改为不再接管位移，只保留原有经验与坠落距离
 * 重置，让玩家用被授予的 {@code mayfly} 走原版飞行，从而完全可控。</p>
 *
 * <p>注意：本 mixin 刻意不使用 {@code @Inject(cancellable = true)} 这类带回调的注入器。
 * 在该整合包（Sinytra Connector）环境下，对 academy 该类实例方法的回调式注入会出现
 * {@code CallbackInfoReturnable} 实参为 null 的崩溃，因此统一改用不涉及回调的注入方式。</p>
 */
@Mixin(StormWingEffect.class)
public class StormWingEffectMixin {

    @Redirect(method = "onChargingStart",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z",
                    opcode = Opcodes.PUTFIELD))
    private static void gt$noFlightWhileCharging(Abilities abilities, boolean value) {
        // 蓄力阶段不授予飞行，留到蓄满激活时由 performActivate 授予
    }

    /**
     * @author GoldenTweaks
     * @reason storm_wing 蓄满后的飞行交给原版 {@code mayfly} 接管：原实现在无方向输入时每刻给 Y
     * 速度追加 0.078，表现为持续上飘且按跳跃/潜行无法下降（详见类注释）。
     */
    @Overwrite
    private boolean performFlightEffects(ServerPlayer player, PlayerAbilityData data) {
        if (!player.getAbilities().mayfly) {
            return false;
        }
        DynamicSkillRules.addExp(player, data, "storm_wing", 5.0E-5F);
        player.fallDistance = 0.0F;
        return true;
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.academy;

import com.mohistmc.academy.client.KeyInputHandler;
import com.mohistmc.academy.network.ChargingCancelPacket;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 学院（AcademyCraft）的蓄力技能用「客户端发起、服务端确认」的握手机制维护蓄力槽位：
 * {@link KeyInputHandler} 在按键按下时置位本地槽位并发送 SkillKeyDownPacket，此后只有当服务端
 * 返回一条能被接受的 SyncChargingStatePacket（确认 / 周期更新 / 结束）时才会复位该槽位。
 *
 * <p>只要这条确认永远不到达、或永远无法匹配，本地槽位就会一直停留在「蓄力中」：技能图标持续闪烁
 * （AbilityHudOverlay 依据 isSkillHeld 画脉动描边），而且该槽位被当成正在蓄力，新的按下不会再开始
 * 新的蓄力，表现为「释放后卡在闪烁状态、无法再次释放」，只能退出重进存档恢复（重连会
 * resetClientSession 清空本地状态）。</p>
 *
 * <p>服务端存在多条「静默丢弃、不回包」的路径：受 {@code PayloadRateLimiter} 限制的拒绝反馈
 * （skill_key_down_feedback 每 20 刻只放行 1 条）、canAccept 失败时只 refresh 不回执、
 * key-down 本身被限流丢弃；客户端侧也会因握手标识（技能 id / generation）不一致而 IGNORE
 * （例如预设切换竞态）。服务端的蓄力状态通常会在 100 刻后被 serverStartExpired 回收，但客户端的
 * 槽位没有任何本地超时，于是永久卡死。</p>
 *
 * <p>本 mixin 补一个客户端看门狗：槽位进入蓄力后若连续 {@code gt$TIMEOUT_TICKS} 刻没有收到任何被
 * 接受的蓄力状态（正常蓄力每 5 刻就会收到一次），就强制复位该槽位，使 HUD 停止闪烁并允许再次释放。
 * 阈值与服务端 serverStartExpired 同量级（100 刻），远大于正常同步周期，不会误伤正常蓄力。若该槽位
 * 已拿到服务端 epoch（说明服务端很可能仍持有蓄力状态），复位前再补发一条 ChargingCancelPacket，
 * 避免只在客户端复位而把服务端的蓄力状态永久留在那里。</p>
 */
@Mixin(KeyInputHandler.class)
public class KeyInputHandlerChargingWatchdogMixin {

    @Unique
    private static final int gt$TIMEOUT_TICKS = 100;

    @Unique
    private static final int[] gt$unconfirmedTicks = new int[4];

    @Shadow
    @Final
    private static String[] CHARGING_SKILLS;

    @Shadow
    @Final
    private static long[] CHARGING_EPOCHS;

    @Inject(method = "acceptChargingState", at = @At("RETURN"))
    private static void gt$markChargingConfirmed(int slot, String skillId, long epoch, long generation, boolean accepted,
                                                 int ticks, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && slot >= 0 && slot < gt$unconfirmedTicks.length) {
            gt$unconfirmedTicks[slot] = 0;
        }
    }

    @Inject(method = "resetChargingSlot", at = @At("HEAD"))
    private static void gt$clearChargingWatchdog(int slotIndex, CallbackInfo ci) {
        if (slotIndex >= 0 && slotIndex < gt$unconfirmedTicks.length) {
            gt$unconfirmedTicks[slotIndex] = 0;
        }
    }

    @Inject(method = "onClientTick", at = @At("TAIL"))
    private static void gt$reapStuckCharging(ClientTickEvent.Post event, CallbackInfo ci) {
        for (int slot = 0; slot < gt$unconfirmedTicks.length; slot++) {
            if (!KeyInputHandler.isSkillHeld(slot)) {
                gt$unconfirmedTicks[slot] = 0;
                continue;
            }
            if (++gt$unconfirmedTicks[slot] <= gt$TIMEOUT_TICKS) {
                continue;
            }
            gt$unconfirmedTicks[slot] = 0;
            String skillId = CHARGING_SKILLS[slot];
            long epoch = CHARGING_EPOCHS[slot];
            if (epoch != 0L && skillId != null) {
                PacketDistributor.sendToServer(new ChargingCancelPacket(slot, skillId, epoch));
            }
            KeyInputHandler.resetChargingSlot(slot);
        }
    }
}

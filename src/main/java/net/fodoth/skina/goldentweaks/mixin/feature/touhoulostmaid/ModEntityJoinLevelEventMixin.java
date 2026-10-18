package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.qichensn.data.LostMaidData;
import com.github.qichensn.event.ModEntityJoinLevelEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GoldenTweaksLostMaidData;
import net.fodoth.skina.goldentweaks.compat.touhoulostmaid.AltarSummonGuard;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 祭坛召唤出来的女仆不判为迷失女仆。
 * <p>
 * 迷失模组按「无主 + 背包与装备全空」把新加入世界的女仆判为迷失，祭坛（spawn_box / reborn_maid）
 * 现场造的无主女仆正好命中。这里在 {@link AltarSummonGuard} 生效期间取消该判定，女仆保持普通状态，
 * 玩家可直接用驯服物品驯服（与本模组未安装时一致）。
 */
@Mixin(ModEntityJoinLevelEvent.class)
public class ModEntityJoinLevelEventMixin {

    @Inject(method = "markLostMaid", at = @At("HEAD"), cancellable = true, remap = false)
    private static void goldenTweaks$skipAltarSummonedMaid(EntityJoinLevelEvent event, CallbackInfo ci) {

        if (!AltarSummonGuard.isSummoning()) {
            return;
        }

        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }

        // reborn_maid 用胶片复原，胶片本身可能带着迷失状态，这里一并解除
        if (maid.getOrCreateData(LostMaidData.IS_LOST_MAID, false)) {
            maid.setData(LostMaidData.IS_LOST_MAID, false);
            maid.getOrCreateData(GoldenTweaksLostMaidData.TAMED_LOST_MAID, true);
            TaskManager.findTask(TaskIdle.UID).ifPresent(maid::setTask);
        }

        ci.cancel();
    }
}

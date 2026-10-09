package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.qichensn.data.LostMaidData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.item.AbstractStoreMaidItem;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GoldenTweaksLostMaidData;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 恢复出来的女仆物品（照片/智能板）里若是迷失女仆，放置后立即解除迷失状态。
 * <p>
 * 迷失女仆的攻击类任务会带着装备，而迷失模组的 {@code ModEntityJoinLevelEvent}
 * 只在「身上没有任何物品」时才会重判迷失状态；带装备的迷失女仆即使重新有了主人也会一直保持迷失。
 * 这里在 {@code spawnFromStore} 完成（女仆已 load 进实体）后，按迷失模组自己「驯服」的流程清理：
 * 清除 IS_LOST_MAID、标记为本模组记录的可驯服迷失女仆、并把任务重置为待机。
 */
@Mixin(AbstractStoreMaidItem.class)
public class AbstractStoreMaidItemUnLostMixin {

    @Inject(method = "spawnFromStore", at = @At("RETURN"))
    private void goldenTweaks$clearLostMaidState(UseOnContext context, Player player, Level worldIn,
                                                 EntityMaid maid, Runnable runnable,
                                                 CallbackInfoReturnable<InteractionResult> cir) {
        if (!maid.getOrCreateData(LostMaidData.IS_LOST_MAID, false)) {
            return;
        }

        maid.setData(LostMaidData.IS_LOST_MAID, false);
        maid.getOrCreateData(GoldenTweaksLostMaidData.TAMED_LOST_MAID, true);
        TaskManager.findTask(TaskIdle.UID).ifPresent(maid::setTask);
    }
}

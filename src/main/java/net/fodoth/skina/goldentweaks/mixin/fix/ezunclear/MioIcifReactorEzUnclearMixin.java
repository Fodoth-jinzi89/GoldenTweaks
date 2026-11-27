package net.fodoth.skina.goldentweaks.mixin.fix.ezunclear;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_nuclear_reactor_generator;
import net.fodoth.skina.goldentweaks.compat.ezunclear.GTEzUnclearPendingBlasts;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EzUnclear 联动：把奇点迭代核反应堆的爆炸改为「挂起 → 聊天确认后引爆」。
 *
 * <p>没装 EzUnclear 时直接放行，行为完全不变；装了才走挂起逻辑，
 * 与 EzUnclear 自己对 Mekanism 裂变堆 / 龙之进化反应堆的做法一致。</p>
 *
 * <p>{@code dispatchAccident} 在堆芯处于 PENDING 时每个 tick 都会被调用，原本靠
 * {@code claimEffect()} 一次性领取去重。这里在 HEAD 直接取消，堆芯保持 PENDING；
 * 玩家确认后只需摘掉挂起标记，下一 tick 由原逻辑自行引爆，无需反射调用私有方法。</p>
 */
@Mixin(value = mio_icif_nuclear_reactor_generator.class, remap = false)
public abstract class MioIcifReactorEzUnclearMixin {

    @Inject(method = "dispatchAccident", at = @At("HEAD"), cancellable = true)
    private void gt$deferMeltdown(CallbackInfo ci) {
        if (!GTEzUnclearPendingBlasts.isActive()) {
            return;
        }

        mio_icif_nuclear_reactor_generator self = (mio_icif_nuclear_reactor_generator) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel server)) {
            return;
        }

        BlockPos pos = self.getBlockPos();
        if (GTEzUnclearPendingBlasts.defer(server, pos)) {
            ci.cancel();
        }
    }
}

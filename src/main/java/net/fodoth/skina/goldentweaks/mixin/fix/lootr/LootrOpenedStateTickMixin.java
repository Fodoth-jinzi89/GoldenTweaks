package net.fodoth.skina.goldentweaks.mixin.fix.lootr;

import net.fodoth.skina.goldentweaks.compat.lootr.LootrOpenedStateAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 Lootr 的 {@code ILootrBlockEntity#defaultTick} 末尾，把 GoldenTweaks 保存的
 * “已开启”状态强制同步回 Lootr。
 * <p>
 * Lootr 的部分容器会在 tick 中重置 hasBeenOpened，导致快速拾取后容器状态异常。
 * 该 Mixin 挂在接口上，会对所有 Lootr 方块实体生效，因此必须先用
 * {@code instanceof LootrOpenedStateAccess} 过滤：只有被 {@link LootrOpenedStateMixin}
 * 注入过的容器（箱子、桶、潜影盒）才做同步，其它类型（装饰陶罐、刷子方块等）直接跳过，
 * 避免 ClassCastException。
 */
@Mixin(ILootrBlockEntity.class)
public interface LootrOpenedStateTickMixin {

    @Inject(method = "defaultTick", at = @At("TAIL"))
    private void gt$forceState(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof LootrOpenedStateAccess access) {
            ((ILootrBlockEntity) this).setHasBeenOpened(access.gt$isOpened());
        }
    }
}

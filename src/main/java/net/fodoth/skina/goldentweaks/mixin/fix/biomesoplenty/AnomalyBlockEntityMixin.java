package net.fodoth.skina.goldentweaks.mixin.fix.biomesoplenty;

import biomesoplenty.block.entity.AnomalyBlockEntity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * BOP 的「异常方块」（错误方块）在渲染时不断变幻成各种方块模型：原版实现从全部已注册
 * 方块（含其它模组）中取候选，且变幻速度由游戏时间驱动，VOLATILE / UNSTABLE 几乎每 tick 一变。
 * 开启「优化错误方块」后：只在原版方块中随机，且每 10 秒（200 tick）才换一次。
 */
@Mixin(value = AnomalyBlockEntity.class, remap = false)
public class AnomalyBlockEntityMixin {

    @Unique
    private static final long gt$INTERVAL_TICKS = 200L;

    @Unique
    private static List<BlockState> gt$vanillaRenderStatesCache;

    @Unique
    private long gt$lastPeriod = Long.MIN_VALUE;

    @Unique
    private BlockState gt$lastState;

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$optimizeRenderState(CallbackInfoReturnable<BlockState> cir) {
        if (!GoldenTweaksClientConfig.isOptimizeErrorBlockEnabled()) {
            return;
        }

        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
            return;
        }

        long period = level.getGameTime() / gt$INTERVAL_TICKS;
        if (period == this.gt$lastPeriod && this.gt$lastState != null) {
            cir.setReturnValue(this.gt$lastState);
            return;
        }

        List<BlockState> states = gt$collectVanillaRenderStates();
        if (states.isEmpty()) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
            return;
        }

        BlockPos pos = self.getBlockPos();
        RandomSource random = RandomSource.create(pos.asLong() + period);
        BlockState state = states.get(random.nextInt(states.size()));
        this.gt$lastState = state;
        this.gt$lastPeriod = period;
        cir.setReturnValue(state);
    }

    @Unique
    private static List<BlockState> gt$collectVanillaRenderStates() {
        List<BlockState> cached = gt$vanillaRenderStatesCache;
        if (cached != null) {
            return cached;
        }

        List<BlockState> built = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!"minecraft".equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) {
                continue;
            }

            BlockState state = block.defaultBlockState();
            if (state.getRenderShape() == RenderShape.MODEL) {
                built.add(state);
            }
        }

        gt$vanillaRenderStatesCache = built;
        return built;
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.flavorimmerseddailyargo;

import com.miophas.singularity_iteration.common.block.crop.enriched.UraniumRichCrop;
import com.miophas.singularity_iteration.common.effect.mio_icif_effects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 田里的老鼠（{@code flavor_immersed_daily_argo:mouse}）会啃食成熟作物。奇点迭代的「富铀作物」
 * （{@link UraniumRichCrop}）被啃后，理应让老鼠沾染辐射——这里在 {@code Mouse#startEating} 正常结束时
 * 判断被吃掉的方块，若是富铀作物就给老鼠挂上 3 分钟的 {@code mio_icif:radiation}。
 *
 * <p>FID Argo 不在编译期依赖里，因此用 {@code targets} 字符串指定混入目标，处理代码里只引用原版的
 * {@link LivingEntity}（老鼠是 Animal）。选择 {@code TAIL}（成功路径的最后一个 return）而不是
 * {@code HEAD}，可避免未成熟/非服务端提前 return 的分支误判；此时方块已回退一级年龄，但仍是同一个
 * 富铀作物方块。</p>
 */
@Mixin(targets = "com.flavor_immersed_daily_argo.entity.Mouse", remap = false)
public abstract class MouseUraniumRadiationMixin {

    @Inject(method = "startEating", at = @At("TAIL"))
    private void gt$radiateAfterEatingUraniumCrop(BlockPos pos, CallbackInfo ci) {
        LivingEntity mouse = (LivingEntity) (Object) this;
        Level level = mouse.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(serverLevel.getBlockState(pos).getBlock() instanceof UraniumRichCrop)) {
            return;
        }
        mouse.addEffect(new MobEffectInstance(mio_icif_effects.RADIATION, 3 * 60 * 20, 0));
    }
}

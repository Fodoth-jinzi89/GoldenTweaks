package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTGolemSimplify;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.common.entities.golem.ThaumcraftGolemEntity;

/**
 * 神秘傀儡简化模式（见 {@link GTGolemSimplify}）的实体侧处理。
 *
 * <p>取消寻路本身在 {@code GolemPathingMixin}（{@code GolemPathing.moveToward}）里完成，
 * 这里只负责让傀儡在简化模式下留在原点。</p>
 */
@Mixin(value = ThaumcraftGolemEntity.class, remap = false)
public abstract class ThaumcraftGolemEntityMixin {

    /**
     * 简化模式下傀儡应当站在原点：被活塞/爆炸等推离原点时直接拉回。
     * 原点就是放下傀儡的那一格（必定是可替换方块），因此直接传送是安全的。
     * 正在移动（例如躲避苦力怕）时不打断，等它停下来再拉回。
     */
    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void gt$snapBackHome(CallbackInfo ci) {
        ThaumcraftGolemEntity golem = (ThaumcraftGolemEntity) (Object) this;
        if (!golem.getNavigation().isDone()) {
            return;
        }

        BlockPos home = GTGolemSimplify.homeSnapTarget(golem);
        if (home == null) {
            return;
        }

        golem.teleportTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        golem.getNavigation().stop();
    }
}

package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GoldenTweaksLostMaidData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityMaid.class)
public class EntityMaidDieMixin {

    @Shadow
    private void sendMaidPos() {}

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;sendMaidPos()V"
            )
    )
    private void goldenTweaks$cancelSendMaidPos(EntityMaid maid) {

        // 迷失女仆不发送坐标
        if (maid.getOrCreateData(GoldenTweaksLostMaidData.TAMED_LOST_MAID, false)) {
            return;
        }

        // 正常情况执行原方法
        this.sendMaidPos();
    }
}

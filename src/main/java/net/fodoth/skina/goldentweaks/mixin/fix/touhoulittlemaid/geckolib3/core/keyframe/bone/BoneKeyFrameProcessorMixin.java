package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid.geckolib3.core.keyframe.bone;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = BoneKeyFrameProcessor.class, remap = false)
public abstract class BoneKeyFrameProcessorMixin {

    @ModifyArg(method = "process", at = @At(value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/ReferenceArrayList;wrap([Ljava/lang/Object;)Lit/unimi/dsi/fastutil/objects/ReferenceArrayList;"), index = 0)
    private static Object[] gt$shareEmptyFrames(Object[] frames) {
        return frames instanceof BoneKeyFrame[] keyFrames ? GTAnimationConstants.reuseEmptyFrames(keyFrames) : frames;
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid.geckolib3.core.keyframe.bone;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.EasingType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.LinearKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.CatmullRomKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.TransitionKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.Vector3v;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EasingType.class, remap = false)
public interface EasingTypeMixin {

    @WrapOperation(method = "buildCatmullRomKeyFrame", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/CatmullRomKeyFrame"), require = 1)
    private static CatmullRomKeyFrame gt$reuseCatmull(double start, double duration, Vector3v left, Vector3v begin,
                                                    Vector3v end, Vector3v right, Vector3v post,
                                                    Operation<CatmullRomKeyFrame> original) {
        return GTAnimationConstants.reuseCatmull(start, duration, left, begin, end, right, post, original);
    }

    @WrapOperation(method = "buildLinearKeyFrame", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/LinearKeyFrame"), require = 1)
    private static LinearKeyFrame gt$reuseLinear(double start, double duration, Vector3v begin, Vector3v end,
                                                Vector3v post, Operation<LinearKeyFrame> original) {
        return GTAnimationConstants.reuseLinear(start, duration, begin, end, post, original);
    }

    @WrapOperation(method = "buildTransitionKeyFrame", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/TransitionKeyFrame"), require = 1)
    private static TransitionKeyFrame gt$reuseTransition(double duration, Vector3v begin, Vector3v post,
                                                        Operation<TransitionKeyFrame> original) {
        return GTAnimationConstants.reuseTransition(duration, begin, post, original);
    }
}

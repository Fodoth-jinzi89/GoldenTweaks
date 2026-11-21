package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid.geckolib3.core.keyframe.bone;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.RawBoneKeyFrame;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.keyframe.bone.Vector3v;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.DoubleValue;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.IValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RawBoneKeyFrame.class, remap = false)
public abstract class RawBoneKeyFrameMixin {

    @WrapOperation(method = "getValue", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/molang/value/DoubleValue"), require = 2)
    private DoubleValue gt$reuseConstant(double value, Operation<DoubleValue> original) {
        return GTAnimationConstants.reuse(value, original);
    }

    @WrapOperation(method = "init", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/keyframe/bone/Vector3v"), require = 2)
    private Vector3v gt$reuseVector(IValue x, IValue y, IValue z, Operation<Vector3v> original) {
        return GTAnimationConstants.reuseVector(x, y, z, original);
    }
}

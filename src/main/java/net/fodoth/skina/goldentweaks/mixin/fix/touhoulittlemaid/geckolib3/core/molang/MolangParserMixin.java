package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid.geckolib3.core.molang;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.MolangParser;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.value.DoubleValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GTAnimationConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MolangParser.class, remap = false)
public abstract class MolangParserMixin {

    @WrapOperation(method = "getConstant", at = @At(value = "NEW",
            target = "com/github/tartaricacid/touhoulittlemaid/geckolib3/core/molang/value/DoubleValue"))
    private DoubleValue gt$reuseConstant(double value, Operation<DoubleValue> original) {
        return GTAnimationConstants.reuse(value, original);
    }
}

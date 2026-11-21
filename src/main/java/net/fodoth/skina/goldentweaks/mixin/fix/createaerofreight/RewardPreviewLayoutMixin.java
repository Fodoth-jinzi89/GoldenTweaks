package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.client.ui.RewardPreviewLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RewardPreviewLayout.class, remap = false)
public abstract class RewardPreviewLayoutMixin {
    @ModifyVariable(method = "inline", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private static int gt$sixIcons(int previewCount) {
        return Math.min(6, previewCount);
    }
}

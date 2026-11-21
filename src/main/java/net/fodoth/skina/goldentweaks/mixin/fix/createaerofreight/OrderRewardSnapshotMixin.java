package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.OrderRewardSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = OrderRewardSnapshot.class, remap = false)
public abstract class OrderRewardSnapshotMixin {
    @ModifyConstant(method = {"inline", "readSummary"}, constant = @Constant(intValue = 8))
    private static int gt$fullPreview(int original) {
        return 64;
    }
}

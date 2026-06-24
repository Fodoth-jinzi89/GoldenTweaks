package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.entity.SteamboxBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SteamboxBlockEntity.class)
public class SteamboxBlockEntityMixin {

    // 添加一个线程局部变量来防止递归
    @Unique
    private static final ThreadLocal<Boolean> goldenTweaks$isProcessing = ThreadLocal.withInitial(() -> false);

    /**
     * =========================================================
     * 从 bottom → top 顺序填充输入
     * =========================================================
     */
    @Inject(
            method = "addItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private void goldenTweaks$bottomToTopFill(ItemStack held, CallbackInfoReturnable<Integer> cir) {
        //  防止递归：如果已经在处理中，直接返回，让原方法执行
        if (goldenTweaks$isProcessing.get()) {
            return;
        }

        SteamboxBlockEntity self = (SteamboxBlockEntity)(Object)this;

        Level level = self.getLevel();
        if (level == null || level.isClientSide) return;

        //  设置处理标志
        goldenTweaks$isProcessing.set(true);

        try {
            int remaining = held.getCount();
            int addedTotal = 0;

            // ✔ 最多 16 层（与你 stack 规则一致）
            for (int layer = 0; layer < 16 && remaining > 0; layer++) {
                SteamboxBlockEntity layerBe = getLayer(self, layer);
                if (layerBe == null) continue;

                int before = remaining;
                // ✔ 每层尽量填满
                remaining -= fillLayer(layerBe, held, remaining);
                addedTotal += (before - remaining);
            }

            if (addedTotal > 0) {
                held.shrink(addedTotal);
                cir.setReturnValue(addedTotal);
            } else {
                cir.setReturnValue(0);
            }
        } finally {
            //  确保清理标志
            goldenTweaks$isProcessing.set(false);
        }
    }

    /**
     * 填充单层（尽量填满 4 slot 容量）
     */
    @Unique
    private int fillLayer(SteamboxBlockEntity be, ItemStack held, int amount) {
        if (!be.canAcceptItem(held)) return 0;

        // 注意：这里调用 be.addItems() 时，由于 goldenTweaks$isProcessing 为 true，
        // 当前 Mixin 的 @Inject 方法会直接 return，不会再次进入递归
        ItemStack to = held.copy();
        to.setCount(amount);
        return be.addItems(to);  // 现在安全了，不会被Mixin拦截导致递归
    }

    /**
     * 获取某一层 BE（bottom = 0）
     */
    @Unique
    private SteamboxBlockEntity getLayer(SteamboxBlockEntity bottom, int index) {
        var level = bottom.getLevel();
        if (level == null) return null;

        var pos = bottom.getBlockPos().above(index);
        var be = level.getBlockEntity(pos);
        if (be instanceof SteamboxBlockEntity steambox) {
            return steambox;
        }
        return null;
    }
}
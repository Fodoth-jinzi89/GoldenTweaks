package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FamiliarConsumableSystem.ConsumableType.class)
public class ConsumableTypeMixin {

    @Inject(method = "getMaxTier", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$getMaxTier(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(10);
    }

    @Inject(method = "getTierBonus", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$getTierBonus(int tier, CallbackInfoReturnable<Integer> cir) {

        FamiliarConsumableSystem.ConsumableType self =
                (FamiliarConsumableSystem.ConsumableType) (Object) this;

        tier = Math.max(1, tier);

        switch (self) {

            case ARMOR -> {
                int[] values = {2, 3, 4, 8, 16, 32, 128, 512, 2048, 16384};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case HEALTH -> {
                int[] values = {4, 6, 8, 32, 64, 128, 512, 2048, 8192, 65536};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case SPELL_POWER -> {
                int[] values = {3, 6, 9, 18, 36, 72, 288, 1152, 4608, 36864};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case SPELL_RESIST -> {
                int[] values = {2, 2, 2, 5, 5, 5, 10, 10, 10, 10};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case ENRAGED -> {
                int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case BLOCKING -> {
                int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 11};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }
        }
    }

    @Inject(method = "getTierLimit", at = @At("HEAD"), cancellable = true)
    private void goldentweaks$getTierLimit(int tier, CallbackInfoReturnable<Integer> cir) {

        FamiliarConsumableSystem.ConsumableType self =
                (FamiliarConsumableSystem.ConsumableType) (Object) this;

        tier = Math.max(1, tier);

        switch (self) {

            case ARMOR -> {
                int[] values = {10, 16, 20, 40, 80, 160, 640, 2560, 10240, Integer.MAX_VALUE};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case HEALTH -> {
                int[] values = {20, 32, 40, 80, 160, 640, 2560, 10240, 40960, Integer.MAX_VALUE};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case SPELL_POWER -> {
                int[] values = {15, 30, 45, 90, 180, 360, 1440, 5760, 23040, Integer.MAX_VALUE};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case SPELL_RESIST -> {
                int[] values = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case ENRAGED -> {
                int[] values = {10, 20, 30, 40, 50, 64, 80, 100, 120, 150};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }

            case BLOCKING -> {
                int[] values = {1, 2, 3, 4, 5, 6, 8, 10, 12, 16};
                cir.setReturnValue(values[Math.min(values.length - 1, tier - 1)]);
            }
        }
    }
}

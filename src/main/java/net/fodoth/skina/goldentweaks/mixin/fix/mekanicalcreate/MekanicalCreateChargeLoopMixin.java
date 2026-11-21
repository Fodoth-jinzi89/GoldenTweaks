package net.fodoth.skina.goldentweaks.mixin.fix.mekanicalcreate;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code SimulationRecipeResolver#energyChargePlan} 在充电速率取不到时会退化成 1 FE/t，
 * 然后逐 tick 循环（上限 100 万次）调用 {@code receiveEnergy}，只为算出一个展示用时长。
 * 改成把剩余容量一次性充进去：第一步就充满，第二次调用返回 0 后循环自然结束。
 */
@Mixin(targets = "io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver", remap = false)
public class MekanicalCreateChargeLoopMixin {

    @Redirect(method = "energyChargePlan",
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/energy/IEnergyStorage;receiveEnergy(IZ)I",
                    ordinal = 1),
            require = 0)
    private static int gt$bulkReceive(IEnergyStorage storage, int energy, boolean simulate) {

        if (simulate) {
            return storage.receiveEnergy(energy, true);
        }

        long missing = (long) storage.getMaxEnergyStored() - storage.getEnergyStored();
        int amount = (int) Math.min(Integer.MAX_VALUE, Math.max(energy, missing));

        return storage.receiveEnergy(amount, false);
    }
}

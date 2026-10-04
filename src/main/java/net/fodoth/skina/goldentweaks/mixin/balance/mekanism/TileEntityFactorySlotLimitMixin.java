package net.fodoth.skina.goldentweaks.mixin.balance.mekanism;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import net.fodoth.skina.goldentweaks.compat.mekanism.IGTFactorySlotLimit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

/**
 * evolvedmekanism 的高级工厂（超频/量子/致密/寰宇/创造）直接复用原版工厂槽位，
 * 槽位堆叠上限被限制在物品默认值（64），而 mekanism_extras 的工厂槽位按 8 &lt;&lt; 等级 放大。
 * 这里按同样的规则放大超频及以上的工厂输入/输出槽容量，使两者一致。
 * <p>
 * mekanism_extras 的工厂走自己的 {@code TileEntityExtraFactory}（不继承本类），其槽位已自带放大，不受影响。
 */
@Mixin(value = TileEntityFactory.class, remap = false)
public abstract class TileEntityFactorySlotLimitMixin {

    @Shadow(remap = false)
    public FactoryTier tier;

    @Shadow(remap = false)
    @Final
    protected List<IInventorySlot> inputSlots;

    @Shadow(remap = false)
    @Final
    protected List<IInventorySlot> outputSlots;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void goldentweaks$oversizeEvolvedFactorySlots(Holder<Block> blockProvider, BlockPos pos, BlockState state,
                                                          List<?> errorTypes, Set<?> globalErrorTypes, CallbackInfo ci) {
        if (tier == null) {
            return;
        }
        int step = tier.ordinal() - FactoryTier.ULTIMATE.ordinal();
        if (step <= 0) {
            // 原版基础等级维持原样
            return;
        }
        try {
            int multiplier = 8 << (step - 1);
            goldentweaks$applyMultiplier(inputSlots, multiplier);
            goldentweaks$applyMultiplier(outputSlots, multiplier);
        } catch (Exception ignored) {
        }
    }

    @Unique
    private static void goldentweaks$applyMultiplier(List<IInventorySlot> slots, int multiplier) {
        if (slots == null) {
            return;
        }
        for (IInventorySlot slot : slots) {
            if (slot instanceof IGTFactorySlotLimit limit) {
                limit.goldentweaks$setStackLimitMultiplier(multiplier);
            }
        }
    }
}

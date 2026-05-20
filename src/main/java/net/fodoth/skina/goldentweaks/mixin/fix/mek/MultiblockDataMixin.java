package net.fodoth.skina.goldentweaks.mixin.fix.mek;

import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * 修复 MultiblockData 在 Level 未绑定时的 NPE
 */
@Mixin(MultiblockData.class)
public abstract class MultiblockDataMixin {

    @Mutable
    @Shadow
    @Final
    private BooleanSupplier remoteSupplier;

    @Mutable
    @Shadow
    @Final
    private Supplier<Level> worldSupplier;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void fixConstructor(BlockEntity tile, CallbackInfo ci) {

        // 延迟替换 remoteSupplier
        this.remoteSupplier = () -> {
            if (tile.getLevel() == null) {
                return false;
            }
            return tile.getLevel().isClientSide();
        };

        // 延迟 worldSupplier 安全化
        this.worldSupplier = tile::getLevel;
    }
}

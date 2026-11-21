package net.fodoth.skina.goldentweaks.mixin.fix.kaleidoscope;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FruitBasketBlockEntity.class)
public abstract class FruitBasketBlockEntityMixin extends BlockEntity {

    protected FruitBasketBlockEntityMixin(BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = { "putOn", "takeOut" }, at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$onlyMutateInventoryOnServer(CallbackInfo ci) {
        // Carry On cancels the server interaction, but the client still predicts taking an item.
        if (level != null && level.isClientSide) {
            ci.cancel();
        }
    }
}

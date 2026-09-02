package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import thaumcraft.common.blockentities.ThaumatoriumBlockEntity;
import thaumcraft.common.blocks.ThaumatoriumBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ThaumatoriumBlockEntity.class, remap = false)
public abstract class ThaumatoriumOutputMixin {
    @Inject(method = "spawnOutput", at = @At("HEAD"), cancellable = true)
    private void gt$insertIntoContainer(Level level, BlockPos pos, BlockState state, ItemStack output, CallbackInfo ci) {
        Direction facing = state.getValue(ThaumatoriumBlock.FACING);
        if (level.getBlockEntity(pos.relative(facing)) instanceof Container container) {
            int capacity = 0;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack existing = container.getItem(slot);
                if (!container.canPlaceItem(slot, output)) {
                    continue;
                }
                if (existing.isEmpty()) {
                    capacity += output.getMaxStackSize();
                } else if (ItemStack.isSameItemSameComponents(existing, output)) {
                    capacity += existing.getMaxStackSize() - existing.getCount();
                }
            }
            if (capacity >= output.getCount()) {
                ItemStack remaining = output.copy();
                for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
                    ItemStack existing = container.getItem(slot);
                    if (!container.canPlaceItem(slot, remaining)) continue;
                    if (existing.isEmpty()) {
                        int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                        container.setItem(slot, remaining.copyWithCount(moved));
                        remaining.shrink(moved);
                    } else if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                        int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                        if (moved > 0) {
                            existing.grow(moved);
                            remaining.shrink(moved);
                            container.setItem(slot, existing);
                        }
                    }
                }
                container.setChanged();
                ci.cancel();
            }
        }
    }
}

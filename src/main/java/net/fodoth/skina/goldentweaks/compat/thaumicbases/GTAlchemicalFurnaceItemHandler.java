package net.fodoth.skina.goldentweaks.compat.thaumicbases;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.common.blockentities.AlchemicalFurnaceBlockEntity;

/**
 * Face-aware {@link IItemHandler} view of an alchemical furnace. Registered for Thaumic Bases' advanced furnace
 * because Thaumcraft only registers the capability for its own block entity type - the upgraded furnace inherited
 * the container logic but had no item handler, so pipes, hoppers and other logistics could not reach it at all.
 *
 * <p>Face behaviour mirrors Thaumcraft's {@code WorldlyContainer} mapping (bottom = fuel, sides = input, top =
 * nothing), with one addition controlled by {@code balance.thaumicbases.furnace_fuel_first}: the exposed slots are
 * ordered so the <b>fuel slot is offered first</b>. Coal and friends have aspects too, so a plain
 * "first acceptable slot" insert used to drop fuel into the input slot; now anything that can burn lands in the
 * fuel slot and everything else falls through to the input slot. Turning the option off restores Thaumcraft's
 * original slot order.
 */
public final class GTAlchemicalFurnaceItemHandler implements IItemHandler {

    private final AlchemicalFurnaceBlockEntity furnace;
    private final Direction side;

    public GTAlchemicalFurnaceItemHandler(AlchemicalFurnaceBlockEntity furnace, Direction side) {
        this.furnace = furnace;
        this.side = side;
    }

    @Override
    public int getSlots() {
        return this.side == Direction.UP ? 0 : 2;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        int containerSlot = this.containerSlot(slot);
        return containerSlot < 0 ? ItemStack.EMPTY : this.furnace.getItem(containerSlot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.furnace.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        int containerSlot = this.containerSlot(slot);
        return containerSlot >= 0 && this.furnace.canPlaceItem(containerSlot, stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int containerSlot = this.containerSlot(slot);
        if (stack.isEmpty() || containerSlot < 0) {
            return stack;
        }
        return insert(this.furnace, containerSlot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        int containerSlot = this.containerSlot(slot);
        if (containerSlot < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.furnace.getItem(containerSlot);
        if (existing.isEmpty() || amount > existing.getMaxStackSize()) {
            return ItemStack.EMPTY;
        }
        // Same quirk as Thaumcraft#canTakeItemThroughFace: from below the fuel slot only gives back empty buckets.
        if (this.side == Direction.DOWN
                && containerSlot == AlchemicalFurnaceBlockEntity.FUEL_SLOT
                && !existing.is(Items.BUCKET)) {
            return ItemStack.EMPTY;
        }

        if (simulate) {
            return existing.copyWithCount(Math.min(amount, existing.getCount()));
        }
        return this.furnace.removeItem(containerSlot, amount);
    }

    /** Fuel first, then input - so a single face can feed both without fuel ending up in the input slot. */
    private int containerSlot(int handlerSlot) {
        boolean fuelFirst = GoldenTweaksCommonConfig.isFurnaceFuelFirst();
        if (handlerSlot == 0) {
            return fuelFirst ? AlchemicalFurnaceBlockEntity.FUEL_SLOT : AlchemicalFurnaceBlockEntity.INPUT_SLOT;
        }
        if (handlerSlot == 1) {
            return fuelFirst ? AlchemicalFurnaceBlockEntity.INPUT_SLOT : AlchemicalFurnaceBlockEntity.FUEL_SLOT;
        }
        return -1;
    }

    private static ItemStack insert(AlchemicalFurnaceBlockEntity furnace, int containerSlot, ItemStack stack, boolean simulate) {
        if (!furnace.canPlaceItem(containerSlot, stack)) {
            return stack;
        }

        ItemStack existing = furnace.getItem(containerSlot);
        int limit = Math.min(furnace.getMaxStackSize(), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) {
                return stack;
            }
            limit -= existing.getCount();
        }
        if (limit <= 0) {
            return stack;
        }

        int moved = Math.min(stack.getCount(), limit);
        if (!simulate) {
            if (existing.isEmpty()) {
                furnace.setItem(containerSlot, stack.copyWithCount(moved));
            } else {
                furnace.setItem(containerSlot, existing.copyWithCount(existing.getCount() + moved));
            }
            furnace.setChangedAndSync();
        }
        return stack.copyWithCount(stack.getCount() - moved);
    }
}

package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class PaddedStorageContainer implements Container {

    private final IItemHandlerModifiable handler;
    private final int size;
    private final Predicate<Player> stillValid;
    private final Consumer<Player> onClose;

    public PaddedStorageContainer(IItemHandlerModifiable handler, int size,
                                  Predicate<Player> stillValid, Consumer<Player> onClose) {
        this.handler = handler;
        this.size = size;
        this.stillValid = stillValid;
        this.onClose = onClose;
    }

    private boolean isRealSlot(int slot) {
        return slot >= 0 && slot < handler.getSlots();
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return isRealSlot(slot) ? handler.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return isRealSlot(slot) ? handler.extractItem(slot, amount, false) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (!isRealSlot(slot)) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = handler.getStackInSlot(slot);
        handler.setStackInSlot(slot, ItemStack.EMPTY);
        return existing;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (isRealSlot(slot)) {
            handler.setStackInSlot(slot, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid.test(player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isRealSlot(slot) && handler.isItemValid(slot, stack);
    }

    @Override
    public void stopOpen(Player player) {
        onClose.accept(player);
    }
}

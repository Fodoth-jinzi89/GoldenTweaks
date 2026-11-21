import io

# ---------------- 1. the port exposes its contents as a Container ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''public class GTPortBlockEntity extends BlockEntity implements MenuProvider, Nameable {''',
'''public class GTPortBlockEntity extends BlockEntity
        implements MenuProvider, Nameable, Container {''', 1)

s = s.replace('''    /** The item handler for an item port, or {@code null} for any other kind. */''',
'''    // ---- Container view, so Containers.dropContentsOnDestroy can return the contents ----
    //
    // The machine does the same thing for its controller slot, and for the same reason: that helper
    // is the one that reads a tile's container and drops it, guarding on the block actually
    // changing. Exposing it here means a broken port spills what it held, and the packing into
    // matter clusters happens in the drop path itself rather than being reinvented per block.

    @Override
    public int getContainerSize() {
        return storageSlots();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (this.items != null) {
            return slot < this.items.getSlots() ? this.items.getStackInSlot(slot) : ItemStack.EMPTY;
        }
        // Fluids have no item form; the drop path reads them through the fluid view below.
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.items != null && slot < this.items.getSlots()) {
            this.items.insertItem(slot, stack, false);
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.items == null || slot >= this.items.getSlots()) {
            return ItemStack.EMPTY;
        }
        return this.items.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, Integer.MAX_VALUE);
    }

    @Override
    public void clearContent() {
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                this.items.extractItem(slot, Integer.MAX_VALUE, false);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        if (this.items != null) {
            for (int slot = 0; slot < this.items.getSlots(); slot++) {
                if (!this.items.getStackInSlot(slot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** How many slots the container view exposes: item slots, or none for the other kinds. */
    private int storageSlots() {
        return this.items == null ? 0 : this.items.getSlots();
    }

    /** The item handler for an item port, or {@code null} for any other kind. */''', 1)

# imports
for imp, anchor in [
    ('import net.minecraft.world.Container;', 'import net.minecraft.nbt.CompoundTag;'),
    ('import net.minecraft.world.entity.player.Player;', 'import net.minecraft.world.entity.player.Player;'),
]:
    if imp not in s:
        s = s.replace(anchor, imp + '\n' + anchor, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('entity:', 'changed' if s != orig else 'NO CHANGE')

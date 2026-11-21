import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * Drops the port as an item, carrying the name the player gave it.''',
'''    /**
     * Spills the port's contents the way the machine spills its controller slot.
     *
     * <p>{@code Containers.dropContentsOnDestroy} is the same helper the controller uses, and it is
     * the right one for the same reasons: it guards on the block actually changing, so a state
     * update drops nothing, and it reads the tile's container view instead of iterating anything by
     * hand.</p>
     *
     * <p>Fluids have no container view, so they are packed here. Energy is deliberately dropped —
     * a broken hatch comes back as the hatch alone, since charge has no item to travel in.</p>
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                         boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof GTPortBlockEntity port) {
            port.spillFluids();
        }
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Drops the port as an item, carrying the name the player gave it.''', 1)

for imp in ['import net.minecraft.world.Containers;']:
    if imp not in s:
        s = s.replace('import net.minecraft.world.InteractionResult;',
                      imp + '\nimport net.minecraft.world.InteractionResult;', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('block:', 'changed' if s != orig else 'NO CHANGE')

# ---------------- the fluid spill on the entity ----------------
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
orig2 = s2

s2 = s2.replace('''    /** How many slots the container view exposes: item slots, or none for the other kinds. */''',
'''    /**
     * Drops whatever the tanks hold, packed into fluid clusters.
     *
     * <p>Called from the block's removal, because a fluid buffer is not a {@code Container} and the
     * vanilla helper cannot see it. Packed rather than poured out stack by stack: the same reason
     * the machine packs its parts.</p>
     */
    public void spillFluids() {
        if (this.level == null || this.level.isClientSide || this.fluids == null) {
            return;
        }
        java.util.List<FluidStack> held = new java.util.ArrayList<>();
        for (int tank = 0; tank < this.fluids.getTanks(); tank++) {
            FluidStack stack = this.fluids.getFluidInTank(tank);
            if (stack.isEmpty()) {
                continue;
            }
            FluidStack drained = this.fluids.drain(stack, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                held.add(drained);
            }
        }
        if (held.isEmpty()) {
            return;
        }
        java.util.List<ItemStack> clusters = net.weibai.avaritia_more_items.common.items.matter_cluster
                .FluidMatterClusterItem.makeClusters(held);
        for (ItemStack cluster : clusters) {
            Block.popResource(this.level, this.worldPosition, cluster);
        }
    }

    /** How many slots the container view exposes: item slots, or none for the other kinds. */''', 1)

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('entity:', 'changed' if s2 != orig2 else 'NO CHANGE')

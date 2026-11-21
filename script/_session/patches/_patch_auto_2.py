import io

# ---------------- block: register a ticker ----------------
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlock.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    @Nullable
    @Override
    public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        return new GTPortBlockEntity(pos, state);
    }''',
'''    @Nullable
    @Override
    public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
        return new GTPortBlockEntity(pos, state);
    }

    /**
     * The port ticks so an active one can reach through its face.
     *
     * <p>Only the server side matters: the reach-through moves real contents, and a client copy
     * doing it as well would double every transfer.</p>
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != GTMultiblockBlocks.PORT_ENTITY.get()) {
            return null;
        }
        return (tickingLevel, pos, tickingState, blockEntity) -> {
            if (blockEntity instanceof GTPortBlockEntity port) {
                port.serverTick();
            }
        };
    }''', 1)

for imp in ['import net.minecraft.world.level.block.entity.BlockEntityTicker;',
            'import net.minecraft.world.level.block.entity.BlockEntityType;']:
    if imp not in s:
        s = s.replace('import net.minecraft.world.level.block.entity.BlockEntity;',
                      imp + '\nimport net.minecraft.world.level.block.entity.BlockEntity;', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('block:', 'changed' if s != orig else 'NO CHANGE')

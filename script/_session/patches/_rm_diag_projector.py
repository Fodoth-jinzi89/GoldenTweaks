"""Remove the projector diagnostic now that the cause is known."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockProjectionClient.java"
s = io.open(p, encoding="utf-8").read()

old = '''        BlockState state = event.getLevel().getBlockState(event.getPos());
        ResourceLocation diagId = GTMultiblockBlocks.structureForHost(state);
        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] projector right-click on {} -> structureId={} layoutPresent={}",
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                diagId, diagId != null && GTProjectionStructures.INSTANCE.layout(diagId) != null);
        if (diagId == null) {
            return;
        }'''
new = '''        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (GTMultiblockBlocks.structureForHost(state) == null) {
            return;
        }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("diagnostic removed")

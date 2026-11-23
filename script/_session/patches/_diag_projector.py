"""Temporary: print what the projector resolves for each host.

In-game report is "only the basic one previews", so the question is which link in
block -> structureId -> layout is returning nothing. This prints all three.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTMultiblockProjectionClient.java"
s = io.open(p, encoding="utf-8").read()

old = '''        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (GTMultiblockBlocks.structureForHost(state) == null) {
            return;
        }'''
new = '''        BlockState state = event.getLevel().getBlockState(event.getPos());
        ResourceLocation diagId = GTMultiblockBlocks.structureForHost(state);
        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] projector right-click on {} -> structureId={} layoutPresent={}",
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                diagId, diagId != null && GTProjectionStructures.INSTANCE.layout(diagId) != null);
        if (diagId == null) {
            return;
        }'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("projector diagnostic added")

"""Temporary: report what the port renderer actually receives."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortCasingModel.java"
s = io.open(p, encoding="utf-8").read()

old = '''        BlockState casing = data.get(CASING);
        if (casing == null) {'''
new = '''        BlockState casing = data.get(CASING);
        if (side == Direction.NORTH) {
            net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                    "[GT/diag] port quads: state={} casing={} quads={} total={}",
                    state == null ? "null" : state.getBlock(), casing,
                    quads.size(), quads.size());
        }
        if (casing == null) {'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("diagnostic added")

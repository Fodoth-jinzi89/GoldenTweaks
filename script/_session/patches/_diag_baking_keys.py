"""Print what the baking keys actually look like, so the match can be written against reality.

The previous attempts guessed at the key shape twice — `gt_port_` and `contains("/gt_port_")` — and
both missed. This prints the real ids for anything containing `gt_port`, which settles it.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/client/GTPortModelWrapping.java"
s = io.open(p, encoding="utf-8").read()

old = '''        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] baking hook: {} models total, {} wrapped as ports",
                models.size(), wrapped.size());'''
new = '''        java.util.List<String> samples = new java.util.ArrayList<>();
        for (ModelResourceLocation id : models.keySet()) {
            if (id.toString().contains("gt_port")) {
                samples.add(id.toString());
                if (samples.size() >= 5) {
                    break;
                }
            }
        }
        // Also show a few keys verbatim, in case the ids do not contain the name at all.
        java.util.List<String> anyKeys = models.keySet().stream()
                .limit(3).map(Object::toString).toList();

        net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER.info(
                "[GT/diag] baking hook: {} models total, {} wrapped as ports, keys={}, rawKeys={}",
                models.size(), wrapped.size(), samples, anyKeys);'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("baking key diagnostic widened")

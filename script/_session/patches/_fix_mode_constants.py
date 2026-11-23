"""Replace the constant block by line range.

The old comment contains two mangled UTF-8 sequences, so an anchor-based edit cannot match
it. Rewriting lines 132..152 (1-based) avoids guessing at those bytes at all.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
with io.open(p, encoding="utf-8") as handle:
    lines = handle.read().split("\n")

# 1-based 132..152 inclusive -> 0-based 131..151
start, end = 131, 151
assert lines[start].strip() == "/**", lines[start]
assert "ACTIVE_ICON_V" in lines[end], lines[end]

replacement = '''    /**
     * The stacking switch, on the first plate of the top-right block.
     *
     * <p>Only shown by item and fluid ports: an energy port has no cells for an entry to span, so
     * the setting controls nothing there and its plate is given to the reach-through switch
     * instead.</p>
     */
    private static final int MODE_X = 154;
    private static final int MODE_Y = 27;
    private static final int MODE_PLATE_U = 44;
    private static final int MODE_PLATE_V = 178;
    private static final int MODE_WIDTH = 21;
    private static final int MODE_HEIGHT = 17;
    private static final int MODE_ICON_U_SINGLE = 44;
    private static final int MODE_ICON_U_MULTI = 65;
    private static final int MODE_ICON_V = 195;

    /**
     * The reach-through switch: second plate for item and fluid ports, first for an energy port.
     *
     * <p>Sitting it after the stacking switch keeps the row contiguous; an energy port has no
     * stacking switch, so it takes the first plate rather than leaving a gap.</p>
     */
    private static final int ACTIVE_X = 179;
    /** Where the reach-through switch goes when there is no stacking switch in front of it. */
    private static final int ACTIVE_X_NO_MODE = 154;
    private static final int ACTIVE_Y = 27;
    private static final int ACTIVE_PLATE_U = 44;
    private static final int ACTIVE_PLATE_V = 178;
    private static final int ACTIVE_WIDTH = 21;
    private static final int ACTIVE_HEIGHT = 17;
    private static final int ACTIVE_ICON_U_PASSIVE = 44;
    private static final int ACTIVE_ICON_U_ACTIVE = 65;
    private static final int ACTIVE_ICON_V = 212;'''.split("\n")

lines[start:end + 1] = replacement

with io.open(p, "w", encoding="utf-8", newline="\n") as handle:
    handle.write("\n".join(lines))

print("constant block replaced; MODE_X present:", "MODE_X = 154" in "\n".join(lines))

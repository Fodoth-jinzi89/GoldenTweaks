"""The periodic check also re-stamps the casing.

While the machine stands, this tick only rescans when the shape is *not* formed — an assembled
machine is revalidated by the parent class instead. That left a gap: a port replaced while the
machine stood (a bus swapped for a hopper, a hatch broken and rebuilt) kept the plate of the port it
used to be, and nothing would correct it until the shape broke or the world reloaded.

Re-stamping on the same interval costs one field comparison per port and closes that gap. It runs
only on the already-formed path, so it never competes with the assembly scan.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        // super already revalidated an assembled shape on this same interval and invalidated it if
        // a block went missing, so the rescan only matters while the machine is unformed.
        if (isFormed()) {
            return;
        }
        if (--this.scanCountdown > 0) {
            return;
        }
        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;'''
new = '''        // super already revalidated an assembled shape on this same interval and invalidated it if
        // a block went missing, so the rescan only matters while the machine is unformed.
        if (isFormed()) {
            // An assembled machine still needs its plates kept current: a port swapped for another
            // kind keeps the plate of the one it replaced until something asks again, and nothing
            // else does while the shape holds.
            if (--this.scanCountdown > 0) {
                return;
            }
            this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;
            applyCasingToPorts();
            return;
        }
        if (--this.scanCountdown > 0) {
            return;
        }
        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("periodic check re-stamps the casing")

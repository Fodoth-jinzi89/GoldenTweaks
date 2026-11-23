"""Re-stamp the casing whenever the shape is refreshed.

`applyCasingToPorts` only ran once, from `formStructure`, on the tick the shape first matched. The
refresh button goes through `refreshStructure`, which re-reads the ports but never touched the
casing — so the plates only ever updated on a reload, when the machine formed again from scratch.

Calling it after the refresh makes both "the machine just formed" and "the player pressed refresh"
put the casing on immediately.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;
        applyRunningState();
        setChanged();
        // Energy totals come from the port bindings, so the client needs a fresh copy.'''
new = '''        this.scanCountdown = STRUCTURE_CHECK_INTERVAL_TICKS;

        // The plates follow the shape, and a refresh is exactly when the shape may have changed
        // hands: a port swapped for another kind, or the whole machine rebuilt in place.
        if (this.structure != null) {
            applyCasingToPorts();
        }

        applyRunningState();
        setChanged();
        // Energy totals come from the port bindings, so the client needs a fresh copy.'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("refresh re-stamps the casing")

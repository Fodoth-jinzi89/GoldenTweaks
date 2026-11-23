"""Wire the plate revert into both paths that take a machine apart."""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

# ---- invalidateStructure: the shape stopped matching ------------------------------------------
old = '''        setChannelsLocked(false);
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;

        this.structure = null;'''
new = '''        setChannelsLocked(false);
        // Same reason as the lock: the ports are still in the world and must go back to the plain
        // plate, which needs the positions remembered while this machine still knows them.
        clearCasingFromPorts();
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;

        this.structure = null;'''
assert old in s
s = s.replace(old, new, 1)

# ---- dismantle: the machine is taken apart on purpose ----------------------------------------
old2 = '''        setChannelsLocked(false);
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
        this.structure = null;
        this.running = false;'''
new2 = '''        setChannelsLocked(false);
        clearCasingFromPorts();
        this.activeChannel = null;
        this.activeMode = null;
        this.selectedChannel = null;
        this.structure = null;
        this.running = false;'''
assert old2 in s
s = s.replace(old2, new2, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("both teardown paths revert the plate")

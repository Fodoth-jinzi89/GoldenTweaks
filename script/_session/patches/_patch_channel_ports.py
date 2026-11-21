import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

pairs = [
    ("""            if (!port.isInput() || port.type() != MultiblockPortTypes.ITEM) {
                continue;
            }
            Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());""",
     """            if (!port.isInput() || port.type() != MultiblockPortTypes.ITEM || !portAllowed(port)) {
                continue;
            }
            Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());"""),

    ("""            if (!port.isInput() || port.type() != MultiblockPortTypes.FLUID) {
                continue;
            }
            Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());""",
     """            if (!port.isInput() || port.type() != MultiblockPortTypes.FLUID || !portAllowed(port)) {
                continue;
            }
            Object capability = MultiblockPortTypes.FLUID.capability(this.level, port.pos());"""),

    ("""        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(items, handler)) {""",
     """        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput() || !portAllowed(port)) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(items, handler)) {"""),

    ("""        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(outputs, handler)) {""",
     """        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput() || !portAllowed(port)) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler
                        && !MultiblockRecipeMatcher.canInsertItems(outputs, handler)) {"""),

    ("""        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput()) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler) {
                    MultiblockRecipeMatcher.insertItems(items, handler);""",
     """        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (port.isInput() || !portAllowed(port)) {
                continue;
            }
            if (port.type() == MultiblockPortTypes.ITEM) {
                Object capability = MultiblockPortTypes.ITEM.capability(this.level, port.pos());
                if (capability instanceof IItemHandler handler) {
                    MultiblockRecipeMatcher.insertItems(items, handler);"""),
]

for old, new in pairs:
    if old not in s:
        print('MISS:', old.splitlines()[1].strip()[:60])
    s = s.replace(old, new, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('done, changed' if s != orig else 'done, NO CHANGE')

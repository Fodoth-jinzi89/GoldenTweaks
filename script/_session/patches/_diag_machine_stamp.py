"""Diagnose the machine side of the stamp.

`getUpdateTag` already carries the field, so a client seeing `null` means the server never wrote it.
Either `applyCasingToPorts` is not reached, or it finds no port entities to write to.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

old = '''        String casingId = BuiltInRegistries.BLOCK.getKey(casingBlock()).toString();
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }
            entity.setCasing(casingId);
            this.restyledPorts.add(port.pos().immutable());
        }'''
new = '''        ResourceLocation casingId = BuiltInRegistries.BLOCK.getKey(casingBlock());
        int written = 0;
        int missing = 0;
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (!(this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity)) {
                missing++;
                continue;
            }
            if (casingId.equals(entity.casing())) {
                continue;
            }
            entity.setCasing(casingId);
            this.restyledPorts.add(port.pos().immutable());
            written++;
        }
        GoldenTweaks.LOGGER.info(
                "[GT/diag] applyCasingToPorts casing={} ports={} written={} missing={}",
                casingId, this.structure.getPorts().size(), written, missing);'''
assert old in s
s = s.replace(old, new, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("machine stamp diagnostic added")

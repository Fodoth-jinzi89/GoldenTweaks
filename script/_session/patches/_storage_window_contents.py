"""Give each storage a window-facing contents method, using imports for the row types."""
import io

ROW = "net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack"

# ---------------- chemical ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/mekanism/GTPortChemicalStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''import mekanism.api.Action;''',
'''import mekanism.api.Action;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;''', 1)
s = s.replace('''import java.util.ArrayList;
import java.util.List;''',
'''import java.util.ArrayList;
import java.util.List;
import java.util.Optional;''', 1)

s = s.replace('''    /**
     * The tank this chemical is allowed to enter, or {@code null} when it must be turned away.''',
'''    /**
     * This hopper's contents, as rows the port window can list.
     *
     * <p>Built here rather than by the window: a row needs a chemical's registry id, and that
     * conversion belongs with the class that already speaks Mekanism's types.</p>
     */
    public List<GTReactionStack> windowContents() {
        List<GTReactionStack> rows = new ArrayList<>();
        for (ChemicalStack tank : this.tanks) {
            if (tank.isEmpty()) {
                continue;
            }
            ResourceLocation id = tank.getChemicalHolder().unwrapKey()
                    .map(key -> key.location()).orElse(null);
            if (id == null) {
                continue;
            }
            rows.add(new GTReactionStack(GTReactionStack.Kind.CHEMICAL, id, tank.getAmount(),
                    Optional.empty()));
        }
        return rows;
    }

    /**
     * The tank this chemical is allowed to enter, or {@code null} when it must be turned away.''', 1)

s = s.replace('''import net.minecraft.nbt.ListTag;''',
'''import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("chemical storage:", "ok" if ROW not in s else "still inline")

# ---------------- essentia ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTPortEssentiaStorage.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''import net.minecraft.core.HolderLookup;''',
'''import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;
import net.minecraft.core.HolderLookup;''', 1)

s2 = s2.replace('''    /** The tank already holding this aspect, or {@code null}. */''',
'''    /**
     * This tank's contents, as rows the port window can list.
     *
     * <p>Essentia has no registry entry, so each row carries the aspect's tag and the stack type
     * this mod uses for it.</p>
     */
    public List<GTReactionStack> windowContents() {
        List<GTReactionStack> rows = new ArrayList<>();
        for (int index = 0; index < this.aspects.length; index++) {
            if (this.aspects[index] == null || this.amounts[index] <= 0) {
                continue;
            }
            rows.add(GTReactionStack.essentia(this.aspects[index].getTag(), this.amounts[index]));
        }
        return rows;
    }

    /** The tank already holding this aspect, or {@code null}. */''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("essentia storage:", "ok" if ROW not in s2 else "still inline")

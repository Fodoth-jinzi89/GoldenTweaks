"""List chemicals and essentia in the port window.

Both need their compat classes to read, so the work happens in GTPortStorages, which is only
reached after the mod check. The entity asks for rows and gets plain GTReactionStack values,
which is all the window ever wanted.
"""
import io

# ---------------- the bridge does the reading ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortStorages.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    /**
     * Split out so the reference to the chemical storage class is only resolved when called.''',
'''    /** The contents of a chemical hopper as window rows. */
    public static java.util.List<net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack>
            chemicalContents(@Nullable Object storage) {
        if (!(storage instanceof mekanism.api.chemical.IChemicalHandler handler)) {
            return java.util.List.of();
        }
        java.util.List<net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack> rows =
                new java.util.ArrayList<>();
        for (int tank = 0; tank < handler.getChemicalTanks(); tank++) {
            var stack = handler.getChemicalInTank(tank);
            if (stack.isEmpty()) {
                continue;
            }
            var id = stack.getChemicalHolder().unwrapKey()
                    .map(key -> key.location()).orElse(null);
            if (id == null) {
                continue;
            }
            rows.add(new net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack(
                    net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack.Kind.CHEMICAL,
                    id, stack.getAmount(), java.util.Optional.empty()));
        }
        return rows;
    }

    /** The contents of an essentia tank as window rows. */
    public static java.util.List<net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack>
            essentiaContents(@Nullable Object storage) {
        if (!(storage instanceof thaumcraft.api.aspects.IAspectContainer container)) {
            return java.util.List.of();
        }
        java.util.List<net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack> rows =
                new java.util.ArrayList<>();
        var list = container.getAspects();
        if (list == null) {
            return rows;
        }
        for (var aspect : list.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amount = list.getAmount(aspect);
            if (amount <= 0) {
                continue;
            }
            rows.add(net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack
                    .essentia(aspect.getTag(), amount));
        }
        return rows;
    }

    /**
     * Split out so the reference to the chemical storage class is only resolved when called.''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("bridge reads the new contents")

# ---------------- the entity delegates ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()
s2 = s2.replace('''            default -> {
            }
        }
        return rows;
    }''',
'''            case CHEMICAL -> rows.addAll(GTPortStorages.chemicalContents(this.chemicals));
            case ESSENTIA -> rows.addAll(GTPortStorages.essentiaContents(this.essentia));
            case ENERGY -> {
            }
        }
        return rows;
    }''', 1)
io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("port contents delegates")

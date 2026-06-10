package net.fodoth.skina.goldentweaks.compat.create;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;

public class GTCreateItems {

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_GEM_DUST;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_COMMON_MATERIAL;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_UNCOMMON_MATERIAL;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_RARE_MATERIAL;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_EPIC_MATERIAL;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_MYTHIC_MATERIAL;

    public static ItemEntry<SequencedAssemblyItem> UNPROCESSED_ANCIENT_MATERIAL;

    public static void register() {
        UNPROCESSED_GEM_DUST = sequencedItem("unprocessed_gem_dust");
        UNPROCESSED_COMMON_MATERIAL = sequencedItem("unprocessed_common_material");
        UNPROCESSED_UNCOMMON_MATERIAL = sequencedItem("unprocessed_uncommon_material");
        UNPROCESSED_RARE_MATERIAL = sequencedItem("unprocessed_rare_material");
        UNPROCESSED_EPIC_MATERIAL = sequencedItem("unprocessed_epic_material");
        UNPROCESSED_MYTHIC_MATERIAL = sequencedItem("unprocessed_mythic_material");
        UNPROCESSED_ANCIENT_MATERIAL = sequencedItem("unprocessed_ancient_material");
    }

    private static ItemEntry<SequencedAssemblyItem> sequencedItem(String name) {
        return GTCreateCompat.registrate()
                .item(name, SequencedAssemblyItem::new)
                .register();
    }
}
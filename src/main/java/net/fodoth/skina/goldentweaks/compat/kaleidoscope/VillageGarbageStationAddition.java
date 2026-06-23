package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import com.mojang.datafixers.util.Pair;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.mixin.feature.kaleidoscope.StructureTemplatePoolAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

public final class VillageGarbageStationAddition {

    private VillageGarbageStationAddition() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {

        Registry<StructureTemplatePool> templatePools =
                event.getServer()
                        .registryAccess()
                        .registryOrThrow(Registries.TEMPLATE_POOL);

        Registry<StructureProcessorList> processorLists =
                event.getServer()
                        .registryAccess()
                        .registryOrThrow(Registries.PROCESSOR_LIST);

        Holder<StructureProcessorList> processor =
                processorLists.getHolderOrThrow(ProcessorLists.MOSSIFY_70_PERCENT);

        // ✔ Plains
        add(templatePools, processor,
                "minecraft:village/plains/houses",
                "goldentweaks:garbage_station_plains",
                2);

        // ✔ Desert
        add(templatePools, processor,
                "minecraft:village/desert/houses",
                "goldentweaks:garbage_station_desert",
                2);

        // ✔ Savanna
        add(templatePools, processor,
                "minecraft:village/savanna/houses",
                "goldentweaks:garbage_station_savanna",
                2);

        // ✔ Snowy
        add(templatePools, processor,
                "minecraft:village/snowy/houses",
                "goldentweaks:garbage_station_snowy",
                2);

        // ✔ Taiga
        add(templatePools, processor,
                "minecraft:village/taiga/houses",
                "goldentweaks:garbage_station_taiga",
                2);

        GoldenTweaks.LOGGER.info("Added biome-specific garbage stations to villages");
    }

    private static void add(
            Registry<StructureTemplatePool> templatePoolRegistry,
            Holder<StructureProcessorList> processor,
            String poolRL,
            String structureRL,
            int weight
    ) {

        StructureTemplatePool pool =
                templatePoolRegistry.get(ResourceLocation.parse(poolRL));

        if (pool == null) return;

        StructureTemplatePoolAccessor accessor =
                (StructureTemplatePoolAccessor) pool;

        SinglePoolElement piece =
                SinglePoolElement
                        .legacy(structureRL, processor)
                        .apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            accessor.gt$getTemplates().add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> entries =
                new ArrayList<>(accessor.gt$getRawTemplates());

        entries.add(Pair.of(piece, weight));

        accessor.gt$setRawTemplates(entries);
    }

}
package net.fodoth.skina.goldentweaks.compat.compactmachines;

import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public final class CompactMachinesMobSpawnHandler {

    private CompactMachinesMobSpawnHandler() {
    }

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if ((event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION)
                && CompactDimension.isLevelCompact(event.getLevel().getLevel())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }
}

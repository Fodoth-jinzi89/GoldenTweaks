package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class VanillaFishSpawnEvent {

    private VanillaFishSpawnEvent() {
    }

    @SubscribeEvent
    public static void onPositionCheck(@NotNull MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL
                && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        EntityType<?> type = event.getEntity().getType();
        if (type != EntityType.COD && type != EntityType.SALMON
                && type != EntityType.PUFFERFISH && type != EntityType.TROPICAL_FISH
                && type != EntityType.SQUID && type != EntityType.GLOW_SQUID) {
            return;
        }
        double chance = GoldenTweaksCommonConfig.getVanillaFishSpawnChance();
        if (chance <= 0.0D || (chance < 1.0D && event.getEntity().getRandom().nextDouble() >= chance)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }
}

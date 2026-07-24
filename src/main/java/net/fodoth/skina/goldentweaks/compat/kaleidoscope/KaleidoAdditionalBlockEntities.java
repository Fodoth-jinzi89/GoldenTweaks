package net.fodoth.skina.goldentweaks.compat.kaleidoscope;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class KaleidoAdditionalBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    GoldenTweaks.MODID
            );

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<GarbageStationFloorBlockEntity>
            > GARBAGE_STATION_FLOOR =
            BLOCK_ENTITIES.register(
                    "garbage_station_floor",
                    () -> BlockEntityType.Builder.of(
                            GarbageStationFloorBlockEntity::new,
                            KaleidoAdditionalBlocks.GARBAGE_STATION_FLOOR.get()
                    ).build(null)
            );

    private KaleidoAdditionalBlockEntities() {
    }
}
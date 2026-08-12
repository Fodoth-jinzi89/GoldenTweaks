package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTThaumcraftAdditionalBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    GoldenTweaks.MODID
            );

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<GTInfusionIntercepterBlockEntity>
            > INFUSION_INTERCEPTER =
            BLOCK_ENTITIES.register(
                    "infusion_intercepter",
                    () -> BlockEntityType.Builder.of(
                            GTInfusionIntercepterBlockEntity::new,
                            GTThaumcraftAdditionalBlocks.INFUSION_INTERCEPTER.get()
                    ).build(null)
            );

    private GTThaumcraftAdditionalBlockEntities() {
    }
}

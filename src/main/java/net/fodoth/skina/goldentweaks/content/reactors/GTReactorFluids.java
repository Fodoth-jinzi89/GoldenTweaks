package net.fodoth.skina.goldentweaks.content.reactors;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class GTReactorFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, GoldenTweaks.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, GoldenTweaks.MODID);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, GoldenTweaks.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GoldenTweaks.MODID);

    private static final Map<GTReactorMaterial, DeferredHolder<FluidType, FluidType>> FLUID_TYPE_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    private static final Map<GTReactorMaterial, DeferredHolder<Fluid, BaseFlowingFluid.Source>> STILL_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    private static final Map<GTReactorMaterial, DeferredHolder<Fluid, BaseFlowingFluid.Flowing>> FLOWING_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    private static final Map<GTReactorMaterial, DeferredHolder<Block, LiquidBlock>> BLOCK_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    private static final Map<GTReactorMaterial, DeferredHolder<Item, BucketItem>> BUCKET_BY_MATERIAL =
            new EnumMap<>(GTReactorMaterial.class);

    static {
        for (GTReactorMaterial material : GTReactorMaterial.values()) {
            if (material.hasFluid()) {
                registerMaterial(material);
            }
        }
    }

    private GTReactorFluids() {
    }

    public static DeferredHolder<FluidType, FluidType> fluidType(GTReactorMaterial material) {
        return FLUID_TYPE_BY_MATERIAL.get(material);
    }

    public static DeferredHolder<Item, BucketItem> bucket(GTReactorMaterial material) {
        return BUCKET_BY_MATERIAL.get(material);
    }

    public static Collection<DeferredHolder<Item, BucketItem>> buckets() {
        return BUCKET_BY_MATERIAL.values();
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }

    private static void registerMaterial(GTReactorMaterial material) {
        FLUID_TYPE_BY_MATERIAL.put(material, FLUID_TYPES.register(material.id(),
                () -> new FluidType(FluidType.Properties.create()
                        .density(900)
                        .viscosity(1400)
                        .temperature(300))));

        STILL_BY_MATERIAL.put(material, FLUIDS.register(material.id(),
                () -> new BaseFlowingFluid.Source(properties(material))));

        FLOWING_BY_MATERIAL.put(material, FLUIDS.register("flowing_" + material.id(),
                () -> new BaseFlowingFluid.Flowing(properties(material))));

        BLOCK_BY_MATERIAL.put(material, BLOCKS.register(material.id(),
                () -> new LiquidBlock(STILL_BY_MATERIAL.get(material).get(), blockProperties())));

        BUCKET_BY_MATERIAL.put(material, ITEMS.register(material.bucketId(),
                () -> new BucketItem(
                        STILL_BY_MATERIAL.get(material).get(),
                        new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)
                )));
    }

    private static BaseFlowingFluid.Properties properties(GTReactorMaterial material) {
        return new BaseFlowingFluid.Properties(
                FLUID_TYPE_BY_MATERIAL.get(material),
                STILL_BY_MATERIAL.get(material),
                FLOWING_BY_MATERIAL.get(material)
        )
                .block(BLOCK_BY_MATERIAL.get(material))
                .bucket(BUCKET_BY_MATERIAL.get(material));
    }

    private static BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .replaceable()
                .noCollission()
                .strength(100.0F)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY);
    }
}

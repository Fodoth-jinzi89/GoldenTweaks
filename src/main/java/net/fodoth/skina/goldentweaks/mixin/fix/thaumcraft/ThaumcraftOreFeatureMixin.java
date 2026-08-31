package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.TCWorldgenConfig;
import thaumcraft.common.registry.TCBlocks;
import thaumcraft.common.worldgen.ThaumcraftBiomeInfo;
import thaumcraft.common.worldgen.ThaumcraftOreFeature;

import java.util.List;

@Mixin(value = ThaumcraftOreFeature.class, remap = false)
public class ThaumcraftOreFeatureMixin {

    @Inject(
            method = "generateInfusedStone(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/ChunkPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$generateInfusedStoneVeins(WorldGenLevel level, ChunkGenerator generator,
                                                     RandomSource random, ChunkPos chunkPos,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (!TCWorldgenConfig.generateInfusedStone()
                || !level.getLevel().dimension().equals(Level.OVERWORLD)
                || !TCWorldgenConfig.allowsDimension(Level.OVERWORLD, TCWorldgenConfig.WorldgenCategory.ORES)) {
            cir.setReturnValue(false);
            return;
        }

        int centerX = chunkPos.getMinBlockX() + 8;
        int centerZ = chunkPos.getMinBlockZ() + 8;
        int centerY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ);
        if (!TCWorldgenConfig.allowsBiome(level.getBiome(new BlockPos(centerX, centerY, centerZ)),
                TCWorldgenConfig.WorldgenCategory.ORES)) {
            cir.setReturnValue(false);
            return;
        }

        boolean generated = false;
        int minY = ThaumcraftOreFeature.infusedStoneMin(level);
        for (int i = 0; i < 8; i++) {
            int x = chunkPos.getMinBlockX() + random.nextInt(16);
            int z = chunkPos.getMinBlockZ() + random.nextInt(16);
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int span = ThaumcraftOreFeature.infusedStoneSpan(level, surfaceY);
            if (span <= 0) {
                continue;
            }

            BlockState infusedStone = gt$chooseInfusedStone(level, random, new BlockPos(x, surfaceY, z))
                    .defaultBlockState();
            OreConfiguration configuration = new OreConfiguration(List.of(
                    OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), infusedStone),
                    OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), infusedStone)
            ), 6);
            generated |= Feature.ORE.place(configuration, level, generator, random,
                    new BlockPos(x, minY + random.nextInt(span), z));
        }
        cir.setReturnValue(generated);
    }

    @Inject(
            method = "generateCrystals(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/ChunkPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$disableCrystalGeneration(WorldGenLevel level, RandomSource random, ChunkPos chunkPos,
                                                    CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Unique
    private static Block gt$chooseInfusedStone(WorldGenLevel level, RandomSource random, BlockPos pos) {
        int index = random.nextInt(6);
        if (random.nextInt(3) == 0) {
            Aspect aspect = ThaumcraftBiomeInfo.randomAspect(level.getBiome(pos), random);
            if (aspect == Aspect.AIR) index = 0;
            else if (aspect == Aspect.FIRE) index = 1;
            else if (aspect == Aspect.WATER) index = 2;
            else if (aspect == Aspect.EARTH) index = 3;
            else if (aspect == Aspect.ORDER) index = 4;
            else if (aspect == Aspect.ENTROPY) index = 5;
        }

        return switch (index) {
            case 0 -> TCBlocks.AIR_INFUSED_STONE.get();
            case 1 -> TCBlocks.FIRE_INFUSED_STONE.get();
            case 2 -> TCBlocks.WATER_INFUSED_STONE.get();
            case 3 -> TCBlocks.EARTH_INFUSED_STONE.get();
            case 4 -> TCBlocks.ORDER_INFUSED_STONE.get();
            default -> TCBlocks.ENTROPY_INFUSED_STONE.get();
        };
    }
}

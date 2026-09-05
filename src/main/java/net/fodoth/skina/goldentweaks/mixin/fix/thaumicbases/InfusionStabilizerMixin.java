package net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.blockentities.InfusionMatrixBlockEntity;

import java.util.Set;

@Mixin(value = InfusionMatrixBlockEntity.class, remap = false)
public abstract class InfusionStabilizerMixin {
    @Unique
    private static final Set<ResourceLocation> gt$STABILIZERS = Set.of(
            gt$id("eldritch_ark"), gt$id("eldritch_ark_slab"),
            gt$id("iron_greatwood"),
            gt$id("old_cobblestone"), gt$id("old_cobblestone_slab"),
            gt$id("old_mossy_cobblestone"), gt$id("old_mossy_cobblestone_slab"),
            gt$id("old_gravel"),
            gt$id("old_bricks"), gt$id("old_bricks_slab"),
            gt$id("old_lapis_block"), gt$id("old_lapis_slab"),
            gt$id("old_iron_block"), gt$id("old_iron_slab"),
            gt$id("old_gold_block"), gt$id("old_gold_slab"),
            gt$id("old_diamond_block"), gt$id("old_diamond_slab"),
            gt$id("quicksilver_block"), gt$id("quicksilver_bricks"),
            gt$id("salis_mundus_block"), gt$id("thauminite_block"),
            gt$id("air_crystal_block"), gt$id("air_crystal_slab"),
            gt$id("fire_crystal_block"), gt$id("fire_crystal_slab"),
            gt$id("water_crystal_block"), gt$id("water_crystal_slab"),
            gt$id("earth_crystal_block"), gt$id("earth_crystal_slab"),
            gt$id("order_crystal_block"), gt$id("order_crystal_slab"),
            gt$id("entropy_crystal_block"), gt$id("entropy_crystal_slab"),
            gt$id("mixed_crystal_block"), gt$id("mixed_crystal_slab"),
            gt$id("tainted_crystal_block"), gt$id("tainted_crystal_slab")
    );

    @Shadow
    private Level level;

    @Inject(method = "isStabilizer", at = @At("HEAD"), cancellable = true)
    private void gt$recognizeThaumicBasesBlocks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (gt$STABILIZERS.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static ResourceLocation gt$id(String path) {
        return ResourceLocation.fromNamespaceAndPath("thaumicbases", path);
    }
}

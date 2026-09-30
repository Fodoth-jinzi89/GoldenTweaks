package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.lib.utils.CropUtils;
import tb.common.block.RedlonStemBlock;

/**
 * Thaumcraft's crop detection returns {@code true} for anything with an {@code age} property that is also in
 * {@code minecraft:crops} / its own harvest tags. Melon-family <b>stems</b> match that shape, so the harvest golem
 * used to walk up and destroy the vine instead of the fruit it carries.
 * <p>
 * This makes the vine never harvestable and the fruit always harvestable:
 * <ul>
 *     <li>vanilla {@link StemBlock} / {@link AttachedStemBlock} and any block whose id ends in {@code _stem}
 *     (covers e.g. {@code spectrum:glistering_melon_stem}) are never a harvest target;</li>
 *     <li>{@code goldentweaks:melon_fruits} (vanilla melon/pumpkin, {@code spectrum:glistering_melon}) always is;</li>
 *     <li>Thaumic Bases' redlon packs the stem and its fruit into one block - only the {@code ATTACHMENT} states
 *     count as fruit.</li>
 * </ul>
 */
@Mixin(value = CropUtils.class, remap = false)
public abstract class CropUtilsMixin {

    @Unique
    private static final TagKey<Block> gt$MELON_FRUITS =
            BlockTags.create(ResourceLocation.fromNamespaceAndPath("goldentweaks", "melon_fruits"));

    @Inject(method = "isGrownCrop", at = @At("HEAD"), cancellable = true)
    private static void gt$melonFamily(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof RedlonStemBlock) {
            // A bare redlon plant is the vine; only an attached one is the "redlon melon" itself.
            cir.setReturnValue(state.getValue(RedlonStemBlock.ATTACHMENT) != RedlonStemBlock.Attachment.NONE);
            return;
        }
        if (state.is(gt$MELON_FRUITS)) {
            cir.setReturnValue(true);
            return;
        }
        if (gt$isStem(block)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean gt$isStem(Block block) {
        if (block instanceof StemBlock || block instanceof AttachedStemBlock) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id != null && id.getPath().endsWith("_stem");
    }
}

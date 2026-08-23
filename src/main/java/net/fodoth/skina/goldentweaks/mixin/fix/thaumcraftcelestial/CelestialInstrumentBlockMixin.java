package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraftcelestial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.celestial.common.block.CelestialInstrumentBlock;
import thaumcraft.celestial.common.blockentity.CelestialInstrumentBlockEntity;

@Mixin(BlockBehaviour.class)
public class CelestialInstrumentBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void gt$processWithoutMenu(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND
                || !(state.getBlock() instanceof CelestialInstrumentBlock)
                || !(level.getBlockEntity(pos) instanceof CelestialInstrumentBlockEntity instrument)
                || !gt$isCatalyst(instrument, stack)) {
            return;
        }

        if (!level.isClientSide) {
            instrument.processHeldCatalyst(player);
        }
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }

    private static boolean gt$isCatalyst(CelestialInstrumentBlockEntity instrument, ItemStack stack) {
        String catalyst = switch (instrument.kind()) {
            case STARGAZING_INSTRUMENT -> "thaumcraftcelestial:meteorite_fragment";
            case CELESTIAL_LENS -> "avaritia:blaze_cube";
            case ASTROLABE -> "thaumic_tinkerer:ichorium_ingot";
            case LUNAR_PHASE_INSTRUMENT -> "spectrum:moonstruck_nectar";
        };
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(ResourceLocation.parse(catalyst));
    }
}

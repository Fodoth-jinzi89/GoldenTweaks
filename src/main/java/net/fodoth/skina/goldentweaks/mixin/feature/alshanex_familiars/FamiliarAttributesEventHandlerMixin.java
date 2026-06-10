package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.event.FamiliarAttributesEventHandler;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksAttributesHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FamiliarAttributesEventHandler.class)
public class FamiliarAttributesEventHandlerMixin {

    @Redirect(
            method = "onCurioEquip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;handleSpellbookEquipChange(Lnet/minecraft/server/level/ServerPlayer;Z)V"
            )
    )
    private static void gt$handleSpellbookEquip(
            ServerPlayer player,
            boolean equipped
    ) {

        GoldenTweaksAttributesHelper.handleSpellbookEquipChange(
                player,
                equipped
        );
    }

    @Redirect(
            method = "onCurioUnequip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;handleSpellbookEquipChange(Lnet/minecraft/server/level/ServerPlayer;Z)V"
            )
    )
    private static void gt$handleSpellbookUnequip(
            ServerPlayer player,
            boolean equipped
    ) {
        GoldenTweaksAttributesHelper.handleSpellbookEquipChange(
                player,
                equipped
        );
    }

    @Redirect(
            method = "onPlayerTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/alshanex/familiarslib/util/familiars/FamiliarAttributesHelper;handlePlayerAttributeChange(Lnet/minecraft/server/level/ServerPlayer;)V"
            )
    )
    private static void gt$handlePlayerAttributeChange(
            ServerPlayer player
    ) {
        GoldenTweaksAttributesHelper.handlePlayerAttributeChange(
                player
        );
    }
}

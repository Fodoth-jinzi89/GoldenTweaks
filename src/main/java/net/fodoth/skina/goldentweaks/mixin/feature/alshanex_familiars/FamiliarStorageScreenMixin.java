package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.screen.FamiliarStorageScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

@Mixin(FamiliarStorageScreen.class)
public abstract class FamiliarStorageScreenMixin {

    @Shadow
    private int storedPanelX;

    @Shadow
    private int playerPanelX;

    @Shadow
    private int PANEL_WIDTH;

    @Shadow
    private int PANEL_HEIGHT;

    @Shadow
    private int panelY;

    @Shadow
    private int storedScrollOffset;

    @Shadow
    private int playerScrollOffset;

    @Shadow
    private int FAMILIAR_ITEM_HEIGHT;

    @Shadow
    private UUID selectedStoredFamiliar;

    @Shadow
    private UUID selectedPlayerFamiliar;

    @Shadow
    @Final
    private List<?> storedFamiliars;

    @Shadow
    @Final
    private List<?> playerFamiliars;

    @Shadow
    protected abstract void updateButtonStates();

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gt$mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (button != 0) {
            return;
        }

        // =====================================================
        // Stored panel
        // =====================================================

        if (gt$isInsidePanel(
                mouseX,
                mouseY,
                this.storedPanelX
        )) {

            UUID id = gt$getClickedId(
                    mouseY,
                    this.panelY,
                    this.storedScrollOffset,
                    this.FAMILIAR_ITEM_HEIGHT,
                    this.storedFamiliars
            );

            if (id != null) {

                this.selectedStoredFamiliar = id;
                this.selectedPlayerFamiliar = null;

                this.updateButtonStates();

                cir.setReturnValue(true);
            }

            return;
        }

        // =====================================================
        // Player panel
        // =====================================================

        if (gt$isInsidePanel(
                mouseX,
                mouseY,
                this.playerPanelX
        )) {

            UUID id = gt$getClickedId(
                    mouseY,
                    this.panelY,
                    this.playerScrollOffset,
                    this.FAMILIAR_ITEM_HEIGHT,
                    this.playerFamiliars
            );

            if (id != null) {

                this.selectedPlayerFamiliar = id;
                this.selectedStoredFamiliar = null;

                this.updateButtonStates();

                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean gt$isInsidePanel(
            double mouseX,
            double mouseY,
            int panelX
    ) {

        return mouseX >= panelX
                && mouseX < panelX + this.PANEL_WIDTH
                && mouseY >= this.panelY
                && mouseY < this.panelY + this.PANEL_HEIGHT;
    }

    @Unique
    private static UUID gt$getClickedId(
            double mouseY,
            int panelY,
            int scrollOffset,
            int itemHeight,
            List<?> entries
    ) {

        int relativeY =
                (int) (mouseY - panelY + scrollOffset);

        int itemIndex =
                relativeY / itemHeight;

        if (itemIndex < 0
                || itemIndex >= entries.size()) {

            return null;
        }

        Object entry =
                entries.get(itemIndex);

        try {

            Field field =
                    entry.getClass().getDeclaredField("id");

            field.setAccessible(true);

            return (UUID) field.get(entry);

        } catch (ReflectiveOperationException e) {

            return null;
        }
    }
}
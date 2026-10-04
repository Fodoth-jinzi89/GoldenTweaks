package net.fodoth.skina.goldentweaks.mixin.fix.appflux;

import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternProviderMenu;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.mixin.fix.appflux.accessor.AEBaseMenuSemanticsAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rearth.ae2helpers.ae2helpers;

import java.util.List;

/**
 * The sidebar now shows every slot of the single shared upgrade inventory (appflux / extendedae_plus
 * {@code af_upgrades}, enlarged to 4 slots / 7 for ExtendedAE), so all cards work from any slot. This mixin only
 * handles the case where ae2helpers' own menu mixin is still enabled: its separate import slot is moved into the
 * sidebar so it is not left dangling off-screen.
 */
@Mixin(PatternProviderMenu.class)
public abstract class PatternProviderMenuMergeMixin extends AEBaseMenu {

    protected PatternProviderMenuMergeMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/patternprovider/PatternProviderLogicHost;)V",
            at = @At("TAIL")
    )
    private void gt$mergeImportUpgradeSlot(MenuType<?> menuType, int id, Inventory playerInventory,
                                           PatternProviderLogicHost host, CallbackInfo ci) {
        if (this.getSlots(SlotSemantics.UPGRADE).isEmpty()) {
            return;
        }

        List<Slot> importSlots = this.getSlots(ae2helpers.IMPORT_UPGRADE);
        if (importSlots.isEmpty()) {
            return;
        }

        GoldenTweaks.LOGGER.info("[GT/appflux] menu: moving {} ae2helpers import slot(s) into UPGRADE",
                importSlots.size());
        moveToUpgradeSemantic(importSlots);
    }

    private void moveToUpgradeSemantic(List<Slot> importSlots) {
        var semantics = (AEBaseMenuSemanticsAccessor) this;
        var slotsBySemantic = semantics.gt$getSlotsBySemantic();
        var semanticBySlot = semantics.gt$getSemanticBySlot();
        for (Slot slot : List.copyOf(importSlots)) {
            slotsBySemantic.remove(ae2helpers.IMPORT_UPGRADE, slot);
            slotsBySemantic.put(SlotSemantics.UPGRADE, slot);
            semanticBySlot.put(slot, SlotSemantics.UPGRADE);
        }
    }
}

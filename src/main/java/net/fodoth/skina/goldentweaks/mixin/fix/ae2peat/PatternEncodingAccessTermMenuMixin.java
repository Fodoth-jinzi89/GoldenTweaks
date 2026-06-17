package net.fodoth.skina.goldentweaks.mixin.fix.ae2peat;

import appeng.api.networking.IGridNode;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.parts.encoding.EncodingMode;
import cn.dancingsnow.neoecoae.api.IECOPatternStorageService;
import cn.dancingsnow.neoecoae.api.PatternEncodingTermMenuExtension;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.IPEATMenuHost;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;

@Mixin({PatternEncodingAccessTermMenu.class})
public class PatternEncodingAccessTermMenuMixin extends AEBaseMenu implements PatternEncodingTermMenuExtension {
    @Shadow
    @Final
    private RestrictedInputSlot encodedPatternSlot;
    @Shadow
    private EncodingMode currentMode;
    @Unique
    private final String ACTION_UPLOAD_PATTERN = "neoecoae:uploadPattern";

    @Shadow
    public @Nullable IGridNode getGridNode() {
        return null;
    }

    @Shadow
    public ILinkStatus getLinkStatus() {
        return null;
    }

    public PatternEncodingAccessTermMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, ITerminalHost host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(
            method = {"<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lyuuki1293/ae2peat/menu/IPEATMenuHost;Z)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lyuuki1293/ae2peat/menu/PatternEncodingAccessTermMenu;registerClientAction(Ljava/lang/String;Ljava/lang/Runnable;)V",
                    ordinal = 0
            )}
    )
    void onRegisterClientActions(MenuType<?> menuType, int id, Inventory ip, IPEATMenuHost host, boolean bindInventory, CallbackInfo ci) {
        this.registerClientAction("neoecoae:uploadPattern", this::neoecoae$uploadPattern);
    }

    public void neoecoae$uploadPattern() {
        if (this.isClientSide()) {
            this.sendClientAction("neoecoae:uploadPattern");
        } else {
            IGridNode node = this.getGridNode();
            if (node != null) {
                if (this.getLinkStatus().connected()) {
                    if (this.currentMode != EncodingMode.PROCESSING) {
                        ItemStack itemStack = this.encodedPatternSlot.getItem();
                        IECOPatternStorageService service = node.getGrid().getService(IECOPatternStorageService.class);
                        if (service != null && service.getPatternStorage().insertPattern(itemStack.copy())) {
                            this.encodedPatternSlot.clearStack();
                        }

                    }
                }
            }
        }
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import com.gali.ae2_auto_pattern_upload.client.ProviderSelectScreen;
import com.gali.ae2_auto_pattern_upload.client.RecipeTypeMappingScreen;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import appeng.client.gui.widgets.AE2Button;
import com.gali.ae2_auto_pattern_upload.network.UploadEncodedPatternC2SPacket;
import com.gali.ae2_auto_pattern_upload.client.ResizableAETextField;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.ProviderIconCache;
import net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload.accessor.ProviderGroupAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@Mixin(ProviderSelectScreen.class)
public abstract class ProviderSelectScreenMixin extends Screen {
    @Shadow
    @Final
    private List<Button> entryButtons;

    @Shadow
    @Final
    private List<?> filteredGroups;

    @Shadow
    private int page;

    @Shadow
    private int pageSize;

    @Shadow
    private ResizableAETextField mappingValueInput;

    @Shadow
    private String query;

    protected ProviderSelectScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gt$layoutEntries(CallbackInfo ci) {
        int rows = pageSize;
        int capacity = rows * 4;
        int maxPage = Math.max(0, (filteredGroups.size() - 1) / capacity);
        page = Math.min(page, maxPage);
        int top = entryButtons.isEmpty() ? height / 2 - rows * 25 / 2 : entryButtons.getFirst().getY();
        for (Button button : entryButtons) {
            removeWidget(button);
        }
        entryButtons.clear();

        int gap = 6;
        int buttonWidth = 117;
        int columns = 4;
        int left = width / 2 - (buttonWidth * columns + gap * (columns - 1)) / 2;
        int from = page * capacity;
        int to = Math.min(filteredGroups.size(), from + capacity);
        for (int i = from; i < to; i++) {
            ProviderGroupAccessor group = (ProviderGroupAccessor) filteredGroups.get(i);
            int localIndex = i - from;
            Button button = new AE2Button(
                    left + localIndex % columns * (buttonWidth + gap),
                    top + localIndex / columns * 25,
                    buttonWidth,
                    20,
                    Component.literal("   " + group.gt$label()),
                    ignored -> {
                        PacketDistributor.sendToServer(new UploadEncodedPatternC2SPacket(
                                group.gt$getRepresentativeId(), false, group.gt$getName()));
                        onClose();
                    }
            );
            entryButtons.add(button);
            addRenderableWidget(button);
        }

        for (var child : children()) {
            if (child instanceof Button button) {
                if (button.getMessage().getString().equals("<")) {
                    button.active = page > 0;
                } else if (button.getMessage().getString().equals(">")) {
                    button.active = page < maxPage;
                }
            }
        }

    }

    @Inject(method = "addMappingFromUi", at = @At("HEAD"), cancellable = true)
    private void gt$openMappingManagementWhenIncomplete(CallbackInfo ci) {
        String recipeType = query == null ? "" : query.trim();
        String providerSearch = mappingValueInput == null ? "" : mappingValueInput.getValue().trim();
        if (recipeType.isEmpty() || providerSearch.isEmpty()) {
            minecraft.setScreen(new RecipeTypeMappingScreen(this));
            ci.cancel();
        }
    }

    @Inject(method = "changePage", at = @At("HEAD"), cancellable = true)
    private void gt$changePage(int delta, CallbackInfo ci) {
        int nextPage = page + delta;
        int capacity = pageSize * 4;
        if (nextPage >= 0 && nextPage * capacity < filteredGroups.size()) {
            page = nextPage;
            refreshPending = true;
        }
        ci.cancel();
    }

    @Shadow
    private boolean refreshPending;

    @ModifyExpressionValue(
            method = "mouseClicked",
            at = @At(value = "FIELD", target = "Lcom/gali/ae2_auto_pattern_upload/client/ProviderSelectScreen;pageSize:I")
    )
    private int gt$pinPageCapacity(int original) {
        return original * 4;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void gt$renderProviderIcons(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        for (int i = 0; i < entryButtons.size(); i++) {
            int groupIndex = page * pageSize * 4 + i;
            if (groupIndex >= filteredGroups.size()) {
                break;
            }
            String name = ((ProviderGroupAccessor) filteredGroups.get(groupIndex)).gt$getName();
            ItemStack icon = ProviderIconCache.get(name);
            if (!icon.isEmpty()) {
                Button button = entryButtons.get(i);
                guiGraphics.renderItem(icon, button.getX() + 3, button.getY() + 2);
            }
        }
    }
}

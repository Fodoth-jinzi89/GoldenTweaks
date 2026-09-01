package net.fodoth.skina.goldentweaks.mixin.fix.ae2;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.Repo;
import appeng.core.AEConfig;
import appeng.integration.abstraction.ItemListMod;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MEStorageScreenSearchMixin {
    @Shadow
    @Final
    protected Repo repo;

    @Unique
    private String gt$pendingSearch;
    @Unique
    private long gt$lastInputTick;
    @Unique
    private boolean gt$applyingSearch;

    @Inject(method = "setSearchText", at = @At("HEAD"), cancellable = true)
    private void gt$delaySearch(String searchString, CallbackInfo ci) {
        if (gt$applyingSearch) {
            return;
        }
        if (GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get() == 0) {
            return;
        }
        if (searchString.equals(repo.getSearchString()) || searchString.equals(gt$pendingSearch)) {
            ci.cancel();
            return;
        }
        gt$pendingSearch = searchString;
        gt$lastInputTick = gt$currentTick();
        ci.cancel();
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void gt$applyDelayedSearch(CallbackInfo ci) {
        if (gt$pendingSearch == null
                || gt$currentTick() - gt$lastInputTick < GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get()) {
            return;
        }
        String searchString = gt$pendingSearch;
        gt$pendingSearch = null;
        gt$applyingSearch = true;
        try {
            gt$setSearchText(searchString);
            if (AEConfig.instance().isSyncWithExternalSearch()) {
                ItemListMod.setSearchText(searchString);
            }
        } finally {
            gt$applyingSearch = false;
        }
    }

    @Redirect(
            method = "updateSearch",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/integration/abstraction/ItemListMod;setSearchText(Ljava/lang/String;)V"
            )
    )
    private void gt$delayExternalSearch(String searchString) {
        if (GoldenTweaksClientConfig.SEARCH_TRIGGER_THRESHOLD.get() == 0) {
            ItemListMod.setSearchText(searchString);
        }
    }

    @Invoker("setSearchText")
    protected abstract void gt$setSearchText(String searchString);

    @Unique
    private long gt$currentTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}

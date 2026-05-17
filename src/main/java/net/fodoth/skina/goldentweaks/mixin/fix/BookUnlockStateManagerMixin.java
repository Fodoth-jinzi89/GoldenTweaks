package net.fodoth.skina.goldentweaks.mixin.fix;

import com.klikli_dev.modonomicon.bookstate.BookStatesSaveData;
import com.klikli_dev.modonomicon.bookstate.BookUnlockStateManager;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = BookUnlockStateManager.class, remap = false)
public class BookUnlockStateManagerMixin {

    @Shadow
    public BookStatesSaveData saveData;

    @Inject(
            method = "updateAndSyncFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/klikli_dev/modonomicon/bookstate/BookStatesSaveData;setDirty()V"
            ),
            cancellable = true
    )
    private void gt$ensureSaveData(ServerPlayer player, CallbackInfo ci) {
        if (this.saveData == null) {
            try {
                this.saveData = Objects.requireNonNull(player.getServer())
                        .overworld()
                        .getDataStorage()
                        .computeIfAbsent(
                                new SavedData.Factory<>(
                                        BookStatesSaveData::new,
                                        BookStatesSaveData::load,
                                        DataFixTypes.PLAYER
                                ),
                                "modonomicon_book_states"
                        );

            } catch (Throwable t) {
                GoldenTweaks.LOGGER.warn(
                        "Failed recovering Modonomicon saveData",
                        t
                );
                ci.cancel();
            }
        }
    }
}

package net.fodoth.skina.goldentweaks.mixin.shut;

import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayload;
import com.github.cao.awa.annuus.network.packet.client.update.NoticeUpdateServerAnnuusPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = NoticeUpdateServerAnnuusPayloadHandler.class, remap = false)
public class AnnuusUpdateNoticeMixin {

    /**
     * @author GoldenTweaks
     * @reason Suppress the obsolete and unnecessary Annuus version update notice
     */
    @Overwrite
    public static void tryUpdateAnnuusVersion(NoticeUpdateServerAnnuusPayload payload,
                                              Minecraft minecraft, LocalPlayer player) {
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.bountiful;

import io.ejekta.bountiful.client.widgets.BountyLongButton;
import io.ejekta.bountiful.components.BountyDataEntry;
import io.ejekta.kambrik.gui.draw.KGuiDsl;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BountyLongButton.class)
public class BountyLongButtonMixin {

    @Redirect(
            method = "onDraw$lambda$20",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/ejekta/bountiful/client/widgets/BountyLongButton$Companion;renderEntries(Lio/ejekta/kambrik/gui/draw/KGuiDsl;Ljava/util/List;Lkotlin/jvm/functions/Function4;)V",
                    remap = false
            ),
            remap = false
    )
    private static void redirectRenderEntries(BountyLongButton.Companion companion,
                                              KGuiDsl $this$renderEntries,
                                              List<BountyDataEntry> entries,
                                              Function4<? super KGuiDsl, ? super Integer, ? super Integer, ? super BountyDataEntry, Unit> renderFunc) {
        if (entries.isEmpty()) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        int total = entries.size();
        int visibleCount = Math.min(total, 4);      // 最多显示4个
        int start = 0;

        if (total > 4 && level != null) {
            long gameTime = level.getGameTime();
            start = (int) ((gameTime / 20) % total); // 每秒切换一次
        }

        int spaceDiff = BountyLongButton.BountyZoneSize - visibleCount * 18;
        int spaceStart = spaceDiff / 2;

        for (int i = 0; i < visibleCount; i++) {
            int idx = (start + i) % total;
            renderFunc.invoke($this$renderEntries, spaceStart + i * 18, 1, entries.get(idx));
        }
    }
}
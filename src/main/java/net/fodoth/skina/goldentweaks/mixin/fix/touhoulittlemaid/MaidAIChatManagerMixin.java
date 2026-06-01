package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MaidAIChatManager.class)
public class MaidAIChatManagerMixin {

    /**
     * 清理 TTS 文本中的 part数字 标签
     */
    @ModifyVariable(
            method = "tts",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private String goldentweaks$cleanTTSText(String ttsText) {
        if (ttsText == null) {
            return null;
        }

        ttsText = ttsText.replaceAll("(?im)^\\s*part\\s*\\d+[:：]?\\s*\\n?", "");

        return ttsText.trim();
    }
}

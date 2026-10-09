package net.fodoth.skina.goldentweaks.mixin.fix.maidsoulcore;

import com.maidsoul.brain.forge.network.MaidSoulOwnerChatPacket;
import com.maidsoul.brain.forge.runtime.MaidSoulRuntime;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * 「心契誓约大模型连接」关闭时：主人发起对话不再进入思考核心，
 * 改为在聊天栏提示去 GoldenTweaks 设置里开启。
 */
@Mixin(value = MaidSoulOwnerChatPacket.class, remap = false)
public class MaidSoulOwnerChatPacketMixin {

    private static final String LLM_DISABLED_NOTICE_KEY = "goldentweaks.maid.llm_disabled";

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$noticeWhenLlmDisabled(ServerPlayer player, int maidEntityId, String message,
                                                 Consumer<Component> status, Consumer<MaidSoulRuntime> configure,
                                                 CallbackInfo ci) {
        if (!GoldenTweaksClientConfig.isMaidLlmEnabled() && message != null && !message.isBlank()) {
            status.accept(Component.translatable(LLM_DISABLED_NOTICE_KEY));
            ci.cancel();
        }
    }
}

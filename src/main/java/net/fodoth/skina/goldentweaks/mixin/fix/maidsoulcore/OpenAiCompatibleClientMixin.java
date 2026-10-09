package net.fodoth.skina.goldentweaks.mixin.fix.maidsoulcore;

import com.maidsoul.brain.llm.ChatPayload;
import com.maidsoul.brain.llm.InterruptFlag;
import com.maidsoul.brain.llm.LlmRequestException;
import com.maidsoul.brain.llm.LlmResponse;
import com.maidsoul.brain.llm.OpenAiCompatibleClient;
import com.maidsoul.brain.tool.ToolSpec;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 「心契誓约大模型连接」关闭时，直接在请求发出前拦截，保证不会真的连接大模型
 * （planner / replyer / timing / memory 等全部走 send / sendStream）。
 */
@Mixin(value = OpenAiCompatibleClient.class, remap = false)
public class OpenAiCompatibleClientMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$blockSendWhenLlmDisabled(String requestKind, List<ChatPayload> messages, List<ToolSpec> tools,
                                             long timeoutMillis, InterruptFlag interruptFlag,
                                             CallbackInfoReturnable<LlmResponse> cir) {
        gt$throwIfDisabled(requestKind);
    }

    @Inject(method = "sendStream", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$blockSendStreamWhenLlmDisabled(String requestKind, List<ChatPayload> messages, long timeoutMillis,
                                                   Consumer<String> deltaConsumer, InterruptFlag interruptFlag,
                                                   CallbackInfoReturnable<LlmResponse> cir) {
        gt$throwIfDisabled(requestKind);
    }

    private static void gt$throwIfDisabled(String requestKind) {
        if (!GoldenTweaksClientConfig.isMaidLlmEnabled()) {
            throw new LlmRequestException(requestKind, "disabled", 0, 0L, "心契誓约大模型连接已关闭", null);
        }
    }
}

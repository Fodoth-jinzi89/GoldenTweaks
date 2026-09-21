package net.fodoth.skina.goldentweaks.mixin.fix.oneenoughitem;

import com.google.gson.JsonElement;
import net.fodoth.skina.goldentweaks.compat.oei.OeiRawTagResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * 保存本次资源重载使用的 ResourceManager，供 {@link OeiRawTagResolver} 在注册表标签未就绪时回退读取原始 tags/item。
 *
 * <p>reload 的 prepare 阶段一定早于任何 listener 的 apply 阶段，因此捕获到的 ResourceManager 在
 * OEI/OELib 校验规则时必然可用。</p>
 *
 * <p>注意：目标方法有返回值，handler 必须接收 {@link CallbackInfoReturnable}，即使不使用。</p>
 */
@Mixin(SimpleJsonResourceReloadListener.class)
public class RawTagCaptureMixin {

    @Inject(
            method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;",
            at = @At("HEAD")
    )
    private void gt$captureResourceManager(
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir
    ) {
        OeiRawTagResolver.capture(resourceManager);
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.papi;

import com.dipilodopilasaurus.putapluginit.leaks.ClientWorldLeaveCleanup;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps Put A Plug In it!'s world-leave cleanup from wiping the baked-model registry.
 *
 * <p>{@code leakFixes.minecraft.modelManager.bakedModelMaps.clearOnClientWorldLeave} defaults to
 * {@code true}, so every time the client leaves a world {@code ClientWorldLeaveCleanup} calls
 * {@code MinecraftClientMitigations#tryClearLargeBakedModelMaps}. That method reflectively walks every
 * {@code Map} field of {@code ModelManager} and calls {@code clear()} on any map that holds at least
 * {@code minSizeBeforeClear} (default 10000) values which are {@code BakedModel}s - i.e. the baked model
 * registry itself, which holds hundreds of thousands of entries in a big pack.</p>
 *
 * <p>That map is not a leak: it is the content built by the last resource reload and every later
 * {@code ModelManager#getModel} lookup resolves through it. Once it is emptied, lookups fall back to the
 * missing model, so anything that resolves a model lazily (item frames being the obvious case, hence their
 * entity model texture turning into the purple/black missing texture) renders broken after re-entering a
 * world - in single player and on a server alike - until {@code F3 + T} re-bakes everything.</p>
 *
 * <p>Nothing about the block state cache cleanup ({@code blockStateToIdMap}, disabled by default) is
 * touched: only the baked model map clearing is cancelled.</p>
 */
@Mixin(value = ClientWorldLeaveCleanup.class, remap = false)
public class ClientWorldLeaveCleanupMixin {

    /** One debug line per game session, so a log can tell this patch is the one running. */
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Inject(method = "cleanupBakedModelMaps", at = @At("HEAD"), cancellable = true)
    private static void gt$keepBakedModelRegistry(CallbackInfo ci) {
        if (LOGGED.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.debug(
                    "[PAPI 兼容] 已禁用离开世界时清空 ModelManager 已烘焙模型表的清理（本条只打印一次）"
            );
        }

        ci.cancel();
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Silences Touhou Little Maid's "Error when checking is(Block) method for caching burning state" spam on a
 * dedicated server.
 *
 * <p>TLM injects a post-handler into {@code NodeEvaluator#isBurningBlock(BlockState)} which reflects over the
 * block's class ({@code Block{spectrum:preservation_chest}} in the 圆理服务端 log) to find an
 * {@code is(Block)} method it wants to cache. Spectrum's block class declares a method taking
 * {@code net.minecraft.client.resources.model.Material}, and that client-only class does not exist on a
 * dedicated server, so the reflection blows up with
 * {@code NoClassDefFoundError: net.minecraft.client.resources.model.Material} - TLM catches it and logs an
 * ERROR, and its burning-state cache for that block simply is not populated (it falls back to the vanilla
 * check).</p>
 *
 * <p>So the handler is skipped here: the outcome is the same as the failing path (no cache entry, vanilla
 * burning check still applies for every entity's pathfinding), without the reflection throwing and without
 * the ERROR line. Everything else TLM does is untouched.</p>
 *
 * <p>The method name is matched with a wildcard because Mixin prefixes its own generated part
 * ({@code handler$<hash>$touhou_little_maid$postIsBurningBlock}) - the trailing part belongs to TLM and is
 * stable, the hash changes with mappings. {@code require = 0} means a future rename degrades to "no
 * suppression" instead of breaking a class the whole game pathfinds through.</p>
 */
@Mixin(value = NodeEvaluator.class)
public abstract class NodeEvaluatorBurningCacheMixin {

    /** One debug line per game session, so this patch can be confirmed without adding noise. */
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Inject(
            method = "handler$*$touhou_little_maid$postIsBurningBlock",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void gt$skipBurningCacheReflection(CallbackInfo ci) {
        if (LOGGED.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.debug(
                    "[GT-TLM 兼容] 已跳过火烧状态缓存反射（服务端缺少客户端类 net.minecraft.client.resources.model.Material，本条只打印一次）"
            );
        }

        ci.cancel();
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.thermal;

import cofh.thermal.core.client.renderer.model.UnderlayBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Thermal 的 {@code UnderlayBakedModel} 用两个静态字段缓存底纹 BakedQuad：{@code FLUID_QUAD_CACHE}
 * 是 fastutil 的 {@code Object2ObjectOpenHashMap}，{@code UNDERLAY_QUAD_CACHE} 是 {@code IdentityHashMap}，
 * 而 {@code addUnderlayQuads} 里的 get/put 全都没有加锁。
 * <p>
 * Sodium 用多个 worker 线程并行烘焙区块网格，会有多个线程同时走到这里。
 * 多线程并发 put 命中扩容（rehash）时把开地址哈希表的结构写坏，随后 fastutil 在
 * {@code rehash} 里抛 {@code ArrayIndexOutOfBoundsException}，表现为
 * "Encountered exception while building chunk meshes" 崩溃。
 * <p>
 * 这里把四个缓存访问分别包进各自实例的 monitor，使结构变更串行化；临界区只是一次 map 读写，开销可忽略。
 */
@Mixin(value = UnderlayBakedModel.class, remap = false)
public abstract class UnderlayBakedModelMixin {

    @Redirect(
            method = "addUnderlayQuads",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object gt$syncFluidGet(Map<Object, Object> cache, Object key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Redirect(
            method = "addUnderlayQuads",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object gt$syncFluidPut(Map<Object, Object> cache, Object key, Object value) {
        synchronized (cache) {
            return cache.put(key, value);
        }
    }

    @Redirect(
            method = "addUnderlayQuads",
            at = @At(value = "INVOKE", target = "Ljava/util/IdentityHashMap;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object gt$syncUnderlayGet(IdentityHashMap<Object, Object> cache, Object key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Redirect(
            method = "addUnderlayQuads",
            at = @At(value = "INVOKE", target = "Ljava/util/IdentityHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object gt$syncUnderlayPut(IdentityHashMap<Object, Object> cache, Object key, Object value) {
        synchronized (cache) {
            return cache.put(key, value);
        }
    }
}

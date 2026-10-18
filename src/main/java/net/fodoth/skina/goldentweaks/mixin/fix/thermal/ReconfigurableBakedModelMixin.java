package net.fodoth.skina.goldentweaks.mixin.fix.thermal;

import cofh.thermal.core.client.renderer.model.ReconfigurableBakedModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 与 {@link UnderlayBakedModelMixin} 同因：{@code ReconfigurableBakedModel.getQuads} 里的
 * 静态 {@code SIDE_QUAD_CACHE}（fastutil {@code Int2ObjectOpenHashMap}）同样在 Sodium 的
 * 多线程区块烘焙路径上被并发读写，扩容时结构损坏会抛 {@code ArrayIndexOutOfBoundsException}。
 * 这里同样把 get/put 包进实例 monitor。
 */
@Mixin(value = ReconfigurableBakedModel.class, remap = false)
public abstract class ReconfigurableBakedModelMixin {

    @Redirect(
            method = "getQuads",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;get(I)Ljava/lang/Object;")
    )
    private Object gt$syncSideGet(Int2ObjectMap<Object> cache, int key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Redirect(
            method = "getQuads",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;")
    )
    private Object gt$syncSidePut(Int2ObjectMap<Object> cache, int key, Object value) {
        synchronized (cache) {
            return cache.put(key, value);
        }
    }
}

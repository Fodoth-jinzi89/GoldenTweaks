package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import hellfirepvp.astralsorcery.common.focal.node.FocalPointNode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Astral Sorcery 2.0.1 的 {@code FocalPointSyncData#getNodes} 返回的是
 * {@code Collections.unmodifiableList(levelData.loadedFocalPoints)} —— 一个仍然指向
 * 那份可变 {@link java.util.ArrayList} 的“只读视图”，而不是副本。
 *
 * <p>{@code FocalPointManager#onLevelTick} 直接对它的返回值做 {@code forEach} 并调用
 * {@code FocalPointNode#tick}：而 {@code BasicFocalPointNode#tick} 会在地表抹除植被
 * （{@code ServerLevel#setBlock}），这一步可能触发区块的加载/卸载；区块卸载会走
 * {@code ChunkEvent.Unload} → {@code FocalPointSyncData#unloadNodes} →
 * {@code loadedFocalPoints.remove(...)}，于是正在 {@code forEach} 的列表被结构性修改，
 * {@code ArrayList} 抛 {@link java.util.ConcurrentModificationException}，异常冲出
 * {@code LevelTickEvent.Post} → 整个服务器 tick 循环崩溃。</p>
 *
 * <p>上游 issue: HellFirePvP/AstralSorcery#2037。这里让 {@code getNodes} 返回快照，
 * 语义上与原只读视图一致（调用方只读遍历），但遍历期间增删节点不会再影响当前这一轮 tick。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.data.sync.server.FocalPointSyncData", remap = false)
public abstract class FocalPointSyncDataGetNodesMixin {

    @Inject(method = "getNodes", at = @At("RETURN"), cancellable = true, remap = false)
    private void gt$returnNodeSnapshot(ResourceKey<Level> levelKey, CallbackInfoReturnable<List<FocalPointNode>> cir) {
        List<FocalPointNode> nodes = cir.getReturnValue();
        if (nodes != null && !nodes.isEmpty()) {
            cir.setReturnValue(new ArrayList<>(nodes));
        }
    }
}

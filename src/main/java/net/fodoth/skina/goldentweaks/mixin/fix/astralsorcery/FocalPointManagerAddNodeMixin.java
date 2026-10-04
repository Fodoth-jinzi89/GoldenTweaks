package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataManager;
import hellfirepvp.astralsorcery.common.data.sync.server.FocalPointSyncData;
import hellfirepvp.astralsorcery.common.focal.node.FocalPointNode;
import hellfirepvp.astralsorcery.common.lib.types.SyncDataTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 降星点的“登记”和“激活”是两条互相竞速的路径，而激活那条几乎总是输：
 *
 * <ul>
 *     <li>登记：世界生成时 {@code FocalPointRegisterProcessor#finalizeProcessing} 通过
 *     {@code server.tell(new TickTask(0, ...))} 把节点加进持久化的 {@code FocalPointData}
 *     （此时 {@code levelGen instanceof ServerLevel} 为 false，所以 {@code loadNode = false}）。</li>
 *     <li>激活：{@code FocalPointManager#onChunkLoad} 监听 {@code ChunkEvent.Load}，只有当该区块
 *     对应的 {@code FocalPointData.Section} 里已经有节点时，才把节点塞进运行期的
 *     {@code FocalPointSyncData.loadedFocalPoints}。</li>
 * </ul>
 *
 * <p>问题在于：区块生成完就立刻被加载、{@code ChunkEvent.Load} 当场触发，而那个
 * {@code TickTask} 要等到服务器下一轮 tick 队列才会执行。于是新生成区块里的降星点永远只存在于
 * 持久数据里，从未进入 {@code loadedFocalPoints}：服务器不会 tick 它（不抹植被、夜晚不嬗变/合成），
 * 也不会同步给客户端（看不见光柱），一直要等到该区块被卸载再重新加载才会“复活”。</p>
 *
 * <p>这里在登记完成后补一刀：如果节点所在区块此刻已经加载，就顺手把它激活；
 * 若尚未加载，仍由原版 {@code onChunkLoad} 在区块加载时负责激活，两条路径互不重复
 * （{@code registerNode} 内部有去重判断）。</p>
 */
@Mixin(targets = "hellfirepvp.astralsorcery.common.focal.FocalPointManager", remap = false)
public abstract class FocalPointManagerAddNodeMixin {

    @Inject(method = "addNewNode", at = @At("TAIL"), remap = false)
    private void gt$activateNodeInLoadedChunk(ServerLevel level, FocalPointNode node, boolean loadNode, CallbackInfo ci) {
        if (loadNode) {
            return;
        }

        BlockPos pos = node.getPos().toBlockPos(0);
        if (!level.isLoaded(pos)) {
            return;
        }

        FocalPointSyncData data = (FocalPointSyncData) SyncDataManager.getInstance().getData(SyncDataTypesAS.FOCAL_POINT);
        if (data != null) {
            data.loadNode(level, node);
        }
    }
}

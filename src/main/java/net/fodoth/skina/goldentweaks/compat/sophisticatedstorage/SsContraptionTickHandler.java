package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import java.lang.ref.WeakReference;
import java.util.Map;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.network.packet.S2CSsStorageRenderPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

/**
 * 让移动结构上精致存储容器的可 tick 升级（漏斗/拾取/磁铁等）按容器当前世界坐标运行，
 * 并在内容变化时把服务端算好的渲染信息推给客户端 BE（装置上外观刷新的唯一写入方）。
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public class SsContraptionTickHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            Map<Integer, WeakReference<AbstractContraptionEntity>> contraptions =
                    ContraptionHandler.loadedContraptions.get(level);
            if (contraptions == null) {
                continue;
            }
            for (WeakReference<AbstractContraptionEntity> reference : contraptions.values()) {
                AbstractContraptionEntity entity = reference.get();
                if (entity == null) {
                    continue;
                }
                Contraption contraption = entity.getContraption();
                if (contraption == null) {
                    continue;
                }
                contraption.getStorage().getAllItemStorages().forEach((localPos, storage) -> {
                    if (!(storage instanceof SophisticatedStorageMountedStorage sophisticatedStorage)) {
                        return;
                    }
                    StorageBlockEntity shadow = sophisticatedStorage.getShadow();
                    if (shadow == null) {
                        sophisticatedStorage.ensureShadow(level.registryAccess());
                        shadow = sophisticatedStorage.getShadow();
                        if (shadow == null) {
                            return;
                        }
                    }
                    Vec3 globalPos = entity.toGlobalVector(Vec3.atCenterOf(localPos), 1.0F);
                    shadow.setLevel(level);
                    shadow.setBlockState(sophisticatedStorage.getState());
                    StorageBlockEntity.serverTick(level, BlockPos.containing(globalPos), shadow);

                    if (sophisticatedStorage.isRenderDirty()) {
                        sophisticatedStorage.markRenderClean();
                        CompoundTag renderInfo = sophisticatedStorage.buildRenderInfoTag();
                        if (!renderInfo.isEmpty()) {
                            PacketDistributor.sendToPlayersTrackingEntity(entity,
                                    new S2CSsStorageRenderPacket(entity.getId(), localPos, renderInfo));
                        }
                    }
                });
            }
        }
    }
}

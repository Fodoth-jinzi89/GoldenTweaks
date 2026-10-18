package net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.client;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import java.lang.ref.WeakReference;
import java.util.Map;
import net.createmod.catnip.data.Couple;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SophisticatedStorageMountedStorage;
import net.fodoth.skina.goldentweaks.network.packet.C2SSsContraptionAttackPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlock;

/** 移动结构上左键 SS 限类桶 → 取出物品（Create 对存储没有左键路径，这里补一条）。 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public class SsContraptionAttackHandler {

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        if (!ModList.get().isLoaded("create") || !ModList.get().isLoaded("sophisticatedstorage")) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || player.isSpectator()) {
            return;
        }
        Map<Integer, WeakReference<AbstractContraptionEntity>> contraptions =
                ContraptionHandler.loadedContraptions.get(minecraft.level);
        if (contraptions == null) {
            return;
        }
        Couple<Vec3> rayInputs = ContraptionHandlerClient.getRayInputs(player);
        Vec3 origin = rayInputs.getFirst();
        Vec3 target = rayInputs.getSecond();
        AABB bounds = new AABB(origin, target).inflate(16.0);

        AbstractContraptionEntity bestEntity = null;
        BlockHitResult bestResult = null;
        double bestDistance = Double.MAX_VALUE;
        for (WeakReference<AbstractContraptionEntity> reference : contraptions.values()) {
            AbstractContraptionEntity contraptionEntity = reference.get();
            if (contraptionEntity == null || !contraptionEntity.getBoundingBox().intersects(bounds)) {
                continue;
            }
            BlockHitResult result = ContraptionHandlerClient.rayTraceContraption(origin, target, contraptionEntity);
            if (result == null) {
                continue;
            }
            double distance = contraptionEntity.toGlobalVector(result.getLocation(), 1.0F).distanceTo(origin);
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestResult = result;
                bestEntity = contraptionEntity;
            }
        }
        if (bestEntity == null || bestResult == null) {
            return;
        }
        Contraption contraption = bestEntity.getContraption();
        if (contraption == null) {
            return;
        }
        StructureBlockInfo info = contraption.getBlocks().get(bestResult.getBlockPos());
        if (info == null || !(info.state().getBlock() instanceof LimitedBarrelBlock)) {
            return;
        }
        if (!(contraption.getStorage().getAllItemStorages().get(bestResult.getBlockPos())
                instanceof SophisticatedStorageMountedStorage)) {
            return;
        }
        PacketDistributor.sendToServer(new C2SSsContraptionAttackPacket(
                bestEntity.getId(), bestResult.getBlockPos(), bestResult.getDirection()));
        event.setCanceled(true);
        event.setSwingHand(true);
    }
}

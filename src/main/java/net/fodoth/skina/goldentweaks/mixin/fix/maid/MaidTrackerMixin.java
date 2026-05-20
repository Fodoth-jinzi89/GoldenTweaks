package net.fodoth.skina.goldentweaks.mixin.fix.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maidbeacon.event.MaidTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(value = MaidTracker.class, remap = false)
public class MaidTrackerMixin {

    @Unique
    private static final Queue<Runnable> GT_PENDING = new ConcurrentLinkedQueue<>();

    /**
     * 拦截 MaidTracker 原逻辑，避免在 entity leave 阶段遍历 world
     */
    @Inject(
            method = "onEntityLeave",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldenTweaks$onEntityLeave(EntityLeaveLevelEvent event, CallbackInfo ci) {

        if (event.getLevel().isClientSide()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof EntityMaid maid)) return;

        if (!(event.getLevel() instanceof ServerLevel level)) return;

        MinecraftServer server = level.getServer();

        // ✔ 延迟执行，避免 entity section manager 冲突
        final int maidId = maid.getId();

        GT_PENDING.add(() -> {
            ServerLevel safeLevel = server.getLevel(level.dimension());
            if (safeLevel == null) return;

            safeLevel.getEntities().getAll().forEach(e -> {
                if (e instanceof com.mastermarisa.maidbeacon.entity.ExtraRenderingEntity extra) {
                    if (extra.getOwnerID() == maidId) {
                        if (!e.isRemoved()) {
                            e.discard();
                        }
                    }
                }
            });
        });

        ci.cancel();
    }


    @Unique
    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Post event) {
        Runnable task;
        while ((task = GT_PENDING.poll()) != null) {
            try {
                task.run();
            } catch (Exception ignored) {
            }
        }
    }
}
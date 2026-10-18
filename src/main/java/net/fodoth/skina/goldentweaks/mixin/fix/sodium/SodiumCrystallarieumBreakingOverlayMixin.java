package net.fodoth.skina.goldentweaks.mixin.fix.sodium;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Sodium 在渲染方块实体时，会把「破坏进度」的裂纹（{@code SheetedDecalTextureGenerator}）一并套到方块实体几何上：
 *
 * <pre>
 * VertexConsumer transformer = new SheetedDecalTextureGenerator(bufferBuilder, entry, 1.0F);
 * consumer = layer -&gt; layer.affectsCrumbling() ? VertexMultiConsumer.create(transformer, immediate.getBuffer(layer)) : immediate.getBuffer(layer);
 * dispatcher.render(entity, tickDelta, matrices, consumer);
 * </pre>
 *
 * <p>Spectrum 结晶器上方的「魔法阵」（{@link de.dafuqs.spectrum.blocks.ink.sink.CrystallarieumBlockEntityRenderer}
 * 渲染的悬浮圆盘/回响环）正是方块实体几何，于是被挖时也被盖上裂纹。原版本只会给方块模型铺裂纹，不会动方块实体。</p>
 *
 * <p>这里对结晶器直接跳过这层裂纹装饰，用原始 buffer source 正常渲染它的方块实体。{@code targets} 用字符串、
 * {@code remap = false}、{@code require = 0}，Sodium 缺失或方法改名时静默回退（最多裂纹回到方块实体，不会崩）。</p>
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer", remap = false)
public abstract class SodiumCrystallarieumBreakingOverlayMixin {

    @Unique
    private static final ResourceLocation GT$CRYSTALLARIEUM = ResourceLocation.fromNamespaceAndPath("spectrum", "crystallarieum");

    @Inject(
            method = "renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/player/LocalPlayer;Lcom/llamalad7/mixinextras/sugar/ref/LocalBooleanRef;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void goldentweaks$skipBreakDecalForCrystallarieum(
            PoseStack matrices,
            RenderBuffers bufferBuilders,
            Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
            float tickDelta,
            MultiBufferSource.BufferSource immediate,
            double x,
            double y,
            double z,
            BlockEntityRenderDispatcher dispatcher,
            BlockEntity entity,
            LocalPlayer player,
            LocalBooleanRef isGlowing,
            CallbackInfo ci
    ) {
        if (!GT$CRYSTALLARIEUM.equals(BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock()))) {
            return;
        }

        BlockPos pos = entity.getBlockPos();
        matrices.pushPose();
        matrices.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
        dispatcher.render(entity, tickDelta, matrices, immediate);
        matrices.popPose();
        ci.cancel();
    }
}

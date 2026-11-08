package net.fodoth.skina.goldentweaks.compat.flavorimmerseddailyargo.client;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * flavor_immersed_daily_argo 的 waterwheel_water 流体只注册了 FluidType，没有注册客户端扩展，
 * 于是 {@link IClientFluidTypeExtensions} 落到默认实现：getStillTexture()/getFlowingTexture() 都返回
 * {@code null}，水面渲染成紫黑格（缺失纹理）。这里在客户端补一个扩展，让它直接套用原版水的
 * still/flow/overlay 贴图，并按生物群系取水色（与原版水一致）。
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID, value = Dist.CLIENT)
public final class WaterwheelWaterClientExtensions {

    private static final ResourceLocation FLUID_ID =
            ResourceLocation.fromNamespaceAndPath("flavor_immersed_daily_argo", "waterwheel_water");

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        if (!ModList.get().isLoaded("flavor_immersed_daily_argo")) {
            return;
        }

        FluidType fluidType = NeoForgeRegistries.FLUID_TYPES.get(FLUID_ID);
        if (fluidType == null) {
            return;
        }

        event.registerFluidType(new VanillaWaterExtension(), fluidType);
    }

    private WaterwheelWaterClientExtensions() {
    }

    /** 与原版水一致的贴图与颜色（原版水没有走 FluidType，故这里手动对齐）。 */
    private static final class VanillaWaterExtension implements IClientFluidTypeExtensions {

        private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
        private static final ResourceLocation FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
        private static final ResourceLocation OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
        private static final int DEFAULT_TINT = 0xFF3F76E4;

        @Override
        public ResourceLocation getStillTexture() {
            return STILL;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return FLOWING;
        }

        @Override
        public ResourceLocation getOverlayTexture() {
            return OVERLAY;
        }

        @Override
        public int getTintColor() {
            return DEFAULT_TINT;
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return BiomeColors.getAverageWaterColor(getter, pos) | 0xFF000000;
        }
    }
}

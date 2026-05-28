package net.fodoth.skina.goldentweaks.mixin.fix.cmpackagecouriers;

import com.kreidev.cmpackagecouriers.compat.Mods;
import com.kreidev.cmpackagecouriers.compat.create_factory_logistics.FactoryLogisticsCompat;
import com.kreidev.cmpackagecouriers.compat.create_factory_logistics.JarPlaneRenderer;
import com.kreidev.cmpackagecouriers.plane.CardboardPlaneItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CardboardPlaneItemRenderer.class, remap = false)
public class CardboardPlaneItemRendererMixin {

    /**
     * @author GoldenTweaks
     * @reason Prevent crashes caused by invalid/null baked partial models
     */
    @Overwrite
    public static void renderPlane(
            ItemStack box,
            PoseStack ms,
            MultiBufferSource buffer,
            int light
    ) {
        try {

            if (box.isEmpty() || !PackageItem.isPackage(box)) {
                box = PackageStyles.getDefaultBox();
            }

            ms.pushPose();

            ms.translate(0.0F, -0.25F, 0.0F);

            /*
             * Plane body
             */
            try {

                SuperByteBuffer planeBuffer =
                        CachedBuffers.partial(
                                CardboardPlaneItemRenderer.DELIVERY_PLANE,
                                Blocks.AIR.defaultBlockState()
                        );

                planeBuffer
                        .translate(-0.5F, 0.0F, -0.5F)
                        .light(light)
                        .renderInto(
                                ms,
                                buffer.getBuffer(RenderType.cutout())
                        );

            } catch (Throwable t) {

                GoldenTweaks.LOGGER.warn(
                        "Failed to render cardboard delivery plane model",
                        t
                );
            }

            ms.scale(
                    0.33333334F,
                    0.33333334F,
                    0.33333334F
            );

            /*
             * Factory Logistics jar renderer
             */
            if (
                    Mods.CRATE_FACTORY_LOGISTICS.isLoaded()
                            && FactoryLogisticsCompat.isJar(box)
            ) {

                try {

                    JarPlaneRenderer.renderJar(
                            box,
                            ms,
                            buffer,
                            light
                    );

                } catch (Throwable t) {

                    GoldenTweaks.LOGGER.warn(
                            "Failed to render jar plane",
                            t
                    );
                }

            } else {

                /*
                 * Package model
                 */
                PartialModel model =
                        AllPartialModels.PACKAGES.get(
                                BuiltInRegistries.ITEM.getKey(
                                        box.getItem()
                                )
                        );

                if (model != null) {

                    try {

                        SuperByteBuffer packageBuffer =
                                CachedBuffers.partial(
                                        model,
                                        Blocks.AIR.defaultBlockState()
                                );

                        packageBuffer
                                .translate(
                                        -0.5D,
                                        -PackageItem.getHeight(box),
                                        -1.0D
                                )
                                .rotateCentered(
                                        -AngleHelper.rad(90.0D),
                                        Direction.UP
                                )
                                .light(light)
                                .renderInto(
                                        ms,
                                        buffer.getBuffer(RenderType.cutout())
                                );

                    } catch (Throwable t) {

                        GoldenTweaks.LOGGER.warn(
                                "Failed to render package partial model for {}",
                                BuiltInRegistries.ITEM.getKey(
                                        box.getItem()
                                ),
                                t
                        );
                    }
                }

                /*
                 * Rope model
                 */
                PartialModel rope =
                        CardboardPlaneItemRenderer.PACKAGE_ROPE.get(
                                BuiltInRegistries.ITEM.getKey(
                                        box.getItem()
                                )
                        );

                if (rope != null) {

                    try {

                        SuperByteBuffer ropeBuffer =
                                CachedBuffers.partial(
                                        rope,
                                        Blocks.AIR.defaultBlockState()
                                );

                        ropeBuffer
                                .translate(
                                        -0.5D,
                                        -PackageItem.getHeight(box),
                                        -1.0D
                                )
                                .rotateCentered(
                                        -AngleHelper.rad(90.0D),
                                        Direction.UP
                                )
                                .light(light)
                                .renderInto(
                                        ms,
                                        buffer.getBuffer(RenderType.cutout())
                                );

                    } catch (Throwable t) {

                        GoldenTweaks.LOGGER.warn(
                                "Failed to render rope partial model for {}",
                                BuiltInRegistries.ITEM.getKey(
                                        box.getItem()
                                ),
                                t
                        );
                    }
                }
            }

            ms.popPose();

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "Suppressed cardboard plane renderer crash",
                    t
            );
        }
    }
}
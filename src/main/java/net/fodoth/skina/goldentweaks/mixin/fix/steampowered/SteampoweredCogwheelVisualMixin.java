package net.fodoth.skina.goldentweaks.mixin.fix.steampowered;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create: Steam Powered 的金属齿轮（bronze / cast_iron / steel 的 cogwheel 与 large_cogwheel）在方块
 * 形态下只显示杆子、不显示轮盘。
 *
 * 原因：steampowered 把这颗方块实体的 Flywheel 视觉注册成了 Create 的
 * SingleAxisRotatingVisual.shaft——该方法只取 AllPartialModels.SHAFT 杆模型；而 Create 官方齿轮使用
 * BracketedKineticBlockEntityVisual（轴与轮盘分开的 partial 模型）。物品栏与放置预览走原版烘焙模型
 * （blockstate -> steampowered:block/*_cogwheel，含轴 + 盘面），所以看起来正常；方块形态走 Flywheel
 * 视觉，轮盘就丢了。
 *
 * 修复：当 shaft() 被用于 steampowered 的齿轮方块时，改用该方块自身的烘焙模型作为旋转模型。
 * 该模型含轴 + 盘面，结构与 Create 官方 cogwheel/large_cogwheel 的 partial 一致；轴元素沿旋转轴
 * 径向对称，随盘一起旋转不影响观感。
 */
@Mixin(SingleAxisRotatingVisual.class)
public class SteampoweredCogwheelVisualMixin {

    @Inject(method = "shaft", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$renderCogwheelWheel(
            VisualizationContext context,
            KineticBlockEntity blockEntity,
            float partialTick,
            CallbackInfoReturnable<SingleAxisRotatingVisual<KineticBlockEntity>> cir) {

        Block block = blockEntity.getBlockState().getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (blockId == null
                || !"steampowered".equals(blockId.getNamespace())
                || !blockId.getPath().endsWith("cogwheel")) {
            return;
        }

        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(
                blockId.getNamespace(), "block/" + blockId.getPath());

        cir.setReturnValue(new SingleAxisRotatingVisual<>(
                context, blockEntity, partialTick, Models.partial(PartialModel.of(modelId))));
    }
}

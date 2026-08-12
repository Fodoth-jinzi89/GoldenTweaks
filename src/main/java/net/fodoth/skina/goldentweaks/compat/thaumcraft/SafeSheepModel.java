package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/**
 * 安全的羊模型，用于渲染非 {@code Sheep} 的实体（如 Thaumcraft 的 TaintSheepEntity）。
 *
 * <p>使用与 {@link net.minecraft.client.model.SheepModel} 完全相同的 {@link ModelPart} 层级结构
 * （通过 {@link net.minecraft.client.model.geom.ModelLayers#SHEEP} 获取），
 * 因此 OptiFine CEM / ETF / FreshAnimations 等资源包可正确识别并替换模型。</p>
 *
 * <p>与 {@code SheepModel} 的关键区别：
 * <ul>
 *   <li>泛型参数为 {@code LivingEntity} 而非 {@code Sheep}</li>
 *   <li>不覆盖 {@code prepareMobModel}，避免桥接方法将实体强转为 {@code Sheep} 引发
 *       {@link ClassCastException}</li>
 * </ul>
 * </p>
 */
public class SafeSheepModel extends QuadrupedModel<LivingEntity> {

    /**
     * 使用与 {@code SheepModel} 相同的骨骼参数构建四足动物模型。
     *
     * @param root 使用 {@link net.minecraft.client.model.geom.ModelLayers#SHEEP} 烘焙的 ModelPart
     */
    public SafeSheepModel(ModelPart root) {
        super(root, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
    }
}

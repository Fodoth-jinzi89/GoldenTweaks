package net.fodoth.skina.goldentweaks.mixin.fix.jaopca;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thelm.jaopca.compat.create.CreateHelper;
import thelm.jaopca.compat.create.CreateCompatModule;

/**
 * JAOPCA 的 Create 兼容模块会给每个锭类材料自动生成「锭 → 板」的压板配方，
 * 产物写的是 {@code c:plates/<材料>} 这个<b>标签</b>而不是具体物品，
 * 于是 {@code jaopca:create.material_to_plate.super_iridium} 会按标签解析，
 * 无法保证产出超级铱合金压板。
 *
 * <p>datapack 的 {@code neoforge:conditions} 在 reload 的 prepare 阶段就被过滤，
 * 而 JAOPCA 在 apply 阶段用 {@code putIfAbsent} 注入配方，所以放同 ID 的占位 JSON 挡不住它。
 * 这里直接在注册调用处跳过该材料，其余材料仍走原逻辑。</p>
 */
@Mixin(value = CreateCompatModule.class, remap = false)
public abstract class JaopcaCreatePlateRecipeMixin {

    @Unique
    private static final String GT_BLOCKED_MATERIAL = "super_iridium";

    @Redirect(
            method = "onCommonSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Lthelm/jaopca/compat/create/CreateHelper;registerPressingRecipe(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;Ljava/lang/Object;I)Z"))
    private boolean goldentweaks$skipSuperIridiumPlate(
            CreateHelper helper, ResourceLocation key, Object input, Object output, int outputCount) {
        if (key != null && GT_BLOCKED_MATERIAL.equals(key.getPath().substring(key.getPath().lastIndexOf('.') + 1))) {
            return false;
        }
        return helper.registerPressingRecipe(key, input, output, outputCount);
    }
}

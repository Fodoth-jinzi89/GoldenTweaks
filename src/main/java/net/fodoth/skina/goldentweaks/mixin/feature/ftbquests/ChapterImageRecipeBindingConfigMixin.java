package net.fodoth.skina.goldentweaks.mixin.feature.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageRecipeBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在「编辑图片」的配置界面里补两个输入框：绑定来源 id / 绑定用途 id（都填 EMI 的配方 id，形如
 * {@code namespace:path}）。填了之后按 R/U 会优先跳转到这个配方，找不到再退回按物品列配方。
 *
 * <p>单独一个 client mixin：{@code ChapterImage#fillConfigGroup} 本身是 {@code @OnlyIn(CLIENT)}，
 * 而字段与存盘逻辑在 {@code ChapterImageRecipeBindingMixin}（common）。</p>
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.quest.ChapterImage", remap = false)
public abstract class ChapterImageRecipeBindingConfigMixin {

    @Inject(method = "fillConfigGroup", at = @At("TAIL"), remap = false)
    private void gt$addRecipeBindingConfigs(ConfigGroup config, CallbackInfo ci) {
        GTQuestImageRecipeBinding binding = (GTQuestImageRecipeBinding) (Object) this;

        config.addString("gt_recipe_id", binding.goldentweaks$imageRecipeId(),
                        binding::goldentweaks$setImageRecipeId, "")
                .setNameKey("goldentweaks.image.recipe_id");

        config.addString("gt_uses_id", binding.goldentweaks$imageUsesId(),
                        binding::goldentweaks$setImageUsesId, "")
                .setNameKey("goldentweaks.image.uses_id");
    }
}

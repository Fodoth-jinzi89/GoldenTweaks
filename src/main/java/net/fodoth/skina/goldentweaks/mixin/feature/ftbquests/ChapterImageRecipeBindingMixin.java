package net.fodoth.skina.goldentweaks.mixin.feature.ftbquests;

import net.fodoth.skina.goldentweaks.compat.ftbquests.client.GTQuestImageRecipeBinding;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给 {@code ChapterImage} 加两个「绑定配方 id」字段（来源 / 用途），并让它随存档与网络同步一起走。
 *
 * <p>FTB 的 {@code readData}/{@code writeData} 只认自己已知的键，未知键不读也不写，所以必须在
 * 这两个方法里显式读写；同时 {@code readNetData}/{@code writeNetData} 也要带上，否则编辑保存、
 * 单机进游戏时客户端拿不到这两个值（R/U 是在客户端处理的）。</p>
 *
 * <p>这里只放字段和读写（common，专用服务器也用得上）；配置界面在
 * {@code ChapterImageRecipeBindingConfigMixin}（client）。</p>
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.quest.ChapterImage", remap = false)
public abstract class ChapterImageRecipeBindingMixin implements GTQuestImageRecipeBinding {

    private static final String GT_RECIPE_ID_KEY = "gt_recipe_id";
    private static final String GT_USES_ID_KEY = "gt_uses_id";

    @Unique
    private String gt$recipeId = "";

    @Unique
    private String gt$usesId = "";

    @Override
    public String goldentweaks$imageRecipeId() {
        return this.gt$recipeId == null ? "" : this.gt$recipeId;
    }

    @Override
    public String goldentweaks$imageUsesId() {
        return this.gt$usesId == null ? "" : this.gt$usesId;
    }

    @Override
    public void goldentweaks$setImageRecipeId(String recipeId) {
        this.gt$recipeId = recipeId == null ? "" : recipeId;
    }

    @Override
    public void goldentweaks$setImageUsesId(String usesId) {
        this.gt$usesId = usesId == null ? "" : usesId;
    }

    @Inject(method = "writeData", at = @At("TAIL"), remap = false)
    private void gt$writeRecipeBindings(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        if (!this.goldentweaks$imageRecipeId().isEmpty()) {
            nbt.putString(GT_RECIPE_ID_KEY, this.goldentweaks$imageRecipeId());
        }
        if (!this.goldentweaks$imageUsesId().isEmpty()) {
            nbt.putString(GT_USES_ID_KEY, this.goldentweaks$imageUsesId());
        }
    }

    @Inject(method = "readData", at = @At("TAIL"), remap = false)
    private void gt$readRecipeBindings(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo ci) {
        this.goldentweaks$setImageRecipeId(nbt.getString(GT_RECIPE_ID_KEY));
        this.goldentweaks$setImageUsesId(nbt.getString(GT_USES_ID_KEY));
    }

    @Inject(method = "writeNetData", at = @At("TAIL"), remap = false)
    private void gt$writeNetRecipeBindings(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        // 两个字段一定要都写，读取端才不会错位
        buffer.writeUtf(this.goldentweaks$imageRecipeId());
        buffer.writeUtf(this.goldentweaks$imageUsesId());
    }

    @Inject(method = "readNetData", at = @At("TAIL"), remap = false)
    private void gt$readNetRecipeBindings(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        this.goldentweaks$setImageRecipeId(buffer.readUtf());
        this.goldentweaks$setImageUsesId(buffer.readUtf());
    }
}

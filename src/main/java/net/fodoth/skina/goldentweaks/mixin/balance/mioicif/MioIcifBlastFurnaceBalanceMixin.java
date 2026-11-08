package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_blast_furnace;
import com.miophas.singularity_iteration.common.recipe.blast_furnace.mio_icif_BlastFurnaceRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

/**
 * 基础高炉平衡：热量缓冲从 50,000 HU 缩到 5,000 HU（保留 100 HU 的进热余量，实际容量 5,100），
 * 并按配方原时长把基础高炉的加工时间压到：
 * 2400t → 320t、6000t → 400t、1000t → 640t、750t → 160t。
 *
 * <p>只改基础高炉。进阶高炉直接读取配方时长，不受这里的 {@link Redirect} 影响。</p>
 */
@Mixin(value = mio_icif_blast_furnace.class, remap = false)
public abstract class MioIcifBlastFurnaceBalanceMixin {

    @Unique
    private static final long GT_HEAT_THRESHOLD = 5000L;
    @Unique
    private static final long GT_HEAT_CAPACITY = 5100L;

    @Shadow
    private int progress;

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntityType;)V",
            constant = @Constant(longValue = 50100L))
    private static long gt$heatCapacity(long original) {
        return GT_HEAT_CAPACITY;
    }

    @ModifyConstant(method = "receiveHeat", constant = @Constant(longValue = 50000L))
    private long gt$receiveThreshold(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "receiveHeat", constant = @Constant(longValue = 50100L))
    private long gt$receiveCapacity(long original) {
        return GT_HEAT_CAPACITY;
    }

    @ModifyConstant(method = "pullHeat", constant = @Constant(longValue = 50000L))
    private long gt$pullThreshold(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "pullHeat", constant = @Constant(longValue = 50100L))
    private long gt$pullCapacity(long original) {
        return GT_HEAT_CAPACITY;
    }

    @ModifyConstant(method = "isHot", constant = @Constant(longValue = 50000L))
    private long gt$hotThreshold(long original) {
        return GT_HEAT_THRESHOLD;
    }

    @ModifyConstant(method = "getHeatCapacity", constant = @Constant(intValue = 50000))
    private int gt$displayedCapacity(int original) {
        return (int) GT_HEAT_THRESHOLD;
    }

    @Redirect(
            method = "process",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/common/recipe/blast_furnace/mio_icif_BlastFurnaceRecipe;getDuration()I"))
    private int gt$balancedDuration(mio_icif_BlastFurnaceRecipe recipe) {
        return gt$scaledDuration(recipe.getDuration());
    }

    @Unique
    private static int gt$scaledDuration(int duration) {
        return switch (duration) {
            case 2400 -> 320;
            case 6000 -> 400;
            case 1000 -> 640;
            case 750 -> 160;
            default -> duration;
        };
    }

    /** 旧存档里进度是按原时长记的，新时长更短时直接从头开始，避免卡在“进度超过配方时长”。 */
    @Inject(method = "process", at = @At("HEAD"))
    private void gt$resetOversizedProgress(RecipeHolder<mio_icif_BlastFurnaceRecipe> holder, CallbackInfo ci) {
        if (holder != null && this.progress >= gt$scaledDuration(holder.value().getDuration())) {
            this.progress = 0;
        }
    }

    /** 旧存档的热量可能高于新容量，读档后夹到新容量，避免长期带着 50,000 HU 运行。 */
    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void gt$clampLegacyHeat(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        mio_icif_blast_furnace self = (mio_icif_blast_furnace) (Object) this;
        if (self.getHeatStored() > GT_HEAT_CAPACITY) {
            self.setHeat(GT_HEAT_CAPACITY);
        }
    }
}

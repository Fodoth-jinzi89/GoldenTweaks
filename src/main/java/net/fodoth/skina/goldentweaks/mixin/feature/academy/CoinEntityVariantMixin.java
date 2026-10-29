package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.world.entity.CoinEntity;
import net.fodoth.skina.goldentweaks.compat.academy.GTCoinVariant;
import net.fodoth.skina.goldentweaks.compat.academy.GTThermalRailgunCoins;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给学院抛出的硬币实体加一个「来源硬币」的同步字段（见 {@link GTCoinVariant}），并让硬币落回时
 * 退回原本抛出的那枚硬币（热力硬币）而不是学院币。
 */
@Mixin(CoinEntity.class)
public abstract class CoinEntityVariantMixin implements GTCoinVariant {

    @Unique
    private static final EntityDataAccessor<String> gt$COIN_VARIANT =
            SynchedEntityData.defineId(CoinEntity.class, EntityDataSerializers.STRING);

    @Unique
    private static final String gt$NBT_KEY = "GoldenTweaksCoinVariant";

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void gt$defineVariant(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(gt$COIN_VARIANT, "");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void gt$saveVariant(CompoundTag tag, CallbackInfo ci) {
        String variant = this.gt$getCoinVariant();
        if (!variant.isEmpty()) {
            tag.putString(gt$NBT_KEY, variant);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void gt$readVariant(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(gt$NBT_KEY)) {
            this.gt$setCoinVariant(tag.getString(gt$NBT_KEY));
        }
    }

    @ModifyArg(method = "settleCoin",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;)V"))
    private ItemLike gt$refundVariant(ItemLike original) {
        ItemStack refund = GTThermalRailgunCoins.stackForVariant(this.gt$getCoinVariant());
        return refund.isEmpty() ? original : refund.getItem();
    }

    @Override
    public String gt$getCoinVariant() {
        return this.gt$data().get(gt$COIN_VARIANT);
    }

    @Override
    public void gt$setCoinVariant(String variant) {
        this.gt$data().set(gt$COIN_VARIANT, variant == null ? "" : variant);
    }

    @Unique
    private SynchedEntityData gt$data() {
        return ((Entity) (Object) this).getEntityData();
    }
}

package net.fodoth.skina.goldentweaks.mixin.feature.academy;

import com.mohistmc.academy.entity.RailgunBeamEntity;
import net.fodoth.skina.goldentweaks.compat.academy.GTBeamScale;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给超电磁炮光束实体加一个「粗细倍率」的同步字段（见 {@link GTBeamScale}），供客户端渲染按倍率缩放光束直径。
 */
@Mixin(RailgunBeamEntity.class)
public abstract class RailgunBeamScaleMixin implements GTBeamScale {

    @Unique
    private static final EntityDataAccessor<Float> gt$BEAM_SCALE =
            SynchedEntityData.defineId(RailgunBeamEntity.class, EntityDataSerializers.FLOAT);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void gt$defineScale(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(gt$BEAM_SCALE, 1.0F);
    }

    @Override
    public float gt$getBeamScale() {
        return this.gt$data().get(gt$BEAM_SCALE);
    }

    @Override
    public void gt$setBeamScale(float scale) {
        this.gt$data().set(gt$BEAM_SCALE, scale);
    }

    @Unique
    private SynchedEntityData gt$data() {
        return ((Entity) (Object) this).getEntityData();
    }
}

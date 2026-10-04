package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import net.fodoth.skina.goldentweaks.compat.traveloptics.TravelOpticsSummonProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 traveloptics 召唤物设置召唤者时记录 召唤物 -> 召唤者。
 *
 * <p>部分召唤物的 {@code getSummoner()} 会返回 null，直接读取该方法拿不到主人；
 * 但所有召唤物在构造（{@code SummonedXxx(Level, LivingEntity)}）时都会调用
 * {@code setSummoner}，其参数即为真正的召唤者，因此在这里记录最可靠。</p>
 */
@Mixin(targets = {
        "com.gametechbc.traveloptics.entity.summons.SummonedAptrgangr",
        "com.gametechbc.traveloptics.entity.summons.SummonedDraugr",
        "com.gametechbc.traveloptics.entity.summons.SummonedEliteDraugr",
        "com.gametechbc.traveloptics.entity.summons.SummonedEnderGolem",
        "com.gametechbc.traveloptics.entity.summons.SummonedIgnitedBerserker",
        "com.gametechbc.traveloptics.entity.summons.SummonedIgnitedRevenant",
        "com.gametechbc.traveloptics.entity.summons.SummonedKobolediator",
        "com.gametechbc.traveloptics.entity.summons.SummonedKoboleton",
        "com.gametechbc.traveloptics.entity.summons.SummonedRoyalDraugr",
        "com.gametechbc.traveloptics.entity.summons.SummonedTheProwler",
        "com.gametechbc.traveloptics.entity.summons.SummonedTheWatcher",
        "com.gametechbc.traveloptics.entity.summons.SummonedWadjet"
}, remap = false)
public abstract class SummonedOwnerCaptureMixin {

    @Inject(
            method = "setSummoner",
            at = @At("HEAD")
    )
    private void goldentweaks$captureSummoner(LivingEntity owner, CallbackInfo ci) {
        TravelOpticsSummonProtection.captureOwner((Entity) (Object) this, owner);
        TravelOpticsSummonProtection.applyIntendedStats((LivingEntity) (Object) this);
    }
}

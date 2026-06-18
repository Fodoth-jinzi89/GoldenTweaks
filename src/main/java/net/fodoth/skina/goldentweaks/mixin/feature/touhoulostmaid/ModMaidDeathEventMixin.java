package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.qichensn.data.LostMaidData;
import com.github.qichensn.data.ModDataAttachment;
import com.github.qichensn.data.TamedLostMaidCountAttachment;
import com.github.qichensn.event.ModMaidDeathEvent;
import com.github.qichensn.util.ItemUtil;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidDeathEvent;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskIdle;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.fodoth.skina.goldentweaks.compat.touhoulittlemaid.GoldenTweaksLostMaidData;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(ModMaidDeathEvent.class)
public class ModMaidDeathEventMixin {

    @Inject(
            method = "onDeath",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldenTweaks$onDeathRewrite(MaidDeathEvent event, CallbackInfo ci) {

        if (event == null) return;

        if (!GoldenTweaksCommonConfig.LOST_MAID_DROP.getAsBoolean()) return;

        EntityMaid maid = event.getMaid();

        ItemUtil.deleteBannedItems(maid);

        ci.cancel();

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Player owner = goldentweaks$resolveOwner(attacker);

        if (owner == null || owner.isFakePlayer()) return;

        if (owner.level().random.nextFloat() > GoldenTweaksCommonConfig.LOST_MAID_DROP_CHANCE.getAsDouble()) return;

        if (maid.getOrCreateData(LostMaidData.IS_LOST_MAID, false)) {

                maid.setOwnerUUID(owner.getUUID());
                maid.setData(LostMaidData.IS_LOST_MAID, false);
                maid.getOrCreateData(GoldenTweaksLostMaidData.TAMED_LOST_MAID, true);

                Optional<IMaidTask> task = TaskManager.findTask(TaskIdle.UID);
                task.ifPresent(maid::setTask);

                TamedLostMaidCountAttachment tamedCount =
                        owner.getData(ModDataAttachment.TAMED_COUNT);

                if (tamedCount.canAdd()) {
                    tamedCount.add();
                } else {
                    maid.setData(LostMaidData.NOT_DROP_FILM, true);
                }

        }
        owner.giveExperiencePoints(5);
    }

    @Unique
    private static Player goldentweaks$resolveOwner(Entity attacker) {

        if (attacker == null) return null;

        if (ModList.get().isLoaded("alshanex_familiars")
                && attacker instanceof AbstractSpellCastingPet pet) {

            UUID uuid = pet.getOwnerUUID();
            if (uuid != null) {
                return pet.level().getPlayerByUUID(uuid);
            }
        }

        if (attacker instanceof Projectile projectile) {

            Entity owner = projectile.getOwner();

            if (owner != null) {
                return goldentweaks$resolveOwner(owner);
            }

            Entity e = projectile.getOwner();
            if (e != null) {
                if (e instanceof Player player) {
                    return player;
                }
            }

            return null;
        }

        if (attacker instanceof TamableAnimal tamable) {

            if (!tamable.isTame()) return null;

            Entity owner = tamable.getOwner();
            if (owner instanceof Player player) {
                return player;
            }
        }

        if (attacker instanceof Player player) {
            if (player.isFakePlayer()) return null;
            return player;
        }

        return null;
    }
}
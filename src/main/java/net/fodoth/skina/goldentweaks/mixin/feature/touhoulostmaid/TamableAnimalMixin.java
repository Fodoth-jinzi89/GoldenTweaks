package net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid;

import com.github.qichensn.data.LostMaidData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin {

    @Redirect(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void goldenTweaks$cancelDeathMessage(LivingEntity instance, Component component) {

        TamableAnimal animal = (TamableAnimal)(Object)this;

        // ✔ 迷失女仆：不发送死亡信息
        if (animal instanceof EntityMaid maid) {
            if (maid.getOrCreateData(LostMaidData.IS_LOST_MAID, false)) {
                return;
            }
        }

        Component deathMessage = animal.getCombatTracker().getDeathMessage();
        animal.sendSystemMessage(deathMessage);
    }
}

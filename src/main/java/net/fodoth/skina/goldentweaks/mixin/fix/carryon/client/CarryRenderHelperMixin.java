package net.fodoth.skina.goldentweaks.mixin.fix.carryon.client;

import net.fodoth.skina.goldentweaks.compat.carryon.client.CarryOnCarriedEntityCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tschipp.carryon.client.render.CarryRenderHelper;
import tschipp.carryon.common.carry.CarryOnData;

/**
 * Returns the same stand-in entity for as long as Carry On holds the same creature, see
 * {@link CarryOnCarriedEntityCache}.
 *
 * <p>{@code CarryRenderHelper#getRenderEntity} is the single place the carried entity is built, and Carry On
 * calls it from every render pass ({@code CarriedObjectRender#drawFirstPersonEntity},
 * {@code CarriedObjectRender#drawThirdPerson} and its own size maths). Recreating the entity there every frame
 * throws away all of its state, which is what the carried creature twitches from.</p>
 */
@Mixin(value = CarryRenderHelper.class, remap = false)
public class CarryRenderHelperMixin {

    @Redirect(
            method = "getRenderEntity(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/Entity;",
            at = @At(
                    value = "INVOKE",
                    target = "Ltschipp/carryon/common/carry/CarryOnData;getEntity(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"
            ),
            remap = false
    )
    private static Entity gt$stableCarriedEntity(CarryOnData carryData, Level level) {
        return CarryOnCarriedEntityCache.entity(carryData, level);
    }
}

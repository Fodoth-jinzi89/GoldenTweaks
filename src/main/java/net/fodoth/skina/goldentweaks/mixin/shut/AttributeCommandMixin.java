package net.fodoth.skina.goldentweaks.mixin.shut;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.server.commands.AttributeCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AttributeCommand.class)
public class AttributeCommandMixin {

    /**
     * @author Fodoth
     * @reason Disable attribute missing throw
     */
    @Overwrite
    private static AttributeInstance getAttributeInstance(Entity entity, Holder<Attribute> attribute) {
        LivingEntity livingEntity = getLivingEntity(entity);

        if (livingEntity == null) {
            return null;
        }

        return livingEntity.getAttributes().getInstance(attribute);
    }

    /**
     * @author Fodoth
     * @reason Disable non-living entity throw
     */
    @Overwrite
    private static LivingEntity getLivingEntity(Entity target) {
        return target instanceof LivingEntity livingEntity
                ? livingEntity
                : null;
    }

    /**
     * @author Fodoth
     * @reason Disable attribute existence throw
     */
    @Overwrite
    private static LivingEntity getEntityWithAttribute(Entity entity, Holder<Attribute> attribute) {
        LivingEntity livingEntity = getLivingEntity(entity);

        if (livingEntity == null) {
            return null;
        }

        return livingEntity.getAttributes().hasAttribute(attribute)
                ? livingEntity
                : null;
    }
}
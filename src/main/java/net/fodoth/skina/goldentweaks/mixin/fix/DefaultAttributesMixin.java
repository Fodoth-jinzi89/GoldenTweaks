package net.fodoth.skina.goldentweaks.mixin.fix;

import com.google.common.collect.ImmutableMap;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mixin(DefaultAttributes.class)
public class DefaultAttributesMixin {

    @Shadow
    @Final
    @Mutable
    private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS;

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void goldentweaks$injectMissingAttributes(CallbackInfo ci) {

        try {

            Map<EntityType<? extends LivingEntity>, AttributeSupplier> fixed =
                    new HashMap<>(SUPPLIERS);

            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {

                if (type.getCategory() == MobCategory.MISC) {
                    continue;
                }

                if (!LivingEntity.class.isAssignableFrom(type.getBaseClass())) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                EntityType<? extends LivingEntity> livingType =
                        (EntityType<? extends LivingEntity>) type;

                if (fixed.containsKey(livingType)) {
                    continue;
                }

                AttributeSupplier supplier =
                        goldentweaks$createFallbackSupplier(livingType);

                fixed.put(livingType, supplier);

                GoldenTweaks.LOGGER.warn(
                        "Injected fallback attributes for entity: {}",
                        BuiltInRegistries.ENTITY_TYPE.getKey(livingType)
                );
            }

            SUPPLIERS = ImmutableMap.copyOf(fixed);

            goldentweaks$patchNeoForgeAttributesView();

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to inject fallback attributes",
                    t
            );
        }
    }

    @Unique
    private static AttributeSupplier goldentweaks$createFallbackSupplier(
            EntityType<? extends LivingEntity> type
    ) {

        AttributeSupplier.Builder builder;

        if (Monster.class.isAssignableFrom(type.getBaseClass())) {

            builder = Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.25D)
                    .add(Attributes.ATTACK_DAMAGE, 2.0D);

        } else if (Mob.class.isAssignableFrom(type.getBaseClass())) {

            builder = Mob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.25D);

        } else {

            builder = LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D);
        }

        return builder.build();
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static void goldentweaks$patchNeoForgeAttributesView() {

        try {

            Field field = CommonHooks.class.getDeclaredField("FORGE_ATTRIBUTES");

            field.setAccessible(true);

            Object obj = field.get(null);

            if (obj instanceof Map<?, ?> map) {

                ((Map<EntityType<? extends LivingEntity>, AttributeSupplier>) map)
                        .putAll(SUPPLIERS);

                GoldenTweaks.LOGGER.warn(
                        "Patched NeoForge attribute registry"
                );
            }

        } catch (Throwable t) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to patch NeoForge attribute registry",
                    t
            );
        }
    }
}
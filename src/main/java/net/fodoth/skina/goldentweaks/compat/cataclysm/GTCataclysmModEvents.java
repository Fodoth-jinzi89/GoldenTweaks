package net.fodoth.skina.goldentweaks.compat.cataclysm;

import com.github.L_Ender.cataclysm.init.ModEntities;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

public class GTCataclysmModEvents {

    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeModificationEvent event) {
        if (!GoldenTweaksCommonConfig.isCataclysmBalanced()) {
            return;
        }
        add(event, ModEntities.ENDER_GUARDIAN.get(), Attributes.ARMOR);
        add(event, ModEntities.ENDER_GUARDIAN.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.ENDER_GOLEM.get(), Attributes.ARMOR);
        add(event, ModEntities.ENDER_GOLEM.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.ENDERMAPTERA.get(), Attributes.ARMOR);
        add(event, ModEntities.ENDERMAPTERA.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.NETHERITE_MONSTROSITY.get(), Attributes.ARMOR);
        add(event, ModEntities.NETHERITE_MONSTROSITY.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.NETHERITE_MINISTROSITY.get(), Attributes.ARMOR);
        add(event, ModEntities.NETHERITE_MINISTROSITY.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.IGNIS.get(), Attributes.ARMOR);
        add(event, ModEntities.IGNIS.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.IGNITED_BERSERKER.get(), Attributes.ARMOR);
        add(event, ModEntities.IGNITED_BERSERKER.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.IGNITED_REVENANT.get(), Attributes.ARMOR);
        add(event, ModEntities.IGNITED_REVENANT.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.THE_HARBINGER.get(), Attributes.ARMOR);
        add(event, ModEntities.THE_HARBINGER.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.THE_PROWLER.get(), Attributes.ARMOR);
        add(event, ModEntities.THE_PROWLER.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.THE_WATCHER.get(), Attributes.ARMOR);
        add(event, ModEntities.THE_WATCHER.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.THE_LEVIATHAN.get(), Attributes.ARMOR);
        add(event, ModEntities.THE_LEVIATHAN.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.THE_BABY_LEVIATHAN.get(), Attributes.ARMOR);
        add(event, ModEntities.THE_BABY_LEVIATHAN.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.CORALSSUS.get(), Attributes.ARMOR);
        add(event, ModEntities.CORALSSUS.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.CORAL_GOLEM.get(), Attributes.ARMOR);
        add(event, ModEntities.CORAL_GOLEM.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DEEPLING_BRUTE.get(), Attributes.ARMOR);
        add(event, ModEntities.DEEPLING_BRUTE.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DEEPLING_PRIEST.get(), Attributes.ARMOR);
        add(event, ModEntities.DEEPLING_PRIEST.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DEEPLING_WARLOCK.get(), Attributes.ARMOR);
        add(event, ModEntities.DEEPLING_WARLOCK.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DEEPLING.get(), Attributes.ARMOR);
        add(event, ModEntities.DEEPLING.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.LIONFISH.get(), Attributes.ARMOR);
        add(event, ModEntities.LIONFISH.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.AMETHYST_CRAB.get(), Attributes.ARMOR);
        add(event, ModEntities.AMETHYST_CRAB.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.ANCIENT_REMNANT.get(), Attributes.ARMOR);
        add(event, ModEntities.ANCIENT_REMNANT.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.MODERN_REMNANT.get(), Attributes.ARMOR);
        add(event, ModEntities.MODERN_REMNANT.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.KOBOLEDIATOR.get(), Attributes.ARMOR);
        add(event, ModEntities.KOBOLEDIATOR.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.WADJET.get(), Attributes.ARMOR);
        add(event, ModEntities.WADJET.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.KOBOLETON.get(), Attributes.ARMOR);
        add(event, ModEntities.KOBOLETON.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.MALEDICTUS.get(), Attributes.ARMOR);
        add(event, ModEntities.MALEDICTUS.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.APTRGANGR.get(), Attributes.ARMOR);
        add(event, ModEntities.APTRGANGR.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.ELITE_DRAUGR.get(), Attributes.ARMOR);
        add(event, ModEntities.ELITE_DRAUGR.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.ROYAL_DRAUGR.get(), Attributes.ARMOR);
        add(event, ModEntities.ROYAL_DRAUGR.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DRAUGR.get(), Attributes.ARMOR);
        add(event, ModEntities.DRAUGR.get(), Attributes.ARMOR_TOUGHNESS);

        add(event, ModEntities.SCYLLA.get(), Attributes.ARMOR);
        add(event, ModEntities.SCYLLA.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.CLAWDIAN.get(), Attributes.ARMOR);
        add(event, ModEntities.CLAWDIAN.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.HIPPOCAMTUS.get(), Attributes.ARMOR);
        add(event, ModEntities.HIPPOCAMTUS.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.CINDARIA.get(), Attributes.ARMOR);
        add(event, ModEntities.CINDARIA.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.URCHINKIN.get(), Attributes.ARMOR);
        add(event, ModEntities.URCHINKIN.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.DROWNED_HOST.get(), Attributes.ARMOR);
        add(event, ModEntities.DROWNED_HOST.get(), Attributes.ARMOR_TOUGHNESS);
        add(event, ModEntities.SYMBIOCTO.get(), Attributes.ARMOR);
        add(event, ModEntities.SYMBIOCTO.get(), Attributes.ARMOR_TOUGHNESS);
    }

    @SuppressWarnings("unchecked")
    private static void add(
            EntityAttributeModificationEvent event,
            EntityType<?> rawType,
            Holder<Attribute> attribute
    ) {
        EntityType<? extends LivingEntity> type =
                (EntityType<? extends LivingEntity>) rawType;

        try {
            if (!event.has(type, attribute)) {
                event.add(type, attribute);

            }
        } catch (Throwable t) {
            // fallback：避免不同 NeoForge 版本 API 差异炸启动
            event.add(type, attribute);

            GoldenTweaks.LOGGER.warn(
                    "[GoldenTweaks] Forced add attribute {} -> {} (fallback path)",
                    attribute.value().getDescriptionId(),
                    EntityType.getKey(type)
            );
        }
    }
}
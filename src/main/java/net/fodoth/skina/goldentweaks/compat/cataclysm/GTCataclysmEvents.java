package net.fodoth.skina.goldentweaks.compat.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.*;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.Endermaptera_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.Koboleton_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.The_Watcher_Entity;
import com.github.L_Ender.cataclysm.entity.Deepling.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.AcropolisMonsters.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Aptrgangr_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Draugr_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Elite_Draugr_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Royal_Draugr_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.Pet.Modern_Remnant_Entity;
import com.github.L_Ender.cataclysm.entity.Pet.Netherite_Ministrosity_Entity;
import com.github.L_Ender.cataclysm.entity.Pet.The_Baby_Leviathan_Entity;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class GTCataclysmEvents {

    private static final ResourceLocation ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "cataclysm_armor_multiplier");

    private static final ResourceLocation TOUGHNESS_ID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "cataclysm_armor_toughness_bonus");

    private static final ResourceLocation MAX_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "cataclysm_max_health_bonus");

    private static final ResourceLocation ATTACK_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath("goldentweaks", "cataclysm_attack_damage_bonus");


    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {

        if (!GoldenTweaksCommonConfig.isCataclysmBalanced()) {
            return;
        }
        Entity entity = event.getEntity();

        if (entity instanceof Ender_Guardian_Entity) {
            applyArmorBuff(entity, 10.0, 40.0);
        }
        if (entity instanceof Ender_Golem_Entity) {
            applyArmorBuff(entity, 5.0, 15.0);
        }
        if (entity instanceof Endermaptera_Entity) {
            applyArmorBuff(entity, 5.0, 15.0);
            applyMaxHealthBuff(entity, 40.0);
            applyAttackDamageBuff(entity, 10.0);
        }

        if (entity instanceof Netherite_Monstrosity_Entity) {
            applyArmorBuff(entity, 10.0, 50.0);
            applyMaxHealthBuff(entity, 2.0);
        }
        if (entity instanceof Netherite_Ministrosity_Entity) {
            applyArmorBuff(entity, 20.0, 30.0);
            applyMaxHealthBuff(entity, 2.0);
        }

        if (entity instanceof Ignis_Entity) {
            applyArmorBuff(entity, 50.0, 15.0);
        }

        if (entity instanceof Ignited_Revenant_Entity) {
            applyArmorBuff(entity, 10.0, 10.0);
        }

        if (entity instanceof Ignited_Berserker_Entity) {
            applyArmorBuff(entity, 6.0, 5.0);
            applyMaxHealthBuff(entity, 8.0);
            applyAttackDamageBuff(entity, 6.0);
        }

        if (entity instanceof The_Harbinger_Entity) {
            applyArmorBuff(entity, 100.0, 100.0);
        }

        if (entity instanceof The_Prowler_Entity) {
            applyArmorBuff(entity, 40.0, 80.0);
        }

        if (entity instanceof The_Watcher_Entity) {
            applyArmorBuff(entity, 40.0, 40.0);
            applyMaxHealthBuff(entity, 60.0);
            applyAttackDamageBuff(entity, 10.0);
        }

        if (entity instanceof The_Leviathan_Entity) {
            applyArmorBuff(entity, 80.0, 80.0);
        }
        if (entity instanceof The_Baby_Leviathan_Entity) {
            applyArmorBuff(entity, 600.0, 60.0);
        }
        if (entity instanceof Coralssus_Entity) {
            applyArmorBuff(entity, 60.0, 40.0);
            applyMaxHealthBuff(entity, 300.0);
            applyAttackDamageBuff(entity, 25.0);
        }
        if (entity instanceof Coral_Golem_Entity) {
            applyArmorBuff(entity, 30.0, 20.0);
            applyMaxHealthBuff(entity, 100.0);
            applyAttackDamageBuff(entity, 10.0);
        }
        if (entity instanceof Deepling_Brute_Entity) {
            applyArmorBuff(entity, 10.0, 10.0);
            applyMaxHealthBuff(entity, 10.0);
            applyAttackDamageBuff(entity, 5.0);
        }
        if (entity instanceof Deepling_Priest_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            30.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 10.0);
            applyAttackDamageBuff(entity, 5.0);
        }
        if (entity instanceof Deepling_Warlock_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            30.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 10.0);
            applyAttackDamageBuff(entity, 5.0);
        }
        if (entity instanceof Deepling_Angler_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            5.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 4.0);
            applyAttackDamageBuff(entity, 3.0);
        }
        if (entity instanceof Deepling_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            12.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            4.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 3.0);
            applyAttackDamageBuff(entity, 2.0);
        }
        if (entity instanceof Lionfish_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            5.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            3.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 2.0);
            applyAttackDamageBuff(entity, 2.0);
        }

        if (entity instanceof Amethyst_Crab_Entity) {
            applyArmorBuff(entity, 100.0, 50.0);
        }

        if (entity instanceof Ancient_Remnant_Entity) {
            applyArmorBuff(entity, 20.0, 25.0);
        }
        if (entity instanceof Modern_Remnant_Entity) {
            applyArmorBuff(entity, 50.0, 40.0);
            applyAttackDamageBuff(entity, 5.0);
        }
        if (entity instanceof Kobolediator_Entity) {
            applyArmorBuff(entity, 10.0, 15.0);
        }
        if (entity instanceof Wadjet_Entity) {
            applyArmorBuff(entity, 15.0, 15.0);

        }
        if (entity instanceof Koboleton_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            5.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 2.0);
            applyAttackDamageBuff(entity, 2.5);
        }

        if (entity instanceof Maledictus_Entity) {
            applyArmorBuff(entity, 30.0, 30.0);
        }
        if (entity instanceof Aptrgangr_Entity) {
            applyArmorBuff(entity, 15.0, 15.0);
        }

        if (entity instanceof Elite_Draugr_Entity) {
            applyArmorBuff(entity, 20.0, 10.0);
            applyMaxHealthBuff(entity, 20.0);
            applyAttackDamageBuff(entity, 4.0);
        }
        if (entity instanceof Royal_Draugr_Entity) {
            applyArmorBuff(entity, 25.0, 5.0);
            applyMaxHealthBuff(entity, 25.0);
            applyAttackDamageBuff(entity, 2.0);
        }
        if (entity instanceof Draugr_Entity) {
            applyArmorBuff(entity, 15.0, 5.0);
            applyMaxHealthBuff(entity, 10.0);
            applyAttackDamageBuff(entity, 2.0);
        }

        if (entity instanceof Scylla_Entity) {
            applyArmorBuff(entity, 15.0, 25.0);
        }
        if (entity instanceof Clawdian_Entity) {
            applyArmorBuff(entity, 10.0, 15.0);
        }
        if (entity instanceof Hippocamtus_Entity) {
            applyArmorBuff(entity, 10.0, 10.0);
            applyMaxHealthBuff(entity, 15.0);
            applyAttackDamageBuff(entity, 4.0);
        }
        if (entity instanceof Cindaria_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            50.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            15.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 10.0);
            applyAttackDamageBuff(entity, 3.0);
        }
        if (entity instanceof Urchinkin_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            25.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            10.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 15.0);
            applyAttackDamageBuff(entity, 2.0);
        }
        if (entity instanceof Drowned_Host_Entity) {
            applyArmorBuff(entity, 5.0, 10.0);
            applyMaxHealthBuff(entity, 15.0);
            applyAttackDamageBuff(entity, 2.0);
        }
        if (entity instanceof Symbiocto_Entity) {
            addModifier(
                    entity,
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_ID,
                            35.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            addModifier(
                    entity,
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_ID,
                            10.0,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
            applyMaxHealthBuff(entity, 15.0);
            applyAttackDamageBuff(entity, 2.0);
        }

    }

    private static void applyArmorBuff(Entity entity, double armorMultiplier, double toughnessBonus) {

        addModifier(
                entity,
                Attributes.ARMOR,
                new AttributeModifier(
                        ARMOR_ID,
                        armorMultiplier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
        );

        addModifier(
                entity,
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        TOUGHNESS_ID,
                        toughnessBonus,
                        AttributeModifier.Operation.ADD_VALUE
                )
        );
    }

    /**
     * 增加最大生命值，并将当前生命值设置为新的最大生命值（满血）
     * @param entity 目标实体
     * @param healthMultiplier 增加的生命值数量
     */
    private static void applyMaxHealthBuff(Entity entity, double healthMultiplier) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        AttributeInstance instance = livingEntity.getAttributes().getInstance(Attributes.MAX_HEALTH);
        if (instance == null) {
            return;
        }

        // 检查是否已经添加过该修饰符
        if (instance.getModifier(MAX_HEALTH_ID) != null) {
            return;
        }

        // 添加最大生命值修饰符
        instance.addPermanentModifier(
                new AttributeModifier(
                        MAX_HEALTH_ID,
                        healthMultiplier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
        );

        // 将当前生命值设置为新的最大生命值（满血）
        livingEntity.setHealth(livingEntity.getMaxHealth());
    }

    /**
     * 增加攻击力
     * @param entity 目标实体
     * @param damageMultiplier 增加的攻击力数量
     */
    private static void applyAttackDamageBuff(Entity entity, double damageMultiplier) {
        addModifier(
                entity,
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        ATTACK_DAMAGE_ID,
                        damageMultiplier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
        );
    }

    private static void addModifier(
            Entity entity,
            Holder<Attribute> attribute,
            AttributeModifier modifier
    ) {
        if (! (entity instanceof LivingEntity livingEntity)) {
            return;
        }
        AttributeInstance instance = livingEntity.getAttributes().getInstance(attribute);

        if (instance == null) {
            return;
        }

        if (instance.getModifier(modifier.id()) != null) {
            return;
        }

        instance.addPermanentModifier(modifier);

    }
}
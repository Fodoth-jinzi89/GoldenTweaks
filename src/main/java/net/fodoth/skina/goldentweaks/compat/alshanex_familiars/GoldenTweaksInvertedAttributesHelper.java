package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.alshanex.familiarslib.data.PlayerFamiliarData;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.mixin.SchoolTypeAccessor;
import net.alshanex.familiarslib.registry.AttachmentRegistry;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.*;

public final class GoldenTweaksInvertedAttributesHelper {

    private static volatile Map<Holder<Attribute>, ResourceLocation>
            SHARED_ATTRIBUTES;

    private static volatile Map<Holder<Attribute>, ResourceLocation>
            SHARED_ATTRIBUTES_RESIST;

    private static final double EPSILON = 0.0001D;

    private static final ResourceLocation ARMOR_MODIFIER =
            id("inverted_familiar_armor");

    private static final ResourceLocation HEALTH_MODIFIER =
            id("inverted_familiar_health");

    private GoldenTweaksInvertedAttributesHelper() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID,
                path
        );
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    getSharedAttributes() {

        if (SHARED_ATTRIBUTES == null) {

            synchronized (GoldenTweaksInvertedAttributesHelper.class) {

                if (SHARED_ATTRIBUTES == null) {
                    SHARED_ATTRIBUTES =
                            buildSharedAttributesMap();
                }
            }
        }

        return SHARED_ATTRIBUTES;
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    getSharedAttributesResist() {

        if (SHARED_ATTRIBUTES_RESIST == null) {

            synchronized (GoldenTweaksInvertedAttributesHelper.class) {

                if (SHARED_ATTRIBUTES_RESIST == null) {
                    SHARED_ATTRIBUTES_RESIST =
                            buildSharedAttributesResistMap();
                }
            }
        }

        return SHARED_ATTRIBUTES_RESIST;
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    buildSharedAttributesMap() {

        Map<Holder<Attribute>, ResourceLocation> map =
                new LinkedHashMap<>();

        map.put(
                AttributeRegistry.SPELL_POWER,
                id("inverted_familiar_spell_power")
        );

        for (SchoolType school : SchoolRegistry.REGISTRY) {

            String name =
                    school.getId().getPath();

            Holder<Attribute> power =
                    ((SchoolTypeAccessor) school)
                            .getPowerAttribute();

            if (power != null) {

                map.put(
                        power,
                        id("inverted_familiar_" + name + "_spell_power")
                );
            }

        }

        return Collections.unmodifiableMap(map);
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    buildSharedAttributesResistMap() {

        Map<Holder<Attribute>, ResourceLocation> map =
                new LinkedHashMap<>();


        map.put(
                AttributeRegistry.SPELL_RESIST,
                id("inverted_familiar_spell_resist")
        );

        for (SchoolType school : SchoolRegistry.REGISTRY) {

            String name =
                    school.getId().getPath();

            Holder<Attribute> resist =
                    ((SchoolTypeAccessor) school)
                            .getResistanceAttribute();


            if (resist != null) {

                map.put(
                        resist,
                        id("inverted_familiar_" + name + "_magic_resist")
                );
            }
        }

        return Collections.unmodifiableMap(map);
    }

    public static void handleSpellbookChange(
            ServerPlayer player,
            boolean equipped
    ) {

        if (equipped) {
            recalculateAttributes(player);
        } else {
            removeAttributes(player);
        }
    }

    public static void handlePlayerTick(
            ServerPlayer player
    ) {

        recalculateAttributes(player);
    }

    public static void recalculateAttributes(
            ServerPlayer player
    ) {

        if (!GoldenTweaksFamiliarCurioHelper
                .isWearingInvertedFamiliarSpellbook(player)) {
            removeAttributes(player);
            return;
        }

        List<Double> armorValues =
                new ArrayList<>();

        List<Double> healthValues =
                new ArrayList<>();

        List<Double> spellPowerValues =
                new ArrayList<>();

        List<Double> spellResistValues =
                new ArrayList<>();

        for (AbstractSpellCastingPet familiar :
                getSummonedFamiliars(player)) {

            GoldenTweaksConsumableData data =
                    GoldenTweaksConsumableHelper.getData(familiar);

            armorValues.add(
                    sanitize(data.getArmor())
            );

            healthValues.add(
                    sanitize(data.getHealth())
            );

            spellPowerValues.add(
                    sanitize(data.getSpellPower())
            );

            spellResistValues.add(
                    sanitize(data.getSpellResist())
            );
        }

        double armor =
                calculateBonus(armorValues);

        double health =
                calculateBonus(healthValues);

        double spellPower =
                calculateBonus(spellPowerValues);

        double spellResist =
                calculateBonus(spellResistValues);



        updateModifier(
                player,
                Attributes.ARMOR,
                ARMOR_MODIFIER,
                armor,
                AttributeModifier.Operation.ADD_VALUE
        );

        updateModifier(
                player,
                Attributes.MAX_HEALTH,
                HEALTH_MODIFIER,
                health / 25D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        for (var entry :
                getSharedAttributes().entrySet()) {

            Holder<Attribute> attribute =
                    entry.getKey();

            ResourceLocation modifierId =
                    entry.getValue();

            AttributeInstance playerAttr =
                    player.getAttribute(attribute);

            if (playerAttr == null) {
                continue;
            }

            updateModifier(
                    player,
                    attribute,
                    modifierId,
                    spellPower / 25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );

        }

        for (var entry :
                getSharedAttributesResist().entrySet()) {

            Holder<Attribute> attribute =
                    entry.getKey();

            ResourceLocation modifierId =
                    entry.getValue();

            AttributeInstance playerAttr =
                    player.getAttribute(attribute);

            if (playerAttr == null) {
                continue;
            }

            updateModifier(
                    player,
                    attribute,
                    modifierId,
                    spellResist / 25D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );

        }

    }

    private static void updateModifier(
            ServerPlayer player,
            Holder<Attribute> attribute,
            ResourceLocation modifierId,
            double amount,
            AttributeModifier.Operation operation
    ) {

        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        AttributeModifier existing =
                instance.getModifier(modifierId);

        if (amount <= 0D) {

            if (existing != null) {
                instance.removeModifier(modifierId);
            }

            return;
        }

        if (existing != null
                && existing.operation() == operation
                && Math.abs(existing.amount() - amount)
                < EPSILON) {

            return;
        }

        if (existing != null) {
            instance.removeModifier(modifierId);
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        modifierId,
                        amount,
                        operation
                )
        );
    }

    public static void removeAttributes(
            ServerPlayer player
    ) {

        removeModifier(
                player,
                Attributes.ARMOR,
                ARMOR_MODIFIER
        );

        removeModifier(
                player,
                Attributes.MAX_HEALTH,
                HEALTH_MODIFIER
        );

        for (var entry :
                getSharedAttributes().entrySet()) {

            Holder<Attribute> attribute =
                    entry.getKey();

            ResourceLocation modifierId =
                    entry.getValue();

            AttributeInstance playerAttr =
                    player.getAttribute(attribute);

            if (playerAttr == null) {
                continue;
            }

            removeModifier(
                    player,
                    attribute,
                    modifierId
            );

        }


        for (var entry :
                getSharedAttributesResist().entrySet()) {

            Holder<Attribute> attribute =
                    entry.getKey();

            ResourceLocation modifierId =
                    entry.getValue();

            AttributeInstance playerAttr =
                    player.getAttribute(attribute);

            if (playerAttr == null) {
                continue;
            }

            removeModifier(
                    player,
                    attribute,
                    modifierId
            );

        }

        clampHealth(player);
    }

    private static List<AbstractSpellCastingPet> getSummonedFamiliars(
            ServerPlayer player
    ) {

        List<AbstractSpellCastingPet> familiars =
                new ArrayList<>();

        if (!(player.level() instanceof ServerLevel)) {
            return familiars;
        }

        PlayerFamiliarData data =
                player.getData(
                        AttachmentRegistry.PLAYER_FAMILIAR_DATA
                );

        Set<UUID> summonedIds = data.getSummonedFamiliarIds();


        if (!summonedIds.isEmpty()) {
            Level var3 = player.level();
            if (var3 instanceof ServerLevel serverLevel) {

                for (UUID id : summonedIds) {
                    Entity entity = serverLevel.getEntity(id);
                    if (entity instanceof AbstractSpellCastingPet familiar) {
                        if (familiar.isAlive() && !familiar.getIsInHouse()) {
                            familiars.add(familiar);
                        }
                    }
                }

            }
        }
        return familiars;
    }


    private static void removeModifier(
            ServerPlayer player,
            Holder<Attribute> attribute,
            ResourceLocation modifierId
    ) {

        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }

    private static void clampHealth(
            ServerPlayer player
    ) {

        float maxHealth =
                player.getMaxHealth();

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static double sanitize(double value) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            return 0D;
        }

        return value;
    }

    private static double calculateBonus(
            List<Double> values
    ) {

        values.removeIf(v -> v <= 0D);

        values.sort(Double::compareTo);

        Collections.reverse(values);

        double bonus = 0D;

        for (int i = 0; i < values.size(); i++) {

            double x = values.get(i);

            double y;

            if (x < 100D) {
                y = x * 0.05D;
            } else {
                y = 5D * Math.sqrt(x / 100D);
            }

            bonus += y * Math.pow(0.8D, i);
        }

        return bonus;
    }
}

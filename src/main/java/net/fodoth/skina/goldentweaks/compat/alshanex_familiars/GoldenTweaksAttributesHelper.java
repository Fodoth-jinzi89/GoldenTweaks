package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.alshanex.familiarslib.data.PlayerFamiliarData;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.mixin.SchoolTypeAccessor;
import net.alshanex.familiarslib.registry.AttachmentRegistry;
import net.alshanex.familiarslib.util.CurioUtils;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GoldenTweaksAttributesHelper {

    private static volatile Map<Holder<Attribute>, ResourceLocation>
            SHARED_ATTRIBUTES;

    private GoldenTweaksAttributesHelper() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                GoldenTweaks.MODID,
                path
        );
    }

    private static ResourceLocation familiarsLibId(
            ResourceLocation modifierId
    ) {

        return ResourceLocation.fromNamespaceAndPath(
                "familiarslib",
                modifierId.getPath()
        );
    }

    private static List<AbstractSpellCastingPet>
    getSummonedFamiliars(ServerPlayer player) {

        List<AbstractSpellCastingPet> familiars =
                new ArrayList<>();

        Level level = player.level();

        if (!(level instanceof ServerLevel serverLevel)) {
            return familiars;
        }

        PlayerFamiliarData data =
                player.getData(
                        AttachmentRegistry.PLAYER_FAMILIAR_DATA
                );

        for (Entity entity : serverLevel.getAllEntities()) {

            if (!(entity instanceof AbstractSpellCastingPet familiar)) {
                continue;
            }

            UUID ownerUUID =
                    familiar.getOwnerUUID();

            if (ownerUUID == null) {
                continue;
            }

            if (!ownerUUID.equals(player.getUUID())) {
                continue;
            }

            if (!data.hasFamiliar(familiar.getUUID())) {
                continue;
            }

            if (familiar.getIsInHouse()) {
                continue;
            }

            familiars.add(familiar);
        }

        return familiars;
    }

    public static void removeAllAttributes(
            ServerPlayer player
    ) {

        List<AbstractSpellCastingPet> familiars =
                getSummonedFamiliars(player);

        for (AbstractSpellCastingPet familiar :
                familiars) {

            removeAttributes(familiar);
        }
    }

    public static void handleSpellbookEquipChange(
            ServerPlayer player,
            boolean equipped
    ) {

        if (equipped) {
            recalculateAllAttributes(player);
        } else {
            removeAllAttributes(player);
        }
    }

    public static void handleFamiliarSummoned(
            ServerPlayer player,
            AbstractSpellCastingPet familiar
    ) {

        if (GoldenTweaksFamiliarCurioHelper
                .isWearingRegularFamiliarSpellbook(player)) {
            recalculateAllAttributes(player);
        }
    }

    public static void handleFamiliarDismissed(
            ServerPlayer player,
            AbstractSpellCastingPet familiar
    ) {

        removeAttributes(familiar);

        if (GoldenTweaksFamiliarCurioHelper
                .isWearingRegularFamiliarSpellbook(player)) {
            recalculateAllAttributes(player);
        }
    }

    public static void handlePlayerAttributeChange(
            ServerPlayer player
    ) {

        if (GoldenTweaksFamiliarCurioHelper
                .isWearingRegularFamiliarSpellbook(player)) {
            recalculateAllAttributes(player);
        }
    }

    public static void applyAttributes(
            AbstractSpellCastingPet familiar
    ) {

        LivingEntity owner =
                familiar.getSummoner();

        if (!(owner instanceof ServerPlayer player)) {
            return;
        }

        if (!GoldenTweaksFamiliarCurioHelper
                .isWearingRegularFamiliarSpellbook(player)) {

            removeAttributes(familiar);
            return;
        }

        List<AbstractSpellCastingPet> familiars =
                getSummonedFamiliars(player);

        if (familiars.isEmpty()) {
            return;
        }

        applySharedModifiers(
                familiar,
                player,
                familiars.size()
        );
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    getSharedAttributes() {

        if (SHARED_ATTRIBUTES == null) {

            synchronized (GoldenTweaksAttributesHelper.class) {

                if (SHARED_ATTRIBUTES == null) {
                    SHARED_ATTRIBUTES =
                            buildSharedAttributesMap();
                }
            }
        }

        return SHARED_ATTRIBUTES;
    }

    private static Map<Holder<Attribute>, ResourceLocation>
    buildSharedAttributesMap() {

        Map<Holder<Attribute>, ResourceLocation> map =
                new LinkedHashMap<>();

        map.put(
                AttributeRegistry.SPELL_POWER,
                id("shared_spell_power")
        );

        map.put(
                AttributeRegistry.SPELL_RESIST,
                id("shared_spell_resist")
        );

        for (SchoolType school : SchoolRegistry.REGISTRY) {

            String name =
                    school.getId().getPath();

            Holder<Attribute> power =
                    ((SchoolTypeAccessor) school)
                            .getPowerAttribute();

            Holder<Attribute> resist =
                    ((SchoolTypeAccessor) school)
                            .getResistanceAttribute();

            if (power != null) {

                map.put(
                        power,
                        id("shared_" + name + "_spell_power")
                );
            }

            if (resist != null) {

                map.put(
                        resist,
                        id("shared_" + name + "_magic_resist")
                );
            }
        }

        return Collections.unmodifiableMap(map);
    }

    private static void applySharedModifiers(
            AbstractSpellCastingPet familiar,
            ServerPlayer player,
            int totalFamiliars
    ) {

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

            AttributeInstance familiarAttr =
                    familiar.getAttribute(attribute);

            if (familiarAttr == null) {
                continue;
            }

            removeSharedModifier(
                    familiarAttr,
                    modifierId
            );

            double additional =
                    playerAttr.getValue()
                            - playerAttr.getBaseValue();

            additional /= 2D;

            additional =
                    Math.round(additional * 100D)
                            / 100D;

            if (additional <= 0D) {
                continue;
            }

            double share =
                    additional / totalFamiliars;

            share =
                    Math.round(share * 100D)
                            / 100D;

            familiarAttr.addTransientModifier(
                    new AttributeModifier(
                            modifierId,
                            share,
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
        }
    }

    private static void removeSharedModifier(
            AttributeInstance instance,
            ResourceLocation modifierId
    ) {

        instance.removeModifier(modifierId);
        instance.removeModifier(
                familiarsLibId(modifierId)
        );
    }

    public static void removeAttributes(
            AbstractSpellCastingPet familiar
    ) {

        for (var entry :
                getSharedAttributes().entrySet()) {

            AttributeInstance instance =
                    familiar.getAttribute(
                            entry.getKey()
                    );

            if (instance != null) {

                removeSharedModifier(
                        instance,
                        entry.getValue()
                );
            }
        }
    }

    public static void recalculateAllAttributes(
            ServerPlayer player
    ) {

        if (!GoldenTweaksFamiliarCurioHelper
                .isWearingRegularFamiliarSpellbook(player)) {

            removeAllAttributes(player);
            return;
        }

        List<AbstractSpellCastingPet> familiars =
                getSummonedFamiliars(player);

        int count = familiars.size();

        for (AbstractSpellCastingPet familiar :
                familiars) {

            applySharedModifiers(
                    familiar,
                    player,
                    count
            );
        }
    }
}

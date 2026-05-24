package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars.accessor.EntityAccessor;
import net.fodoth.skina.goldentweaks.network.packet.S2CConsumableSyncPacket;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GoldenTweaksConsumableHelper {

    public static final String NBT_KEY = "GoldenTweaksConsumables";

    private static final Map<UUID, GoldenTweaksConsumableData> CACHE =
            new ConcurrentHashMap<>();

    private GoldenTweaksConsumableHelper() {}

    // =========================================================
    // 基础访问
    // =========================================================

    public static GoldenTweaksConsumableData getData(AbstractSpellCastingPet familiar) {

        UUID id = familiar.getUUID();

        GoldenTweaksConsumableData cached = CACHE.get(id);
        if (cached != null) {
            return cached;
        }

        GoldenTweaksConsumableData loaded = loadData(familiar);
        CACHE.put(id, loaded);

        return loaded;
    }

    public static double getValue(AbstractSpellCastingPet familiar,
                                  FamiliarConsumableSystem.ConsumableType type) {
        return getData(familiar).getValue(type);
    }

    public static void setValue(AbstractSpellCastingPet familiar,
                                FamiliarConsumableSystem.ConsumableType type,
                                double value) {

        GoldenTweaksConsumableData data = getData(familiar);
        data.setValue(type, value);
        saveData(familiar, data);
    }

    public static void addValue(AbstractSpellCastingPet familiar,
                                FamiliarConsumableSystem.ConsumableType type,
                                double delta) {

        GoldenTweaksConsumableData data = getData(familiar);
        data.addValue(type, delta);
        saveData(familiar, data);
    }

    // =========================================================
    // 核心写入（唯一入口）
    // =========================================================

    public static void saveData(AbstractSpellCastingPet familiar) {
        saveData(familiar, getData(familiar));
    }

    public static void saveData(AbstractSpellCastingPet familiar,
                                GoldenTweaksConsumableData data) {

        CACHE.put(familiar.getUUID(), data);

        CompoundTag tag = new CompoundTag();
        data.saveToNBT(tag);

        familiar.getPersistentData().put(NBT_KEY, tag);

        applyAttributes(familiar, data);
    }

    // =========================================================
    // 加载
    // =========================================================

    public static GoldenTweaksConsumableData loadData(AbstractSpellCastingPet familiar) {

        CompoundTag persistent = familiar.getPersistentData();

        if (!persistent.contains(NBT_KEY)) {

            GoldenTweaksConsumableData migrated = migrateFromVanilla(familiar);
            saveData(familiar, migrated);
            return migrated;
        }

        CompoundTag tag = persistent.getCompound(NBT_KEY);
        return new GoldenTweaksConsumableData(tag);
    }

    public static void loadDataIntoCache(AbstractSpellCastingPet familiar,
                                         CompoundTag sourceTag) {

        GoldenTweaksConsumableData data;

        if (sourceTag.contains(NBT_KEY)) {
            data = loadFromNBT(sourceTag);
        } else {
            data = migrateFromVanilla(sourceTag);
        }

        CACHE.put(familiar.getUUID(), data);
    }

    public static void clearData(AbstractSpellCastingPet familiar) {
        CACHE.remove(familiar.getUUID());
    }

    // =========================================================
    // NBT 工具
    // =========================================================

    public static void saveToNBT(CompoundTag nbt,
                                 GoldenTweaksConsumableData data) {

        CompoundTag tag = new CompoundTag();
        data.saveToNBT(tag);
        nbt.put(NBT_KEY, tag);
    }

    public static GoldenTweaksConsumableData loadFromNBT(CompoundTag nbt) {

        if (!nbt.contains(NBT_KEY)) {
            return new GoldenTweaksConsumableData();
        }

        return new GoldenTweaksConsumableData(nbt.getCompound(NBT_KEY));
    }

    public static CompoundTag toNBT(AbstractSpellCastingPet pet) {
        return toNBT(getData(pet));
    }

    public static CompoundTag toNBT(GoldenTweaksConsumableData data) {

        CompoundTag tag = new CompoundTag();

        for (FamiliarConsumableSystem.ConsumableType type : FamiliarConsumableSystem.ConsumableType.values()) {
            tag.putDouble(type.name(), data.getValue(type));
        }

        return tag;
    }

    public static CompoundTag createFamiliarNBT(AbstractSpellCastingPet familiar) {
        CompoundTag nbt = new CompoundTag();

        GoldenTweaksConsumableData data = getData(familiar);

        familiar.saveWithoutId(nbt);

        nbt.putFloat("currentHealth", familiar.getHealth());
        nbt.putFloat("baseMaxHealth", familiar.getBaseMaxHealth());
        nbt.putString("id", EntityType.getKey(familiar.getType()).toString());

        if (familiar.hasCustomName()) {
            nbt.putString("customName", Objects.requireNonNull(familiar.getCustomName()).getString());
        }

        saveToNBT(nbt, data);
        GoldenTweaksConsumableHelper.saveData(familiar, data);

        return nbt;
    }

    public static GoldenTweaksConsumableData fromNBT(CompoundTag tag) {

        GoldenTweaksConsumableData data = new GoldenTweaksConsumableData();

        for (FamiliarConsumableSystem.ConsumableType type : FamiliarConsumableSystem.ConsumableType.values()) {

            if (tag.contains(type.name())) {
                data.setValue(type, tag.getDouble(type.name()));
            }
        }

        return data;
    }

    public static void applyClientData(
            AbstractSpellCastingPet pet,
            GoldenTweaksConsumableData data
    ) {
        saveData(pet, data);
    }

    public static GoldenTweaksConsumableData getClientData(AbstractSpellCastingPet pet) {
        return CACHE.getOrDefault(pet.getUUID(), new GoldenTweaksConsumableData());
    }

    public static void clearClientData(UUID entityId) {
        CACHE.remove(entityId);
    }

    // =========================================================
    // 迁移（只执行一次）
    // =========================================================

    public static GoldenTweaksConsumableData migrateFromVanilla(AbstractSpellCastingPet familiar) {

        return migrateFromVanilla(familiar.getPersistentData());
    }

    public static GoldenTweaksConsumableData migrateFromVanilla(CompoundTag nbt) {

        GoldenTweaksConsumableData data = new GoldenTweaksConsumableData();

        FamiliarConsumableSystem.ConsumableData vanilla =
                new FamiliarConsumableSystem.ConsumableData(nbt);

        for (FamiliarConsumableSystem.ConsumableType type :
                FamiliarConsumableSystem.ConsumableType.values()) {

            data.setValue(type, vanilla.getValue(type));
        }

        return data;
    }

// =========================================================
// Attribute Bridge（GT 唯一属性入口）
// =========================================================

    public static void applyAttributes(
            AbstractSpellCastingPet familiar
    ) {
        applyAttributes(familiar, getData(familiar));
    }

    public static void applyAttributes(
            AbstractSpellCastingPet familiar,
            GoldenTweaksConsumableData data
    ) {

        removeAllModifiers(familiar);

        addModifier(
                familiar,
                Attributes.ARMOR,
                "consumable_armor",
                sanitize(data.getArmor()),
                AttributeModifier.Operation.ADD_VALUE
        );

        addModifier(
                familiar,
                Attributes.MAX_HEALTH,
                "consumable_health",
                sanitize(data.getHealth()) / 100D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        addModifier(
                familiar,
                AttributeRegistry.SPELL_POWER,
                "consumable_spell_power",
                sanitize(data.getSpellPower()) / 100D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        addModifier(
                familiar,
                AttributeRegistry.SPELL_RESIST,
                "consumable_spell_resist",
                sanitize(data.getSpellResist()) / 100D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        addModifier(
                familiar,
                Attributes.ATTACK_DAMAGE,
                "consumable_enraged",
                sanitize(data.getEnraged()) * 0.5D,
                AttributeModifier.Operation.ADD_VALUE
        );

        clampHealth(familiar);

        sendToClient(familiar, data);
    }

    private static void addModifier(
            AbstractSpellCastingPet familiar,
            Holder<Attribute> attribute,
            String path,
            double amount,
            AttributeModifier.Operation operation
    ) {

        if (amount <= 0D) {
            return;
        }

        AttributeInstance instance =
                familiar.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        instance.addPermanentModifier(
                new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath(
                                "goldentweaks",
                                path
                        ),
                        amount,
                        operation
                )
        );
    }

    private static void clampHealth(
            AbstractSpellCastingPet familiar
    ) {

        if (familiar.level().isClientSide) {
            return;
        }

        float maxHealth =
                familiar.getMaxHealth();

        if (familiar.getHealth() > maxHealth) {

            familiar.setHealth(maxHealth);
        }
    }

    public static void refreshAttributes(
            AbstractSpellCastingPet familiar
    ) {
        applyAttributes(
                familiar,
                getData(familiar)
        );
    }

    public static void sync(
            AbstractSpellCastingPet familiar
    ) {
        refreshAttributes(familiar);
    }

// =========================================================
// 组合操作
// =========================================================

    public static void apply(
            AbstractSpellCastingPet familiar,
            FamiliarConsumableSystem.ConsumableType type,
            double delta
    ) {

        GoldenTweaksConsumableData data =
                getData(familiar);

        data.addValue(
                type,
                delta
        );

        saveData(
                familiar,
                data
        );

        refreshAttributes(familiar);
    }

// =========================================================
// INTERNAL
// =========================================================

    private static double sanitize(
            double value
    ) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            return 0D;
        }

        return value;
    }
    public static double getGuiValue(AbstractSpellCastingPet familiar,
                                     FamiliarConsumableSystem.ConsumableType type) {
        return getData(familiar).getValue(type);
    }

// =========================================================
// Modifier Cleanup
// =========================================================

    public static void removeAllModifiers(
            AbstractSpellCastingPet familiar
    ) {

        removeModifier(
                familiar,
                Attributes.ARMOR,
                "familiarslib:consumable_armor",
                "goldentweaks:consumable_armor"
        );

        removeModifier(
                familiar,
                Attributes.MAX_HEALTH,
                "familiarslib:consumable_health",
                "goldentweaks:consumable_health"
        );

        removeModifier(
                familiar,
                AttributeRegistry.SPELL_POWER,
                "familiarslib:consumable_spell_power",
                "goldentweaks:consumable_spell_power"
        );

        removeModifier(
                familiar,
                AttributeRegistry.SPELL_RESIST,
                "familiarslib:consumable_spell_resist",
                "goldentweaks:consumable_spell_resist"
        );

        removeModifier(
                familiar,
                Attributes.ATTACK_DAMAGE,
                "familiarslib:consumable_enraged",
                "goldentweaks:consumable_enraged"
        );

        removeLegacyModifiers(familiar);
    }

    public static void removeLegacyModifiers(
            AbstractSpellCastingPet familiar
    ) {

        removeModifier(
                familiar,
                Attributes.MAX_HEALTH,
                "familiarslib:familiar_health_modifier"
        );

        removeModifier(
                familiar,
                Attributes.ARMOR,
                "familiarslib:familiar_armor_modifier"
        );
    }

// =========================================================
// INTERNAL
// =========================================================

    private static void removeModifier(
            AbstractSpellCastingPet familiar,
            Holder<Attribute> attribute,
            String... ids
    ) {

        AttributeInstance instance =
                familiar.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        for (String id : ids) {

            AttributeModifier modifier =
                    instance.getModifier(
                            ResourceLocation.parse(id)
                    );

            if (modifier != null) {

                instance.removeModifier(modifier);
            }
        }
    }

    public static void sendToClient(AbstractSpellCastingPet familiar, GoldenTweaksConsumableData data) {
        EntityAccessor e = (EntityAccessor) familiar;
        Level l = e.goldentweaks$getLevel();
        if (!l.isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntity(
                    familiar,
                    new S2CConsumableSyncPacket(familiar.getId(), data)
            );
        }
    }
}

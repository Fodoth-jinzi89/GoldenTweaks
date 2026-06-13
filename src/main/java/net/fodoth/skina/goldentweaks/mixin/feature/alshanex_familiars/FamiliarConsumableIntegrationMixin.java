package net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars;

import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableComponent;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableIntegration;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem;
import net.alshanex.familiarslib.util.consumables.FamiliarConsumableSystem.ConsumableType;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableData;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.GoldenTweaksConsumableHelper;
import net.fodoth.skina.goldentweaks.network.packet.S2CConsumableSyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static net.alshanex.familiarslib.util.consumables.FamiliarConsumableIntegration.getConsumableComponent;

@Mixin(FamiliarConsumableIntegration.class)
public abstract class FamiliarConsumableIntegrationMixin {

    @Shadow
    private static String getTypeTranslationKey(ConsumableType type) {
        return "";
    }

    @Unique
    private static void goldentweaks$sendActionBar(
            Player player,
            Component component,
            ChatFormatting color
    ) {

        if (player instanceof ServerPlayer serverPlayer) {

            serverPlayer.connection.send(
                    new ClientboundSetActionBarTextPacket(
                            component.copy().withStyle(color)
                    )
            );
        }
    }

    @Unique
    private static int goldentweaks$getMaxUsableTierInternal(
            double currentValue,
            ConsumableType type
    ) {

        for (int tier = type.getMaxTier(); tier >= 1; --tier) {

            if (currentValue < type.getTierLimit(tier)) {
                return tier;
            }
        }

        return 0;
    }

    @Unique
    private static String goldentweaks$formatValue(double value) {

        return value == Math.floor(value)
                ? String.valueOf((int) value)
                : String.format("%.2f", value);
    }

    /**
     * @author GoldenTweaks
     * @reason 以后可以加点特效
     */
    @Overwrite
    private static void spawnConsumableEffects(
            AbstractSpellCastingPet familiar
    ) {
    }

    /**
     * @author GoldenTweaks
     * @reason 使用 GT consumable 数据系统
     */
    @Overwrite
    public static FamiliarConsumableSystem.ConsumableData getConsumableData(
            AbstractSpellCastingPet familiar
    ) {

        GoldenTweaksConsumableData gtData =
                GoldenTweaksConsumableHelper.getData(familiar);

        FamiliarConsumableSystem.ConsumableData vanilla =
                new FamiliarConsumableSystem.ConsumableData();

        for (ConsumableType type : ConsumableType.values()) {

            vanilla.setValue(
                    type,
                    (int) Math.round(gtData.getValue(type))
            );
        }

        return vanilla;
    }

    /**
     * @author GoldenTweaks
     * @reason 直接从 GT NBT 读取
     */
    @Overwrite
    private static FamiliarConsumableSystem.ConsumableData loadFromEntityNBT(
            AbstractSpellCastingPet familiar
    ) {
        return getConsumableData(familiar);
    }

    /**
     * @author GoldenTweaks
     * @reason 写入 GT 数据
     */
    @Overwrite
    private static void saveToEntityNBT(
            AbstractSpellCastingPet familiar,
            FamiliarConsumableSystem.ConsumableData data
    ) {

        GoldenTweaksConsumableData gtData =
                new GoldenTweaksConsumableData();

        for (ConsumableType type : ConsumableType.values()) {
            gtData.setValue(type, data.getValue(type));
        }

        GoldenTweaksConsumableHelper.saveData(
                familiar,
                gtData
        );
    }

    /**
     * @author GoldenTweaks
     * @reason GT bridge
     */
    @Overwrite
    private static void updateConsumableData(
            AbstractSpellCastingPet familiar,
            FamiliarConsumableSystem.ConsumableData data
    ) {
        saveToEntityNBT(familiar, data);
    }

    /**
     * @author GoldenTweaks
     * @reason 使用 GT 数据系统处理 consumable
     */
    @Overwrite
    public static InteractionResult handleConsumableInteraction(
            AbstractSpellCastingPet familiar,
            Player player,
            ItemStack itemStack
    ) {

        FamiliarConsumableComponent component =
                getConsumableComponent(itemStack);

        if (component == null) {
            return InteractionResult.PASS;
        }

        if (familiar.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        GoldenTweaksConsumableData data =
                GoldenTweaksConsumableHelper.getData(familiar);

        ConsumableType type =
                component.type();

        double currentValue =
                data.getValue(type);

        int maxAllowed =
                component.getLimit();

        if (currentValue >= maxAllowed) {

            goldentweaks$sendActionBar(
                    player,
                    Component.translatable(
                            "message.familiarslib.consumable.max_reached",
                            Component.translatable(
                                    getTypeTranslationKey(type)
                            )
                    ),
                    ChatFormatting.RED
            );

            return InteractionResult.FAIL;
        }

        int maxUsableTier =
                getMaxUsableTier(familiar, type);

        if (component.tier() > maxUsableTier) {

            goldentweaks$sendActionBar(
                    player,
                    Component.translatable(
                            "message.familiarslib.consumable.tier_too_high",
                            component.tier(),
                            maxUsableTier
                    ),
                    ChatFormatting.RED
            );

            return InteractionResult.FAIL;
        }

        double newValue = Math.min(
                currentValue + component.getBonus(),
                maxAllowed
        );

        GoldenTweaksConsumableHelper.setValue(
                familiar,
                type,
                newValue
        );


        PacketDistributor.sendToPlayersTrackingEntity(
                familiar,
                new S2CConsumableSyncPacket(
                        familiar.getId(),
                        GoldenTweaksConsumableHelper.getData(familiar)
                )
        );

        if (type == ConsumableType.HEALTH) {
            familiar.setHealth(familiar.getMaxHealth());
        }

        itemStack.shrink(1);

        goldentweaks$sendActionBar(
                player,
                Component.translatable(
                        "message.familiarslib.consumable.success",
                        Component.translatable(
                                getTypeTranslationKey(type)
                        ),
                        goldentweaks$formatValue(newValue)
                                + getUnitSuffix(type)
                ),
                ChatFormatting.GREEN
        );


        spawnConsumableEffects(familiar);

        return InteractionResult.SUCCESS;
    }

    /**
     * @author GoldenTweaks
     * @reason 支持 double
     */
    @Overwrite
    private static int getMaxUsableTier(
            FamiliarConsumableSystem.ConsumableData data,
            ConsumableType type
    ) {

        return goldentweaks$getMaxUsableTierInternal(
                data.getValue(type),
                type
        );
    }

    /**
     * @author GoldenTweaks
     * @reason 使用 GT helper
     */
    @Overwrite
    public static void saveConsumableData(
            AbstractSpellCastingPet familiar,
            CompoundTag compound
    ) {

        GoldenTweaksConsumableHelper.saveToNBT(
                compound,
                GoldenTweaksConsumableHelper.getData(familiar)
        );
    }

    /**
     * @author GoldenTweaks
     * @reason 使用 GT helper
     */
    @Overwrite
    public static void loadConsumableData(
            AbstractSpellCastingPet familiar,
            CompoundTag compound
    ) {

        GoldenTweaksConsumableHelper.loadDataIntoCache(
                familiar,
                compound
        );
    }

    /**
     * @author GoldenTweaks
     * @reason GT attribute bridge
     */
    @Overwrite
    public static void removeConsumableModifiers(
            AbstractSpellCastingPet familiar
    ) {

        FamiliarConsumableSystem.removeAllConsumableModifiers(
                familiar
        );

        GoldenTweaksConsumableHelper.clearData(familiar);
    }

    /**
     * @author GoldenTweaks
     * @reason GT attribute bridge
     */
    @Overwrite
    public static void applyConsumableModifiers(
            AbstractSpellCastingPet familiar
    ) {

        GoldenTweaksConsumableHelper.sync(familiar);
    }

    /**
     * @author GoldenTweaks
     * @reason 支持 double
     */
    @Overwrite
    public static int getCurrentBonus(
            AbstractSpellCastingPet familiar,
            ConsumableType type
    ) {

        return (int) Math.round(
                GoldenTweaksConsumableHelper.getValue(
                        familiar,
                        type
                )
        );
    }

    /**
     * @author GoldenTweaks
     * @reason 支持 double
     */
    @Overwrite
    public static int getMaxUsableTier(
            AbstractSpellCastingPet familiar,
            ConsumableType type
    ) {

        return goldentweaks$getMaxUsableTierInternal(
                GoldenTweaksConsumableHelper.getValue(
                        familiar,
                        type
                ),
                type
        );
    }

    /**
     * @author GoldenTweaks
     * @reason GT cache
     */
    @Overwrite
    public static void clearCachedData(
            AbstractSpellCastingPet familiar
    ) {

        GoldenTweaksConsumableHelper.clearData(familiar);
    }

    /**
     * @author GoldenTweaks
     * @reason GT data
     */
    @Overwrite
    public static boolean canFamiliarBlock(
            AbstractSpellCastingPet familiar
    ) {

        return GoldenTweaksConsumableHelper.getValue(
                familiar,
                ConsumableType.BLOCKING
        ) > 0D;
    }

    /**
     * @author GoldenTweaks
     * @reason GT data
     */
    @Overwrite
    public static int getEnragedStacks(
            AbstractSpellCastingPet familiar
    ) {

        return (int) Math.round(
                GoldenTweaksConsumableHelper.getValue(
                        familiar,
                        ConsumableType.ENRAGED
                )
        );
    }

    /**
     * @author GoldenTweaks
     * @reason 迁移旧数据
     */
    @Overwrite
    public static void migrateLegacyData(
            AbstractSpellCastingPet familiar,
            int legacyHealthStacks,
            int legacyArmorStacks,
            int legacyEnragedStacks,
            boolean legacyCanBlock
    ) {

        FamiliarConsumableSystem.removeLegacyModifiers(
                familiar
        );

        float currentHealth =
                familiar.getHealth();

        GoldenTweaksConsumableData data =
                GoldenTweaksConsumableHelper.getData(familiar);

        if (legacyHealthStacks > 0) {

            data.setHealth(
                    Math.min(
                            legacyHealthStacks * 10,
                            ConsumableType.HEALTH.getMaxLimit()
                    )
            );
        }

        if (legacyArmorStacks > 0) {

            data.setArmor(
                    Math.min(
                            legacyArmorStacks,
                            ConsumableType.ARMOR.getMaxLimit()
                    )
            );
        }

        if (legacyEnragedStacks > 0) {

            data.setEnraged(
                    Math.min(
                            legacyEnragedStacks,
                            ConsumableType.ENRAGED.getMaxLimit()
                    )
            );
        }

        if (legacyCanBlock) {
            data.setBlocking(1D);
        }

        GoldenTweaksConsumableHelper.saveData(
                familiar,
                data
        );

        familiar.setHealth(
                Math.min(
                        currentHealth,
                        familiar.getMaxHealth()
                )
        );
    }

    /**
     * @author Fodoth_jinzi89
     * @reason I18n
     */
    @Overwrite
    private static String getUnitSuffix(
            ConsumableType type
    ) {

        return switch (type) {
            case ARMOR -> "⛨";
            case HEALTH, SPELL_POWER, SPELL_RESIST -> "%";
            case ENRAGED -> "▤";
            case BLOCKING -> "";
        };
    }
}
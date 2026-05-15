package net.fodoth.skina.goldentweaks.config;

import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigScreenHelper {

    public static void addDouble(
            ConfigCategory category,
            ConfigEntryBuilder eb,
            String key,
            ModConfigSpec.DoubleValue config,
            double def,
            double min,
            double max
    ) {
        category.addEntry(
                eb.startDoubleField(
                                Component.translatable(key),
                                config.get()
                        )
                        .setDefaultValue(def)
                        .setMin(min)
                        .setMax(max)
                        .setTooltip(Component.translatable(key + ".tooltip"))
                        .setSaveConsumer(config::set)
                        .build()
        );
    }

    public static void addInt(
            ConfigCategory category,
            ConfigEntryBuilder eb,
            String key,
            ModConfigSpec.IntValue config,
            int def,
            int min,
            int max
    ) {
        category.addEntry(
                eb.startIntField(
                                Component.translatable(key),
                                config.get()
                        )
                        .setDefaultValue(def)
                        .setMin(min)
                        .setMax(max)
                        .setTooltip(Component.translatable(key + ".tooltip"))
                        .setSaveConsumer(config::set)
                        .build()
        );
    }

    public static void addBool(
            ConfigCategory category,
            ConfigEntryBuilder eb,
            String key,
            ModConfigSpec.BooleanValue config,
            boolean def
    ) {
        category.addEntry(
                eb.startBooleanToggle(
                                Component.translatable(key),
                                config.get()
                        )
                        .setDefaultValue(def)
                        .setTooltip(Component.translatable(key + ".tooltip"))
                        .setSaveConsumer(config::set)
                        .build()
        );
    }

    public static <T extends Enum<T>> void addEnum(
            ConfigCategory category,
            ConfigEntryBuilder eb,
            String key,
            ModConfigSpec.EnumValue<T> config,
            T def,
            T[] values
    ) {
        category.addEntry(
                eb.startSelector(
                                Component.translatable(key),
                                values,
                                config.get()
                        )
                        .setDefaultValue(def)
                        .setNameProvider(value -> {
                            String name = value.name().toLowerCase();
                            return Component.translatable(key + "." + name);
                        })
                        .setTooltip(Component.translatable(key + ".tooltip"))
                        .setSaveConsumer(config::set)
                        .build()
        );
    }
}
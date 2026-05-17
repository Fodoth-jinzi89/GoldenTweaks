package net.fodoth.skina.goldentweaks.config;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

public class ConfigScreenHelper {

    private static Class<?> ENTRY_CLASS;

    static {
        try {
            ENTRY_CLASS = Class.forName(
                    "me.shedaniel.clothconfig2.api.AbstractConfigListEntry"
            );
        } catch (ClassNotFoundException e) {
            GoldenTweaks.LOGGER.warn("Could not find Cloth Config entry class");
        }
    }

    // =========================================================
    // DOUBLE
    // =========================================================
    public static void addDouble(
            Object category,
            Object eb,
            String key,
            ModConfigSpec.DoubleValue config,
            double def,
            double min,
            double max
    ) {
        try {
            Object builder = eb.getClass()
                    .getMethod(
                            "startDoubleField",
                            Component.class,
                            double.class
                    )
                    .invoke(
                            eb,
                            Component.translatable(key),
                            config.get()
                    );

            invoke(builder, "setDefaultValue", def);
            invoke(builder, "setMin", min);
            invoke(builder, "setMax", max);

            applyComment(builder, key);

            bindSaveConsumer(
                    builder,
                    config::set
            );

            Object entry = invoke(builder, "build");

            bind(entry, config);

            addEntry(category, entry);

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // INT
    // =========================================================
    public static void addInt(
            Object category,
            Object eb,
            String key,
            ModConfigSpec.IntValue config,
            int def,
            int min,
            int max
    ) {
        try {
            Object builder = eb.getClass()
                    .getMethod(
                            "startIntField",
                            Component.class,
                            int.class
                    )
                    .invoke(
                            eb,
                            Component.translatable(key),
                            config.get()
                    );

            invoke(builder, "setDefaultValue", def);
            invoke(builder, "setMin", min);
            invoke(builder, "setMax", max);

            applyComment(builder, key);

            bindSaveConsumer(
                    builder,
                    config::set
            );

            Object entry = invoke(builder, "build");

            bind(entry, config);

            addEntry(category, entry);

        } catch (Exception ignored) {

        }
    }

    // =========================================================
    // BOOL
    // =========================================================
    public static void addBool(
            Object category,
            Object eb,
            String key,
            ModConfigSpec.BooleanValue config,
            boolean def
    ) {
        try {
            Object builder = eb.getClass()
                    .getMethod(
                            "startBooleanToggle",
                            Component.class,
                            boolean.class
                    )
                    .invoke(
                            eb,
                            Component.translatable(key),
                            config.get()
                    );

            invoke(builder, "setDefaultValue", def);

            applyComment(builder, key);

            bindSaveConsumer(
                    builder,
                    config::set
            );

            Object entry = invoke(builder, "build");

            bind(entry, config);

            addEntry(category, entry);

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // ENUM
    // =========================================================
    public static <T extends Enum<T>> void addEnum(
            Object category,
            Object eb,
            String key,
            ModConfigSpec.EnumValue<T> config,
            T def,
            T[] values
    ) {
        try {
            Object builder = eb.getClass()
                    .getMethod(
                            "startSelector",
                            Component.class,
                            Object[].class,
                            Object.class
                    )
                    .invoke(
                            eb,
                            Component.translatable(key),
                            values,
                            config.get()
                    );

            invoke(builder, "setDefaultValue", def);

            applyComment(builder, key);

            bindSaveConsumer(
                    builder,
                    config::set
            );

            Object entry = invoke(builder, "build");

            bind(entry, config);

            addEntry(category, entry);

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // SAVE CONSUMER
    // =========================================================
    static <T> void bindSaveConsumer(
            Object builder,
            Consumer<T> consumer
    ) {
        try {
            invoke(builder, "setSaveConsumer", consumer);
        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // CONFIG BIND
    // =========================================================
    static void bind(Object entry, Object config) {
        try {
            Method setValue = findMethod(entry, "setValue");

            Object current = config.getClass()
                    .getMethod("get")
                    .invoke(config);

            setValue.invoke(entry, current);

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // ADD ENTRY
    // =========================================================
    static void addEntry(
            Object category,
            Object entry
    ) throws Exception {

        if (!ENTRY_CLASS.isInstance(entry)) {
            throw new ClassCastException(
                    "Not entry: " + entry.getClass()
            );
        }

        for (Method method : category.getClass().getMethods()) {

            if (!method.getName().equals("addEntry")) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                continue;
            }

            method.invoke(category, entry);
            return;
        }

        throw new NoSuchMethodException("addEntry");
    }

    // =========================================================
    // REFLECTION
    // =========================================================
    static Method findMethod(
            Object obj,
            String name
    ) throws Exception {

        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        throw new NoSuchMethodException(name);
    }

    static Object invoke(
            Object target,
            String name,
            Object... args
    ) throws Exception {

        for (Method method : target.getClass().getMethods()) {

            if (!method.getName().equals(name)) {
                continue;
            }

            if (method.getParameterCount() != args.length) {
                continue;
            }

            try {
                return method.invoke(target, args);
            } catch (IllegalArgumentException ignored) {
            }
        }

        throw new NoSuchMethodException(name);
    }

    // =========================================================
    // TOOLTIP
    // =========================================================
    static void applyComment(
            Object target,
            String key
    ) {
        try {

            Component tooltip =
                    Component.translatable(key + ".comment");

            for (Method method : target.getClass().getMethods()) {

                if (!method.getName().equals("setTooltip")) {
                    continue;
                }

                if (method.getParameterCount() != 1) {
                    continue;
                }

                Class<?> param =
                        method.getParameterTypes()[0];

                // setTooltip(Component...)
                if (param.isArray()
                        && param.getComponentType() == Component.class) {

                    method.invoke(
                            target,
                            (Object) new Component[]{tooltip}
                    );

                    return;
                }

                // setTooltip(Optional<Component[]>)
                if (Optional.class.isAssignableFrom(param)) {

                    method.invoke(
                            target,
                            Optional.of(
                                    new Component[]{tooltip}
                            )
                    );

                    return;
                }
            }

        } catch (Exception ignored) {
        }
    }
}
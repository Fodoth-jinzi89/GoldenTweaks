package net.fodoth.skina.goldentweaks.compat.iris;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;

import java.lang.reflect.Field;

public final class GTGlslCaches {
    private GTGlslCaches() {
    }

    public static void configureOwner(Class<?> owner) {
        configureOwner(owner, false);
    }

    public static void configureTwoTierOwner(Class<?> owner) {
        configureOwner(owner, true);
    }

    private static void configureOwner(Class<?> owner, boolean twoTier) {
        try {
            Field transformer = owner.getDeclaredField("transformer");
            transformer.setAccessible(true);
            Object instance = transformer.get(null);
            if (twoTier) {
                boolean configured = false;
                for (var method : instance.getClass().getMethods()) {
                    if (method.getName().equals("setParsingCacheStrategy") && method.getParameterCount() == 1) {
                        Class<?> strategyType = method.getParameterTypes()[0];
                        Object strategy = strategyType.getField("TWO_TIER").get(null);
                        method.invoke(instance, strategy);
                        configured = true;
                        break;
                    }
                }
                if (!configured) throw new NoSuchMethodException("setParsingCacheStrategy");
            }
            configure(instance);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot configure Iris GLSL caches", exception);
        }
    }

    public static void configure(Object transformer) throws ReflectiveOperationException {
        Class<?> astParser = transformer.getClass();
        while (!astParser.getName().equals("io.github.douira.glsl_transformer.ast.transform.ASTParser")) {
            astParser = astParser.getSuperclass();
            if (astParser == null) throw new IllegalArgumentException("Missing GLSL ASTParser");
        }
        Field buildCache = astParser.getDeclaredField("buildCache");
        buildCache.setAccessible(true);
        buildCache.set(transformer, buildCache.getType().getConstructor(int.class)
                .newInstance(GoldenTweaksClientConfig.GLSL_DEFAULT_CACHE_LIMIT));
        Field parserField = astParser.getDeclaredField("parser");
        parserField.setAccessible(true);
        Object parser = parserField.get(transformer);
        if (parser.getClass().getName().equals("io.github.douira.glsl_transformer.parser.TwoTierCachingParser")) {
            parser.getClass().getMethod("setTwoTierCacheSizesAndClear", int.class, int.class).invoke(parser,
                    GoldenTweaksClientConfig.GLSL_DEFAULT_CACHE_LIMIT,
                    GoldenTweaksClientConfig.GLSL_TRANSLATION_CACHE_LIMIT);
        } else {
            parser.getClass().getMethod("setParseCacheSizeAndClear", int.class)
                    .invoke(parser, GoldenTweaksClientConfig.GLSL_DEFAULT_CACHE_LIMIT);
        }
    }
}

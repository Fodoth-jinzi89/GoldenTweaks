package net.fodoth.skina.goldentweaks.compat.connector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/** Shares immutable mapping text without dropping mappings or replacing their public views. */
public final class GTMappingStrings {
    private GTMappingStrings() {
    }

    public static int compactLoadedResolver() throws ReflectiveOperationException {
        Class<?> loaderType = Class.forName("net.fabricmc.loader.impl.FabricLoaderImpl");
        Object loader = loaderType.getField("INSTANCE").get(null);
        Field resolverField = loaderType.getDeclaredField("mappingResolver");
        resolverField.setAccessible(true);
        Object resolver = resolverField.get(loader);
        if (resolver == null) return 0;
        Field mappings = resolver.getClass().getDeclaredField("mappings");
        mappings.setAccessible(true);
        return compact(mappings.get(resolver));
    }

    public static int compact(Object root) throws ReflectiveOperationException {
        Map<String, String> strings = new HashMap<>();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<Object> pending = new ArrayDeque<>();
        pending.add(root);
        int replaced = 0;
        while (!pending.isEmpty()) {
            Object current = pending.removeLast();
            if (!visited.add(current)) continue;
            if (current instanceof Map<?, ?> map) {
                // Seed with keys too: field/array strings can then reuse strings already retained by maps.
                for (Object key : map.keySet()) {
                    if (key instanceof String text) strings.putIfAbsent(text, text);
                }
                for (Object value : map.values()) if (value != null) pending.add(value);
            } else if (current instanceof String[] names) {
                for (int i = 0; i < names.length; i++) {
                    String text = names[i];
                    if (text == null) continue;
                    String shared = strings.putIfAbsent(text, text);
                    if (shared != null && !shared.equals(text)) {
                        names[i] = shared;
                        replaced++;
                    }
                }
            } else if (current.getClass().getName().startsWith("reloc.net.minecraftforge.srgutils.")) {
                for (Class<?> type = current.getClass(); type != null; type = type.getSuperclass()) {
                    for (Field field : type.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                        field.setAccessible(true);
                        Object value = field.get(current);
                        if (value instanceof String text) {
                            String shared = strings.putIfAbsent(text, text);
                            if (shared != null && !shared.equals(text)) {
                                field.set(current, shared);
                                replaced++;
                            }
                        } else if (value != null) {
                            pending.add(value);
                        }
                    }
                }
            }
        }
        return replaced;
    }
}

package net.fodoth.skina.goldentweaks.util;

import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Shares text in retained templates without changing nodes, instructions or debug information. */
public final class GTMixinTreeText {
    private GTMixinTreeText() {
    }

    public static int compactLoaded() throws ReflectiveOperationException {
        Class<?> configType = Class.forName("org.spongepowered.asm.mixin.transformer.Config");
        Field configs = configType.getDeclaredField("allConfigs");
        configs.setAccessible(true);
        List<ClassNode> roots = new ArrayList<>();
        for (Object config : new ArrayList<>(((Map<?, ?>) configs.get(null)).values())) {
            Object inner = read(config, "config");
            for (Object mixin : new ArrayList<>((List<?>) read(inner, "mixins"))) {
                Object state = read(mixin, "state");
                if (state == null) continue;
                // Pending validation is still owned by the transformer. Only visit completed states.
                if (read(state, "validationClassNode") == null) {
                    roots.add((ClassNode) read(state, "classNode"));
                }
            }
        }
        return compact(roots);
    }

    private static Object read(Object owner, String name) throws ReflectiveOperationException {
        Field field = owner.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    public static int compact(List<ClassNode> roots) throws IllegalAccessException {
        Map<String, String> strings = new HashMap<>();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<Object> pending = new ArrayDeque<>(roots);
        int replaced = 0;
        while (!pending.isEmpty()) {
            Object current = pending.removeLast();
            if (!visited.add(current)) continue;
            if (current instanceof Iterable<?> values) {
                for (Object value : values) if (value != null) pending.add(value);
                continue;
            }
            // Public ASM fields only; never enter MixinInfo back-references or arbitrary mod objects.
            for (Field field : current.getClass().getFields()) {
                if (!field.getDeclaringClass().getName().startsWith("org.objectweb.asm.tree.")
                        || Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())
                        || field.getType().isPrimitive()) continue;
                Object value = field.get(current);
                if (value instanceof String text) {
                    String shared = strings.putIfAbsent(text, text);
                    if (shared != null && shared != text) {
                        field.set(current, shared);
                        replaced++;
                    }
                } else if (value instanceof Iterable<?> || (value != null
                        && value.getClass().getName().startsWith("org.objectweb.asm.tree."))) {
                    pending.add(value);
                }
            }
        }
        return replaced;
    }
}

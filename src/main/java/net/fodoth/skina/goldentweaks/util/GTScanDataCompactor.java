package net.fodoth.skina.goldentweaks.util;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Retains scan records and insertion order, removing the per-entry linked hash nodes. */
public final class GTScanDataCompactor {
    private GTScanDataCompactor() {
    }

    /** Shares backing text only; Type identity, descriptor slices and scan records stay unchanged. */
    public static int compactTypeText(List<ModFileScanData> scans) throws ReflectiveOperationException {
        Field buffer = Type.class.getDeclaredField("valueBuffer");
        buffer.setAccessible(true);
        Map<String, String> strings = new HashMap<>();
        int replaced = 0;
        for (ModFileScanData scan : scans) {
            for (ModFileScanData.ClassData data : scan.getClasses()) {
                replaced += shareText(data.clazz(), buffer, strings);
                replaced += shareText(data.parent(), buffer, strings);
                for (Type type : data.interfaces()) replaced += shareText(type, buffer, strings);
            }
            for (ModFileScanData.AnnotationData data : scan.getAnnotations()) {
                replaced += shareText(data.clazz(), buffer, strings);
                replaced += shareText(data.annotationType(), buffer, strings);
            }
        }
        return replaced;
    }

    private static int shareText(Type type, Field buffer, Map<String, String> strings) throws IllegalAccessException {
        if (type == null) return 0;
        String text = (String) buffer.get(type);
        if (text == null) return 0;
        String shared = strings.putIfAbsent(text, text);
        if (shared == null || shared == text) return 0;
        buffer.set(type, shared);
        return 1;
    }

    public static int compact(List<ModFileScanData> scans) throws ReflectiveOperationException {
        Field classes = ModFileScanData.class.getDeclaredField("classes");
        Field annotations = ModFileScanData.class.getDeclaredField("annotations");
        classes.setAccessible(true);
        annotations.setAccessible(true);
        int replaced = 0;
        for (ModFileScanData scan : scans) {
            replaced += compactField(scan, classes);
            replaced += compactField(scan, annotations);
        }
        return replaced;
    }

    private static int compactField(ModFileScanData scan, Field field) throws IllegalAccessException {
        Object value = field.get(scan);
        // Leave any other mod's collection replacement alone. Mutable Set API and order stay intact.
        if (value != null && value.getClass() == LinkedHashSet.class) {
            Set<?> source = (Set<?>) value;
            if (!source.isEmpty()) {
                field.set(scan, new ObjectLinkedOpenHashSet<>(source));
                return 1;
            }
        }
        return 0;
    }
}

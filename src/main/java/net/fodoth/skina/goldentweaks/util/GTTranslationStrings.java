package net.fodoth.skina.goldentweaks.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GTTranslationStrings {

    private GTTranslationStrings() {
    }

    public static Map<String, String> compact(@NotNull Map<String, String> translations) {
        Map<String, String> strings = new HashMap<>();
        Map<String, String> result = new LinkedHashMap<>();
        translations.forEach((key, value) -> result.put(
                strings.computeIfAbsent(key, text -> text), strings.computeIfAbsent(value, text -> text)));
        return result;
    }
}

package net.fodoth.skina.goldentweaks.util;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.jetbrains.annotations.NotNull;

/** Shares immutable face texture names without retaining unloaded model resources. */
public final class GTModelTextureNames {
    private static final Interner<String> NAMES = Interners.newWeakInterner();

    private GTModelTextureNames() {
    }

    public static @NotNull String share(@NotNull String name) {
        return NAMES.intern(name);
    }
}

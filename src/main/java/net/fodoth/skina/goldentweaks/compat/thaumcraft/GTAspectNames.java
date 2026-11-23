package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;

/**
 * Aspect names, resolved where Thaumcraft's classes are on the compile path.
 *
 * <p>Kept here rather than in the screen that shows the name: the screen is a general UI class and
 * naming {@code Aspect} from it made the whole file depend on Thaumcraft being present in the IDE
 * model, which it is not for every sync. Everything that touches the mod's API lives in this
 * package, and outside callers reach it through a plain string.</p>
 */
public final class GTAspectNames {

    private GTAspectNames() {
    }

    /**
     * The display name of an aspect, or {@code null} when it cannot be resolved.
     *
     * <p>Uses {@code displayName()} rather than {@code getName()}: the former is a translated
     * component, the latter a raw English fallback, and the window already speaks the player's
     * language.</p>
     */
    @Nullable
    public static Component of(String tag) {
        if (tag == null) {
            return null;
        }
        try {
            var aspect = Aspect.getAspect(tag);
            return aspect == null ? null : aspect.displayName();
        } catch (NoClassDefFoundError absent) {
            return null;
        }
    }
}

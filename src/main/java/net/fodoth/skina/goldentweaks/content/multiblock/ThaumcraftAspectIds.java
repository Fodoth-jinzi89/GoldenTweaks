package net.fodoth.skina.goldentweaks.content.multiblock;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

/**
 * Translates between a game id and Thaumcraft's {@link Aspect}.
 *
 * <p>Aspects are not registry entries: they are a static table keyed by tag, so an id like
 * {@code thaumcraft:aer} is resolved by its path and rebuilt in the {@code thaumcraft} namespace.
 * Kept beside the other Thaumcraft bridges so the port entity itself never names the mod's types.</p>
 */
final class ThaumcraftAspectIds {

    private static final String NAMESPACE = "thaumcraft";

    private ThaumcraftAspectIds() {
    }

    /** The aspect a game id names, or {@code null} when Thaumcraft has no such aspect. */
    static Aspect find(ResourceLocation id) {
        return id == null ? null : Aspect.getAspect(id.getPath());
    }

    /** The game id of an aspect, or {@code null} for {@code null}. */
    static ResourceLocation idOf(Aspect aspect) {
        return aspect == null ? null : ResourceLocation.fromNamespaceAndPath(NAMESPACE, aspect.getTag());
    }
}

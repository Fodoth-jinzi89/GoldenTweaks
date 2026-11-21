package net.fodoth.skina.goldentweaks.compat.continuity;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

/** Resolves the existing registry state without retaining a second index of all state model IDs. */
public final class GTBlockStateModelLookup {
    private GTBlockStateModelLookup() {
    }

    public static @Nullable BlockState find(@Nullable Object key) {
        if (!(key instanceof ModelResourceLocation location) || !BuiltInRegistries.BLOCK.containsKey(location.id())) {
            return null;
        }
        BlockState state = BuiltInRegistries.BLOCK.get(location.id()).defaultBlockState();
        String variant = location.getVariant();
        if (!variant.isEmpty()) {
            for (String assignment : variant.split(",", -1)) {
                int separator = assignment.indexOf('=');
                if (separator <= 0) {
                    return null;
                }
                Property<?> property = state.getBlock().getStateDefinition().getProperty(assignment.substring(0, separator));
                if (property == null) {
                    return null;
                }
                state = withValue(state, property, assignment.substring(separator + 1));
                if (state == null) {
                    return null;
                }
            }
        }
        // The old index contains only complete canonical variants, not partial or reordered ones.
        return BlockModelShaper.stateToModelLocation(location.id(), state).equals(location) ? state : null;
    }

    private static <T extends Comparable<T>> @Nullable BlockState withValue(BlockState state, Property<T> property, String text) {
        return property.getValue(text).map(value -> state.setValue(property, value)).orElse(null);
    }
}

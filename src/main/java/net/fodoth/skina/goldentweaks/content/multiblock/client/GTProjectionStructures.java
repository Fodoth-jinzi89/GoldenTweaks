package net.fodoth.skina.goldentweaks.content.multiblock.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMultiblockPattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Client copy of the multiblock layouts.
 *
 * <p>The server keeps its own layouts next to the AMI patterns, but a connected client never sees
 * the server's data-pack reload. The structure files ship in this mod, so the client reads them
 * from its own resource manager instead, which is all a projection needs.</p>
 */
public final class GTProjectionStructures extends SimpleJsonResourceReloadListener {

    public static final GTProjectionStructures INSTANCE = new GTProjectionStructures();

    private static final String DIRECTORY = "multiblock/structures";

    private final Map<ResourceLocation, GTMultiblockPattern> layouts = new HashMap<>();

    private GTProjectionStructures() {
        super(new Gson(), DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, @NotNull ResourceManager resourceManager,
                         @NotNull ProfilerFiller profiler) {
        this.layouts.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                this.layouts.put(entry.getKey(), GTMultiblockPattern.parse(entry.getValue()));
            } catch (RuntimeException error) {
                GoldenTweaks.LOGGER.error(
                        "[GT] Failed to load client multiblock structure {}", entry.getKey(), error);
            }
        }
    }

    @Nullable
    public GTMultiblockPattern layout(ResourceLocation id) {
        return this.layouts.get(id);
    }
}

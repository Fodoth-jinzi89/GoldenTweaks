package net.fodoth.skina.goldentweaks.gpubooster.api;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl.VertexBufferCache;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VertexRenderCacheAPI {

    private static final Set<VertexFormat> CACHEABLE = new HashSet<>();

    private VertexRenderCacheAPI() {
    }

    @ApiStatus.Internal
    public static void addCacheableFormat(VertexFormat format) {
        CACHEABLE.add(format);
    }

    @ApiStatus.Internal
    public static void addCacheableFormats(VertexFormat... formats) {
        CACHEABLE.addAll(Arrays.asList(formats));
    }

    public static boolean canBeCached(VertexFormat format) {
        return GoldenTweaksClientConfig.VERTEX_FORMAT_CACHE.get()
                && CACHEABLE.contains(format);
    }

    public static void printDebugInfo() {
        VertexBufferCache.printInfo();
    }
}
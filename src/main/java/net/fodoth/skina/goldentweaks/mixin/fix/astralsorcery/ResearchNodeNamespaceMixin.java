package net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery;

import com.google.gson.JsonElement;
import hellfirepvp.astralsorcery.common.research.data.ResearchNodeLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Astral Sorcery loads its tome research through a {@link ResearchNodeLoader} that is a
 * {@code SimpleJsonResourceReloadListener} on the {@code research} directory, but it never filters by
 * namespace: the vanilla {@code prepare} lists {@code data/<any namespace>/research/**.json}, so every
 * Thaumcraft research node ({@code data/thaumcraft/research/**}, ~200 files) is decoded with Astral
 * Sorcery's own codec, fails, and logs one "Failed to load research node" error per file.
 * <p>
 * Drop the foreign namespaces before they are decoded; Astral Sorcery's own {@code astralsorcery:} nodes
 * are untouched.
 */
@Mixin(value = ResearchNodeLoader.class, remap = false)
public abstract class ResearchNodeNamespaceMixin {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD")
    )
    private void gt$dropForeignNamespaceNodes(Map<ResourceLocation, JsonElement> nodes,
                                              ResourceManager resourceManager,
                                              ProfilerFiller profiler,
                                              CallbackInfo ci) {
        nodes.keySet().removeIf(id -> !"astralsorcery".equals(id.getNamespace()));
    }
}

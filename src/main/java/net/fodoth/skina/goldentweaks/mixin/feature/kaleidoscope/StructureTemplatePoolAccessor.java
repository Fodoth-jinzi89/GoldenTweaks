package net.fodoth.skina.goldentweaks.mixin.feature.kaleidoscope;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> gt$getTemplates();

    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> gt$getRawTemplates();

    @Accessor("rawTemplates")
    void gt$setRawTemplates(
            List<Pair<StructurePoolElement, Integer>> rawTemplates
    );
}
package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import thaumcraft.common.research.ResearchNoteData;

@Mixin(ResearchNoteData.class)
public interface ResearchNoteDataAccessor {
    @Accessor("complete")
    void gt$setComplete(boolean value);
}

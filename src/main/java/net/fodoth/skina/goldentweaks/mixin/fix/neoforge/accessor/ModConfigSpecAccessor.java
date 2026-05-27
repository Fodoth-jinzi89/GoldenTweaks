package net.fodoth.skina.goldentweaks.mixin.fix.neoforge.accessor;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModConfigSpec.class)
public interface ModConfigSpecAccessor {

    @Accessor("loadedConfig")
    IConfigSpec.ILoadedConfig goldentweaks$getLoadedConfig();
}
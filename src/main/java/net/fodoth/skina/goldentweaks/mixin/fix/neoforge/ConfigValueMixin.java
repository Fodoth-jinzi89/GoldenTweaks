package net.fodoth.skina.goldentweaks.mixin.fix.neoforge;

import com.electronwill.nightconfig.core.Config;
import net.fodoth.skina.goldentweaks.mixin.fix.neoforge.accessor.ModConfigSpecAccessor;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ModConfigSpec.ConfigValue.class)
public abstract class ConfigValueMixin<T> {

    @Shadow
    @Final
    private Supplier<T> defaultSupplier;

    @Shadow
    private ModConfigSpec spec;

    @Shadow
    @Final
    private List<String> path;

    @Shadow
    public abstract T getRaw(
            Config config,
            List<String> path,
            Supplier<T> defaultSupplier
    );

    /**
     * @author Fodoth_jinzi89
     * @reason 某些 mod 在并发环境下，会在 config 系统尚未初始化完成时提前访问 ConfigValue。
     * NeoForge 原版会直接崩溃，这里改为 fallback 默认值。
     */
    @Overwrite
    public T getRaw() {

        // spec 尚未构建
        if (this.spec == null) {
            return this.defaultSupplier.get();
        }

        IConfigSpec.ILoadedConfig loadedConfig =
                ((ModConfigSpecAccessor) this.spec)
                        .goldentweaks$getLoadedConfig();

        // config 尚未加载
        if (loadedConfig == null) {
            return this.defaultSupplier.get();
        }

        return this.getRaw(
                loadedConfig.config(),
                this.path,
                this.defaultSupplier
        );
    }
}
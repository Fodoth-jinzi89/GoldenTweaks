package net.fodoth.skina.goldentweaks.mixin.fix.placebo;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
        value = DynamicHolder.class,
        remap = false
)
public abstract class DynamicHolderMixin<R extends CodecProvider<? super R>> {

    @Final
    @Shadow
    protected ResourceLocation id;

    @Shadow
    protected R value;

    @Shadow
    abstract void bind();

    /**
     * @author Fodoth_jinzi89
     * @reason Prevent invalid empty:empty registry references
     * from crashing player data loading after datapack reload.
     */
    @Overwrite
    public R get() {

        this.bind();

        /*
         * Placebo reload bug:
         *
         * After /reload some DynamicHolder references
         * become dangling "empty:empty".
         *
         * Returning null here allows codecs/components
         * to gracefully fail instead of corrupting
         * player login.
         */
        if (this.value == null) {

            if (this.id != null
                    && "empty".equals(this.id.getNamespace())
                    && "empty".equals(this.id.getPath())) {

                return null;
            }

            throw new NullPointerException(
                    "Trying to access unbound value: "
                            + this.id
            );
        }

        return this.value;
    }
}
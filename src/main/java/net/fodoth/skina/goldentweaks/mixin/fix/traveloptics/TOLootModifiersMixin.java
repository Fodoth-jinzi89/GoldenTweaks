package net.fodoth.skina.goldentweaks.mixin.fix.traveloptics;

import com.gametechbc.traveloptics.loot.TOLootModifiers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(TOLootModifiers.class)
public class TOLootModifiersMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/neoforged/neoforge/registries/DeferredRegister;register(Ljava/lang/String;Ljava/util/function/Supplier;)Lnet/neoforged/neoforge/registries/DeferredHolder;"
            )
    )
    private static <T, I extends T> DeferredHolder<T, I> traveloptics$removeDuplicate(
            DeferredRegister<T> instance,
            String name,
            Supplier<? extends I> sup
    ) {

        if (name.equals("universal_loot")) {
            return DeferredHolder.create(
                    instance.getRegistryKey(),
                    ResourceLocation.fromNamespaceAndPath(
                            "traveloptics",
                            "universal_loot"
                    )
            );
        }

        return instance.register(name, sup);
    }
}
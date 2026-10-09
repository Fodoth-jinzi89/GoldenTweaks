package net.fodoth.skina.goldentweaks.mixin.fix.emi;

import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.compat.emi.GTEmiCreativeRegroup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Mixin(value = EmiStackList.class, remap = false)
public class EmiStackListMixin {

    @Inject(
            method = "isHiddenFromRecipeViewers",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T> void goldentweaks$showTravelopticsItems(T value, CallbackInfoReturnable<Boolean> cir) {
        if (value instanceof Item item) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("traveloptics")
                    && !id.getPath().contains("example")
                    && !id.getPath().contains("dummy")) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * 把「一次给所有标签 buildContents」拆成每个标签一次，客户端线程不再一次卡 2.65 秒，
     * 见 {@link GTEmiCreativeRegroup}。
     *
     * <p>目标是 {@code Minecraft#submit(Supplier)} —— EMI 那一趟传的是 {@code Supplier}
     * 而不是 {@code Callable}（按 {@code Callable} 写会扫到 0 个目标、mixin 应用失败直接崩启动）。</p>
     */
    @Redirect(method = "reload",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;submit(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<Map<CreativeModeTab, Collection<ItemStack>>> goldentweaks$chunkedTabPass(
            Minecraft client, Supplier<?> original) {
        return GTEmiCreativeRegroup.tabPass(client, original);
    }

    @Inject(
            method = "reload",
            at = @At("TAIL")
    )
    private static void goldentweaks$makeStacksSafe(CallbackInfo ci) {

        for (Item item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("traveloptics")
                    && !id.getPath().contains("example")
                    && !id.getPath().contains("dummy")) {
                var stack = EmiStack.of(new ItemStack(item));
                if (!EmiStackList.stacks.contains(stack)) {
                    EmiStackList.stacks.add(stack);
                }
            }
        }

        EmiStackList.stacks =
                Collections.synchronizedList(
                        EmiStackList.stacks
                );
    }

    @Inject(
            method = "bake",
            at = @At("HEAD")
    )
    private static void goldentweaks$copyStacks(CallbackInfo ci) {

        EmiStackList.stacks =
                new CopyOnWriteArrayList<>(
                        EmiStackList.stacks
                );

    }
}

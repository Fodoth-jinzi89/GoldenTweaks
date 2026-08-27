package net.fodoth.skina.goldentweaks.mixin.fix.fancymenu;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedList;
import java.util.List;

@Pseudo
@Mixin(targets = "de.keksuccino.fancymenu.util.threading.MainThreadTaskExecutor", remap = false)
public class MainThreadTaskExecutorMixin {

    @ModifyReturnValue(method = "getAndClearQueue", at = @At("RETURN"))
    private static List<Runnable> goldentweaks$avoidCorruptedArrayList(List<Runnable> original) {
        return new LinkedList<>(original);
    }
}

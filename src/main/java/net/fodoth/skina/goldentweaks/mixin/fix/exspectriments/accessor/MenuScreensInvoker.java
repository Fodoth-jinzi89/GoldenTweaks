package net.fodoth.skina.goldentweaks.mixin.fix.exspectriments.accessor;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MenuScreens.class)
public interface MenuScreensInvoker {

    @Invoker("register")
    static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void gt$register(
            MenuType<? extends M> menuType,
            MenuScreens.ScreenConstructor<M, U> screenConstructor
    ) {
        throw new AssertionError();
    }
}

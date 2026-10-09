package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.api.contraption.storage.item.menu.MountedStorageMenus;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.PaddedStorageContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MountedStorageMenus.class)
public abstract class MountedStorageMenusMixin {

    // Create 的通用挂载菜单只接受 9 的倍数槽位；限量木桶等 1~8 槽容器会拿不到菜单而无法右键打开。
    // 这里把任意槽位数补齐到 9 的倍数（最多 54），使其可打开。
    @Inject(method = "createGeneric", at = @At("HEAD"), cancellable = true)
    private static void gt$padNonMultipleOfNine(Component name, IItemHandlerModifiable handler,
                                                Predicate<Player> stillValid, Consumer<Player> onClose,
                                                CallbackInfoReturnable<MenuProvider> cir) {
        int slots = handler.getSlots();
        if (slots <= 0 || slots % 9 == 0 || slots > 54) {
            return;
        }
        int rows = (slots + 8) / 9;
        MenuType<?> menuType = MountedStorageMenus.GENERIC_CHEST_MENUS.get(rows - 1);
        PaddedStorageContainer container = new PaddedStorageContainer(handler, rows * 9, stillValid, onClose);
        cir.setReturnValue(new SimpleMenuProvider(
                (id, inventory, player) -> new ChestMenu(menuType, id, inventory, container, rows),
                name));
    }
}

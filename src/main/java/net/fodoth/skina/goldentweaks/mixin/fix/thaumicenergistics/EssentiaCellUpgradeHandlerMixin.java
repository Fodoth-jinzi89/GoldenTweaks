package net.fodoth.skina.goldentweaks.mixin.fix.thaumicenergistics;

import appeng.api.upgrades.IUpgradeInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

/**
 * 修复神秘能源「源质元件工作台」升级槽导致的客户端掉线。
 *
 * <p>该容器把 {@code CellUpgradeHandler}（只实现 {@code IItemHandler}）交给
 * {@code SlotItemHandler}。服务端同步容器内容时 {@code SlotItemHandler#set} 会把它强转成
 * {@code IItemHandlerModifiable}，于是客户端一打开工作台就抛
 * {@code ClassCastException: ...CellUpgradeHandler cannot be cast to ...IItemHandlerModifiable} 并断开连接。</p>
 *
 * <p>这里给 {@code CellUpgradeHandler} 补上 {@code IItemHandlerModifiable}，{@code setStackInSlot}
 * 转发给 AE2 的 {@code IUpgradeInventory#setItemDirect}（空实现也是安全空操作）。</p>
 */
@Mixin(targets = "thaumicenergistics.common.container.ContainerEssentiaCellWorkbench$CellUpgradeHandler", remap = false)
public abstract class EssentiaCellUpgradeHandlerMixin implements IItemHandlerModifiable {

    @Shadow
    @Final
    private Supplier<IUpgradeInventory> supplier;

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        supplier.get().setItemDirect(slot, stack);
    }
}

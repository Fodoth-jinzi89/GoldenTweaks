package net.fodoth.skina.goldentweaks.mixin.fix.thaumicenergistics;

import appeng.api.upgrades.IUpgradeInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumicenergistics.common.container.ContainerEssentiaCellWorkbench;
import thaumicenergistics.common.items.ItemEssentiaCell;
import thaumicenergistics.common.tiles.TileEssentiaCellWorkbench;

/**
 * 让「源质元件工作台」客户端也能显示升级卡。
 *
 * <p>升级槽的内容来自 {@code currentCellUpgrades()}，但它只从方块实体 {@code workbench}
 * 取元件；而客户端的菜单没有方块实体（{@code workbench == null}），于是升级槽永远是空的
 * （曾配合 {@link EssentiaCellUpgradeHandlerMixin} 修掉的那次掉线一起表现）。</p>
 *
 * <p>客户端槽位 0 的 {@code fallbackCell} 里同步着服务端发来的元件物品栈，升级数据就存在
 * 该物品栈上，因此这里在 {@code workbench} 为空时改用槽位 0 的元件重建升级库存。</p>
 */
@Mixin(value = ContainerEssentiaCellWorkbench.class, remap = false)
public abstract class EssentiaCellWorkbenchMenuMixin {

    @Shadow
    @Final
    private TileEssentiaCellWorkbench workbench;

    @Shadow
    @Final
    private IItemHandler fallbackCell;

    @Inject(method = "currentCellUpgrades", at = @At("HEAD"), cancellable = true, remap = false)
    private void gt$clientCellUpgrades(CallbackInfoReturnable<IUpgradeInventory> cir) {
        if (workbench != null) {
            return;
        }
        ItemStack cell = fallbackCell.getStackInSlot(0);
        if (cell.getItem() instanceof ItemEssentiaCell cellItem) {
            cir.setReturnValue(cellItem.getUpgrades(cell));
        }
    }
}

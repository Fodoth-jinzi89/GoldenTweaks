package net.fodoth.skina.goldentweaks.mixin.fix.ldlib2;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 ldlib2 的 {@code ItemSlot} 调用 preApply，
 * 由 {@code Ldlib2ASM} 在专用服务器上把客户端专属的 {@code xeiPhantom()} 清成 {@code return this}
 * （它只注册 JEI/REI/EMI 的拖拽/幽灵槽；服务端调用会加载客户端类 Renderable 而崩服）。
 */
@Mixin(targets = "com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot", remap = false)
public abstract class ItemSlotDummyMixin {
}

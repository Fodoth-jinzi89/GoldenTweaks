package net.fodoth.skina.goldentweaks.mixin.fix.cqa;

import org.spongepowered.asm.mixin.Mixin;

/**
 * 空 mixin：唯一作用是让 mixin 插件对 certain_questing_additions 的「实体图标」类调用 preApply，
 * 由 {@code CqaClientIconASM} 在专用服务器上清空其中引用 {@code net.minecraft.client.*} 的渲染方法。
 *
 * <p>这些类被 CQA 的 {@code IconMixin} 从 {@code Icon.getIcon} 无条件拉进来，只要服务端解析一次图标
 * 就会崩服（详见 {@code CqaClientIconASM} 的注释）。</p>
 */
@Mixin(targets = {
        "ru.hollowhorizon.additions.questing.client.EntityIcon",
        "ru.hollowhorizon.additions.questing.client.EntityIconLighting",
        "ru.hollowhorizon.additions.questing.client.InvalidEntityIcon"
}, remap = false)
public abstract class EntityIconDummyMixin {
}

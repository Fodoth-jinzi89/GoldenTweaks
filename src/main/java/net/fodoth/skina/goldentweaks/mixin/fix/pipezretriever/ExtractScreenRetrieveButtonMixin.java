package net.fodoth.skina.goldentweaks.mixin.fix.pipezretriever;

import com.lumengrid.pipezretriever.IRetrieveMode;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.ExtractScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pipez Retriever 在管道界面上方加了一颗 Extract/Retrieve 模式按钮（60x20，贴在 GUI 上沿外 22 像素处，
 * 默认落在左侧 {@code leftPos + 32}）。这里把它右移到紧贴 GUI 右边框，并换成可翻译文本，
 * 使中文下显示「抽取 / 取回」而不是硬编码的英文。
 * <p>
 * 全部改动放在 {@code renderBg}（每帧画背景时，早于任何控件绘制）里完成：Retriever 自己用
 * {@code init} / {@code containerTick} 的 TAIL 注入设置按钮位置与文字，而本 mixin 与它的注入先后
 * 顺序无法保证，只有在这里覆写才能保证本帧显示的一定是我们设置的文本；位置同理，逐帧对齐一次，
 * 无论对方的 {@code init} 注入先于还是后于本 mixin 都成立。
 */
@Mixin(value = ExtractScreen.class, remap = false)
public abstract class ExtractScreenRetrieveButtonMixin extends AbstractContainerScreen<ExtractContainer> {

    @Unique
    private Button goldentweaks$retrieveButton;

    protected ExtractScreenRetrieveButtonMixin() {
        super(null, null, null);
    }

    @Inject(method = "renderBg", at = @At("HEAD"), remap = false)
    private void goldentweaks$alignRetrieveButton(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        Button button = goldentweaks$retrieveButton;
        if (button == null || !this.children().contains(button)) {
            button = goldentweaks$findRetrieveButton();
            goldentweaks$retrieveButton = button;
        }
        if (button == null) {
            return;
        }

        button.setX(this.leftPos + this.imageWidth - button.getWidth());

        ExtractContainer container = this.menu;
        PipeLogicTileEntity pipe = container.getPipe();
        boolean retrieving = pipe instanceof IRetrieveMode mode && mode.isRetrieving(container.getSide());
        button.setMessage(Component.translatable(retrieving
                ? "goldentweaks.pipezretriever.mode.retrieve"
                : "goldentweaks.pipezretriever.mode.extract"));
    }

    @Unique
    private Button goldentweaks$findRetrieveButton() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button
                    && button.getWidth() == 60
                    && button.getHeight() == 20
                    && button.getY() == this.topPos - 22) {
                return button;
            }
        }
        return null;
    }
}

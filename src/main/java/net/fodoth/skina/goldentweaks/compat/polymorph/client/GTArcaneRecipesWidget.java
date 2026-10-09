package net.fodoth.skina.goldentweaks.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import net.fodoth.skina.goldentweaks.compat.polymorph.GTArcaneRecipes;
import net.fodoth.skina.goldentweaks.network.packet.C2SSelectArcaneRecipePacket;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 奥术工作台 / 奥术合成终端的选择控件。
 *
 * <p>与 Polymorph 默认（按钮在产物格上方、选择列表向上展开）不同，这里把按钮放到
 * <b>产物格下方</b>，选择列表随之向下展开。奥术配方不在 {@code RecipeManager} 里，
 * 选择结果走 GoldenTweaks 自己的 C2S 通道；原版配方仍交给 Polymorph 原生处理。</p>
 */
public class GTArcaneRecipesWidget extends AbstractRecipesWidget {

    /** 选择列表相对按钮的偏移；按钮在产物格下方，故列表向下展开。 */
    private static final int LIST_X_OFFSET = -4;
    private static final int LIST_Y_OFFSET = 22;

    private final Slot outputSlot;

    public GTArcaneRecipesWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
        super(screen, LIST_X_OFFSET, LIST_Y_OFFSET);
        this.outputSlot = outputSlot;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }

    /** 产物格下方 22 像素（Polymorph 默认为上方 22 像素）。 */
    @Override
    public int getYPos() {
        return outputSlot.y + 22;
    }

    @Override
    public void selectRecipe(ResourceLocation id) {
        if (GTArcaneRecipes.isSynthetic(id)) {
            PacketDistributor.sendToServer(new C2SSelectArcaneRecipePacket(id));
        } else {
            PolymorphApi.getInstance().getNetwork().sendPlayerRecipeSelectionC2S(id);
        }
    }
}

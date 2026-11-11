package net.fodoth.skina.goldentweaks.compat.thermal;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * 光谱结晶配方「转换到热力结晶器」的 JEI 页：晶种 + 培养液（+ 可选添加剂）→ 结晶簇方块。
 *
 * <p>原版那套墨水与生长阶段在转换里都不存在（不消耗墨水、培养液只需存在），页面上用几行短注释标明；
 * 真正能否运行取决于是否装了「组件：光谱墨法结晶」，见 {@code ThermalCrystallarieumAugment}。</p>
 *
 * <p>这个类只在客户端加载（{@code IRecipeCategory} 的方法签名带 {@code GuiGraphics}），
 * 所以只有 {@code registerCategories} 会碰它。</p>
 */
final class GTCrystallarieumJeiCategory implements IRecipeCategory<GTCrystallarieumDisplayRecipe> {

    private static final String KEY = "goldentweaks.crystallarieum_crystallizer.";

    private static final int WIDTH = 150;
    /** 高度比内容多留 5px：最后一行的字底正好压在边框上，向下放宽一点。 */
    private static final int HEIGHT = 97;
    private static final int SLOT_INNER = 16;

    private static final int SEED_X = 6;
    private static final int FLUID_X = 28;
    private static final int ROW_Y = 6;
    private static final int ARROW_X = 52;
    private static final int ARROW_Y = 7;
    private static final int ARROW_WIDTH = 22;
    private static final int ARROW_HEIGHT = 16;
    private static final int RESULT_X = 84;

    private static final int LABEL_X = 6;
    private static final int LABEL_Y = 32;
    private static final int ADDITIVE_Y = 44;
    private static final int ADDITIVE_GAP = 22;

    private static final int NOTE_X = 6;
    private static final int NOTE_Y = 68;
    private static final int NOTE_STEP = 10;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    private GTCrystallarieumJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ThermalCrystallarieumAugment.CRYSTALLARIEUM_GROWING_AUGMENT.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    static void register(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new GTCrystallarieumJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public RecipeType<GTCrystallarieumDisplayRecipe> getRecipeType() {
        return GTCrystallarieumJeiPlugin.TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(KEY + "category");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTCrystallarieumDisplayRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, SEED_X, ROW_Y).addIngredients(recipe.seed());

        if (!recipe.fluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, FLUID_X, ROW_Y)
                    .setFluidRenderer(GTCrystallarieumDisplayRecipe.FLUID_AMOUNT, false, SLOT_INNER, SLOT_INNER)
                    .addFluidStack(recipe.fluid().getFluid(), GTCrystallarieumDisplayRecipe.FLUID_AMOUNT);
        }

        List<Ingredient> additives = recipe.additives();
        for (int i = 0; i < additives.size() && i < GTCrystallarieumDisplayRecipe.MAX_ADDITIVES; i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, SEED_X + i * ADDITIVE_GAP, ADDITIVE_Y)
                    .addIngredients(additives.get(i));
        }

        if (!recipe.result().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y).addItemStack(recipe.result());
        }
    }

    @Override
    public void draw(
            GTCrystallarieumDisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        this.arrow.draw(guiGraphics, ARROW_X, ARROW_Y);

        var font = Minecraft.getInstance().font;
        if (!recipe.additives().isEmpty()) {
            guiGraphics.drawString(font, Component.translatable(KEY + "additives"), LABEL_X, LABEL_Y, 0x404040, false);
        }

        String[] notes = {KEY + "note.ink", KEY + "note.fluid", KEY + "note.augment"};
        for (int i = 0; i < notes.length; i++) {
            guiGraphics.drawString(font, Component.translatable(notes[i]), NOTE_X, NOTE_Y + i * NOTE_STEP, 0x404040, false);
        }
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            GTCrystallarieumDisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_WIDTH
                && mouseY >= ARROW_Y && mouseY < ARROW_Y + ARROW_HEIGHT) {
            tooltip.add(Component.translatable(KEY + "converted"));
        }
    }
}

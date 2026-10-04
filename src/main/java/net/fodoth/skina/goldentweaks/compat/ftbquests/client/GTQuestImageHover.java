package net.fodoth.skina.goldentweaks.compat.ftbquests.client;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 「当前鼠标下那张章节图片」的绑定配方 id。
 *
 * <p>{@code ChapterImageButton#getIngredientUnderMouse()} 返回的就是 FTB 认定「鼠标下那一个」控件，
 * 所以在那里面登记即可：键鼠处理再去查时，只有「同一个 ingredient + 刚登记过」才算数，避免鼠标
 * 已经移开、换到别的控件上时还在用上一张图片的绑定。只在客户端渲染/交互线程使用。</p>
 */
public final class GTQuestImageHover {

    private static final long FRESH_MILLIS = 250L;

    private static ItemStack item = ItemStack.EMPTY;
    private static FluidStack fluid = FluidStack.EMPTY;
    private static String recipeId = "";
    private static String usesId = "";
    private static long stamp;

    private GTQuestImageHover() {
    }

    public static void remember(ItemStack item, FluidStack fluid, String recipeId, String usesId) {
        GTQuestImageHover.item = item;
        GTQuestImageHover.fluid = fluid;
        GTQuestImageHover.recipeId = recipeId == null ? "" : recipeId;
        GTQuestImageHover.usesId = usesId == null ? "" : usesId;
        GTQuestImageHover.stamp = System.currentTimeMillis();
    }

    public static String recipeIdFor(Object ingredient) {
        return matches(ingredient) ? recipeId : "";
    }

    public static String usesIdFor(Object ingredient) {
        return matches(ingredient) ? usesId : "";
    }

    private static boolean matches(Object ingredient) {
        if (System.currentTimeMillis() - stamp > FRESH_MILLIS) {
            return false;
        }
        if (ingredient instanceof ItemStack stack && !stack.isEmpty()) {
            return !item.isEmpty() && ItemStack.matches(stack, item);
        }
        if (ingredient instanceof FluidStack stack && !stack.isEmpty()) {
            return !fluid.isEmpty() && FluidStack.matches(stack, fluid);
        }
        return false;
    }
}

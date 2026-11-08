package net.fodoth.skina.goldentweaks.mixin.fix.ae2fct;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEFluidKey;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.StackWithBounds;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.BoMScreen;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import me.myogoo.ae2fct.codec.VirtualFluid;
import me.myogoo.ae2fct.config.FluidCraftingConfig;
import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import me.myogoo.ae2fct.util.FluidCraftingHelper;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * ae2fct 的「虚拟流体物品 → 桶/流体配方查询」集成重写（原本由 ae2fct 的
 * {@code emi.EmiApiAccessor} + {@code emi.EmiScreenManagerMixin} 提供）。
 *
 * <p>原实现坏在 {@code EmiApiAccessor}：它是 {@code @Mixin(EmiApi.class)} 的 <b>接口</b>，
 * 用静态 {@code @Invoker} 去取 {@code EmiApi} 的私有静态方法（{@code pruneUses} /
 * {@code mapRecipes} / {@code setPages}）。运行时 Mixin 把调用点改写成了
 * {@code EmiApi.pruneUses$ae2fct_$md$…} 这个名字，却<b>没有把对应方法注入 {@code EmiApi}</b>，
 * 于是点击 EMI 里的虚拟流体物品时 {@code NoSuchMethodError}（latest-10.29-6.log 行 8538 起）。</p>
 *
 * <p>那名字带 Mixin 的运行时计数、无法复现，而 Mixin 又拒绝把别的模组的 mixin 类当作注入目标
 * （{@code ClassInfo}: {@code Cannot add target … because the target is a mixin}），所以没法在 GT 侧
 * 补上那个方法。这里改成用 EMI 的<b>公开</b> API 重新实现同一套行为：注入移到
 * {@code EmiScreenManager.stackInteraction} 的 HEAD（和 ae2fct 原来的注入点一致），命中虚拟流体
 * 物品 / AE2 终端里的流体键时，把「桶物品」和「流体本身」合并成一页显示。</p>
 *
 * <p>与 ae2fct 原版的差异：多目标时不再调用私有的 {@code pruneUses}（按「最小出现次数」过滤+排序），
 * 只用 {@link LinkedHashSet} 去重——配方集合本身已按各目标的输入/输出查得，展示内容一致，
 * 仅同类别内排序可能不同。{@code setPages} 的等价逻辑见 {@link #gt$openPages}。</p>
 *
 * <p>实例包须按 {@code etc/ae2fct.mixins.json} 停用 ae2fct 的 {@code emi.EmiApiAccessor} 与
 * {@code emi.EmiScreenManagerMixin}，否则两套会同时生效。</p>
 */
@Mixin(EmiScreenManager.class)
public abstract class EmiScreenManagerFluidTargetsMixin {

    @Unique
    private static boolean gt$ae2fctFailureLogged;

    @Inject(method = "stackInteraction", at = @At("HEAD"), cancellable = true)
    private static void gt$showFluidTargets(EmiStackInteraction interaction,
                                            Function<EmiBind, Boolean> function,
                                            CallbackInfoReturnable<Boolean> cir) {
        try {
            gt$handleFluidTargets(interaction, function, cir);
        } catch (Throwable throwable) {
            if (!gt$ae2fctFailureLogged) {
                gt$ae2fctFailureLogged = true;
                GoldenTweaks.LOGGER.warn(
                        "[GT] ae2fct fluid lookup failed; falling back to EMI's own handling", throwable);
            }
        }
    }

    @Unique
    private static void gt$handleFluidTargets(EmiStackInteraction interaction,
                                              Function<EmiBind, Boolean> function,
                                              CallbackInfoReturnable<Boolean> cir) {

        EmiIngredient hovered = interaction.getStack();
        if (hovered.getEmiStacks().isEmpty()) {
            return;
        }
        ItemStack stack = hovered.getEmiStacks().get(0).getItemStack();

        FluidStack fluidStack;
        boolean virtualFluid;
        boolean showRecipes;
        boolean showUses;

        if (FluidCraftingHelper.isVirtualFluidItem(stack)
                && stack.has(AE2FCTDataComponent.VIRTUAL_FLUID)) {
            VirtualFluid virtualFluidData = stack.get(AE2FCTDataComponent.VIRTUAL_FLUID);
            if (virtualFluidData == null || virtualFluidData.fluid().isEmpty()) {
                return;
            }
            fluidStack = virtualFluidData.fluid();
            virtualFluid = true;
            showRecipes = Boolean.TRUE.equals(function.apply(EmiConfig.viewRecipes));
            showUses = Boolean.TRUE.equals(function.apply(EmiConfig.viewUses));
        } else {
            virtualFluid = false;
            fluidStack = gt$getAeFluidStackUnderMouse(hovered);

            if (!fluidStack.isEmpty()) {
                showRecipes = gt$matchesKeyboardBind(function, EmiConfig.viewRecipes);
                showUses = gt$matchesKeyboardBind(function, EmiConfig.viewUses);
                if (!showRecipes && !showUses) {
                    // 该流体键只绑了鼠标键：交给 EMI 自己按鼠标处理，但不要走 vanilla 的「无配方」提示
                    if (Boolean.TRUE.equals(function.apply(EmiConfig.viewRecipes))
                            || Boolean.TRUE.equals(function.apply(EmiConfig.viewUses))) {
                        cir.setReturnValue(false);
                    }
                    return;
                }
                if (!FluidCraftingConfig.showBucketRecipesForAe2FluidKeys()) {
                    cir.setReturnValue(false);
                    return;
                }
            } else {
                fluidStack = gt$getNativeFluidStack(hovered);
                if (fluidStack.isEmpty() || !FluidCraftingConfig.showBucketRecipesForAe2FluidKeys()) {
                    return;
                }
                showRecipes = Boolean.TRUE.equals(function.apply(EmiConfig.viewRecipes));
                showUses = Boolean.TRUE.equals(function.apply(EmiConfig.viewUses));
            }
        }

        List<EmiIngredient> targets = gt$createTargetIngredients(fluidStack, virtualFluid);
        if (targets.isEmpty()) {
            return;
        }

        if (showRecipes) {
            gt$displayRecipes(targets);
            cir.setReturnValue(true);
            return;
        }
        if (showUses) {
            gt$displayUses(targets);
            cir.setReturnValue(true);
        }
    }

    /** 悬停的 AE2 物品键与 AE2 终端里鼠标下的流体键指向同一种流体时返回该流体，否则返回空。 */
    @Unique
    private static FluidStack gt$getAeFluidStackUnderMouse(EmiIngredient hovered) {

        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AEBaseScreen<?> aeScreen) {
            StackWithBounds underMouse =
                    aeScreen.getStackUnderMouse(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY);
            if (underMouse != null) {
                AEKey key = underMouse.stack().what();
                if (key instanceof AEFluidKey fluidKey) {
                    FluidStack candidate = fluidKey.toStack(1000);
                    EmiStack emiStack = NeoForgeEmiStack.of(candidate);
                    if (hovered.getEmiStacks().stream().anyMatch(s -> s.isEqual(emiStack))) {
                        return candidate;
                    }
                }
            }
        }
        return FluidStack.EMPTY;
    }

    /** 悬停的堆栈本身就是流体（EMI 的流体堆栈）时，把它还原成 1000mB 的 {@link FluidStack}。 */
    @Unique
    private static FluidStack gt$getNativeFluidStack(EmiIngredient hovered) {

        EmiStack emiStack = hovered.getEmiStacks().get(0);
        Object key = emiStack.getKey();
        if (key instanceof Fluid fluid) {
            return new FluidStack(fluid.builtInRegistryHolder(), 1000, emiStack.getComponentChanges());
        }
        return FluidStack.EMPTY;
    }

    /**
     * 该流体键是否只绑了键盘键（KEYSYM / SCANCODE）且此刻被按下。
     *
     * <p>ae2fct 原版行为：鼠标键由 EMI 自己处理，只有键盘绑定才由这里接管。</p>
     */
    @Unique
    private static boolean gt$matchesKeyboardBind(Function<EmiBind, Boolean> function, EmiBind source) {

        EmiBind.ModifiedKey[] keys = source.boundKeys.stream()
                .filter(key -> !key.isUnbound())
                .filter(key -> {
                    InputConstants.Type type = key.key().getType();
                    return type == InputConstants.Type.KEYSYM || type == InputConstants.Type.SCANCODE;
                })
                .toArray(EmiBind.ModifiedKey[]::new);

        if (keys.length > 0) {
            return Boolean.TRUE.equals(function.apply(new EmiBind(source.translationKey, keys)));
        }
        return false;
    }

    /** 查询目标：桶物品（可选）+ 流体本身（可选），与 ae2fct 的配置开关一致。 */
    @Unique
    private static List<EmiIngredient> gt$createTargetIngredients(FluidStack fluid, boolean virtualFluid) {

        List<EmiIngredient> targets = new ArrayList<>();

        boolean showBucket = virtualFluid
                ? FluidCraftingConfig.showBucketRecipesForVirtualFluids()
                : FluidCraftingConfig.showBucketRecipesForAe2FluidKeys();
        boolean showFluid = !virtualFluid || FluidCraftingConfig.showFluidRecipesForVirtualFluids();

        if (showBucket) {
            ItemStack bucket = FluidUtil.getFilledBucket(fluid);
            if (!bucket.isEmpty()) {
                targets.add(EmiStack.of(bucket));
            }
        }
        if (showFluid) {
            targets.add(NeoForgeEmiStack.of(fluid));
        }
        return targets;
    }

    @Unique
    private static void gt$displayRecipes(List<EmiIngredient> targets) {

        if (targets.size() == 1) {
            EmiApi.displayRecipes(targets.get(0));
            return;
        }

        Set<EmiRecipe> recipes = new LinkedHashSet<>();
        for (EmiIngredient target : targets) {
            for (EmiStack stack : target.getEmiStacks()) {
                recipes.addAll(EmiApi.getRecipeManager().getRecipesByOutput(stack));
            }
        }
        gt$openPages(gt$mapRecipes(new ArrayList<>(recipes)), targets.get(0));
    }

    @Unique
    private static void gt$displayUses(List<EmiIngredient> targets) {

        if (targets.size() == 1) {
            EmiApi.displayUses(targets.get(0));
            return;
        }

        Set<EmiRecipe> recipes = new LinkedHashSet<>();
        for (EmiIngredient target : targets) {
            for (EmiStack stack : target.getEmiStacks()) {
                recipes.addAll(EmiApi.getRecipeManager().getRecipesByInput(stack));
            }
        }
        gt$openPages(gt$mapRecipes(new ArrayList<>(recipes)), targets.get(0));
    }

    @Unique
    private static Map<EmiRecipeCategory, List<EmiRecipe>> gt$mapRecipes(List<EmiRecipe> recipes) {

        Map<EmiRecipeCategory, List<EmiRecipe>> pages = new LinkedHashMap<>();
        for (EmiRecipe recipe : recipes) {
            pages.computeIfAbsent(recipe.getCategory(), category -> new ArrayList<>()).add(recipe);
        }
        return pages;
    }

    /** {@code EmiApi.setPages} 的等价实现，只用公开 API（RecipeScreen 的构造器是 public）。 */
    @Unique
    private static void gt$openPages(Map<EmiRecipeCategory, List<EmiRecipe>> pages, EmiIngredient focus) {

        Map<EmiRecipeCategory, List<EmiRecipe>> nonEmpty = new LinkedHashMap<>();
        for (Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : pages.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                nonEmpty.put(entry.getKey(), entry.getValue());
            }
        }
        if (nonEmpty.isEmpty()) {
            return;
        }

        EmiSidebars.lookup(focus);

        Minecraft client = Minecraft.getInstance();
        if (EmiApi.getHandledScreen() == null) {
            client.setScreen(new InventoryScreen(client.player));
        }

        Screen screen = client.screen;
        if (screen instanceof AbstractContainerScreen<?> container) {
            gt$push();
            client.setScreen(new RecipeScreen(container, nonEmpty));
        } else if (screen instanceof BoMScreen bom) {
            gt$push();
            client.setScreen(new RecipeScreen(bom.old, nonEmpty));
        } else if (screen instanceof RecipeScreen current) {
            gt$push();
            RecipeScreen next = new RecipeScreen(current.old, nonEmpty);
            client.setScreen(next);
            next.focusCategory(current.getFocusedCategory());
        }
    }

    @Unique
    private static void gt$push() {

        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof RecipeScreen || screen instanceof BoMScreen) {
            EmiHistory.push(screen);
        } else {
            EmiHistory.clear();
        }
    }
}

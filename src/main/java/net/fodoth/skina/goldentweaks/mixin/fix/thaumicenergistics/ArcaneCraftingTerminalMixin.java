package net.fodoth.skina.goldentweaks.mixin.fix.thaumicenergistics;

import com.llamalad7.mixinextras.sugar.Local;
import net.fodoth.skina.goldentweaks.compat.polymorph.PolymorphArcaneCompat;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumicenergistics.common.container.ArcaneTerminalRecipeState;

import java.util.Optional;

/**
 * 给神秘能源（Thaumic Energistics）的「奥术合成终端」接上 Polymorph。
 *
 * <p>2.6.4 起，终端不再在 {@code ContainerArcaneCraftingTerminal#updateCurrentRecipeAndOutput}
 * 里直接查配方，而是统一走 {@link ArcaneTerminalRecipeState#resolve}：先 {@code findMatchingArcaneRecipe}，
 * 没有再退回原版 {@code RecipeManager#getRecipeFor}。这里在奥术查询前重建候选项、在选择列表就绪后
 * 交给 Polymorph，并在原版分支上做同样的多态选择。</p>
 *
 * <p>{@code require = 0}：这两处都是对上游内部调用的重定向，将来再重构时宁可这层兼容失效，
 * 也不要让 {@code InjectionError} 把整个启动崩掉。</p>
 *
 * <p>仅在同时装了 thaumicenergistics 与 Polymorph 时启用（见 {@code GoldenTweaksMixinPlugin}）。</p>
 */
@Mixin(value = ArcaneTerminalRecipeState.class, remap = false)
public abstract class ArcaneCraftingTerminalMixin {

    @Redirect(
            method = "resolve",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/lib/crafting/ThaumcraftCraftingManager;findMatchingArcaneRecipe(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
            ),
            require = 0
    )
    private static ItemStack gt$polymorphArcaneRefresh(Container workbench, Player player) {
        PolymorphArcaneCompat.refresh(player, workbench);
        return ThaumcraftCraftingManager.findMatchingArcaneRecipe(workbench, player);
    }

    @SuppressWarnings("unchecked")
    @Redirect(
            method = "resolve",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"
            ),
            require = 0
    )
    private static Optional<RecipeHolder<CraftingRecipe>> gt$polymorphVanillaRecipe(
            RecipeManager recipeManager, RecipeType<?> type, RecipeInput input, Level level,
            @Local(argsOnly = true) Player player) {
        return PolymorphArcaneCompat.selectVanilla(player, recipeManager,
                (RecipeType<CraftingRecipe>) type, (CraftingInput) input, level);
    }
}

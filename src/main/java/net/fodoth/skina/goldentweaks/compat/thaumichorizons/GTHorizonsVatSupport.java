package net.fodoth.skina.goldentweaks.compat.thaumichorizons;

import com.kentington.thaumichorizons.common.vat.VatBlockEntity;
import com.kentington.thaumichorizons.common.vat.VatMatrixBlockEntity;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.blockentities.PedestalBlockEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.List;

/**
 * Support for the Thaumic Horizons "Modified Runic Matrix" ({@code thaumichorizons:modmatrix}).
 *
 * <p>That block is not a drop-in replacement for the vanilla Runic Matrix: it sits on top of an
 * Infusion Vat ({@link VatBlockEntity}) and starts a vat infusion when a wand right-clicks it.
 * The vat pulls its own offerings from the surrounding pedestals and draws essentia from its
 * internal tank, so this class only has to (1) recognise the matrix, (2) hand essentia to the
 * vat, (3) start the infusion for the owner and (4) fast-forward the offering phase.
 *
 * <p>Every Thaumic Horizons reference lives here so the intercepter keeps working when only
 * Thaumcraft is installed — callers must check {@code ModList.isLoaded("thaumichorizons")}
 * before touching this class. The {@code VatInfusion} internals are reached through
 * MethodHandles and every entry point degrades to a no-op if a handle is unavailable, which
 * leaves the vat's normal behaviour untouched.
 */
public final class GTHorizonsVatSupport {

    private GTHorizonsVatSupport() {
    }

    /* ------------------------------------------------------ */
    /* Matrix / vat access                                     */
    /* ------------------------------------------------------ */

    public static boolean isVatMatrix(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof VatMatrixBlockEntity;
    }

    @Nullable
    private static VatBlockEntity vat(@Nullable BlockEntity matrix) {
        return matrix instanceof VatMatrixBlockEntity vatMatrix ? vatMatrix.vat() : null;
    }

    public static boolean isInfusing(@Nullable BlockEntity matrix) {
        VatBlockEntity vat = vat(matrix);
        return vat != null && vat.infusing();
    }

    public static boolean needsEssentia(@Nullable BlockEntity matrix) {
        VatBlockEntity vat = vat(matrix);
        return vat != null && vat.needsEssentia();
    }

    /** @return how much of {@code amount} the vat would take right now (0 when it wants none of it) */
    public static int acceptEssentia(@Nullable BlockEntity matrix, Aspect aspect, int amount) {
        VatBlockEntity vat = vat(matrix);
        return vat == null ? 0 : vat.simulateAdd(aspect, amount);
    }

    /** @return how much of {@code amount} the vat actually took */
    public static int addEssentia(@Nullable BlockEntity matrix, Aspect aspect, int amount) {
        VatBlockEntity vat = vat(matrix);
        return vat == null ? 0 : vat.add(aspect, amount);
    }

    public static boolean startInfusion(@Nullable BlockEntity matrix, Player player) {
        VatBlockEntity vat = vat(matrix);
        return vat != null && vat.startInfusion(player);
    }

    /* ------------------------------------------------------ */
    /* Instability borrowing                                   */
    /* ------------------------------------------------------ */

    /**
     * Takes up to {@code amount} instability off the vat infusion (the vat rolls accidents once
     * per cycle against that value), mirroring what the intercepter does to a vanilla matrix.
     *
     * @return the amount actually borrowed, to be handed back by {@link #restoreInstability}
     */
    public static int borrowInstability(@Nullable BlockEntity matrix, int amount) {
        Object infusion = infusion(matrix);
        VarHandle handle = Handles.INFUSION_INSTABILITY;
        if (infusion == null || handle == null) return 0;

        int current = (int) handle.get(infusion);
        int borrowed = Math.min(amount, Math.max(0, current));
        if (borrowed > 0) handle.set(infusion, current - borrowed);
        return borrowed;
    }

    public static void restoreInstability(@Nullable BlockEntity matrix, int amount) {
        if (amount <= 0) return;
        Object infusion = infusion(matrix);
        VarHandle handle = Handles.INFUSION_INSTABILITY;
        if (infusion == null || handle == null) return;

        handle.set(infusion, (int) handle.get(infusion) + amount);
    }

    /* ------------------------------------------------------ */
    /* Offering fast-forward                                   */
    /* ------------------------------------------------------ */

    /**
     * Consumes every offering the running infusion still expects from the vat's pedestals in one
     * go, instead of the vat's own five-cycles-per-offering animation, and marks the matching
     * bit in the infusion's consumed bitmask. Crafting remainders are handled exactly like the
     * vat does it.
     *
     * @return the number of offerings consumed
     */
    public static int consumeOfferings(@Nullable BlockEntity matrix) {
        Object infusion = infusion(matrix);
        Level level = matrix == null ? null : matrix.getLevel();
        VarHandle consumedHandle = Handles.INFUSION_CONSUMED;
        VarHandle recipeHandle = Handles.INFUSION_RECIPE;
        VarHandle pedestalsHandle = Handles.INFUSION_PEDESTALS;
        MethodHandle componentsHandle = Handles.RECIPE_COMPONENTS;
        if (level == null || infusion == null || consumedHandle == null || recipeHandle == null
                || pedestalsHandle == null || componentsHandle == null) {
            return 0;
        }

        Object recipe = recipeHandle.get(infusion);
        if (recipe == null) return 0;
        if (!(pedestalsHandle.get(infusion) instanceof List<?> pedestals) || pedestals.isEmpty()) return 0;

        List<?> components;
        try {
            components = (List<?>) componentsHandle.invoke(recipe);
        } catch (Throwable error) {
            GoldenTweaks.LOGGER.error("InfusionIntercepter: cannot read ThaumicHorizons infusion components", error);
            return 0;
        }
        if (components == null || components.isEmpty()) return 0;

        int mask = (int) consumedHandle.get(infusion);
        int original = mask;

        for (int index = 0; index < components.size(); index++) {
            if ((mask & (1 << index)) != 0) continue;
            if (!(components.get(index) instanceof Ingredient ingredient)) continue;

            for (Object candidate : pedestals) {
                if (!(candidate instanceof BlockPos pos)) continue;
                if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) continue;

                ItemStack held = pedestal.getItem(0);
                if (held.isEmpty() || !ingredient.test(held)) continue;

                ItemStack remainder = held.copyWithCount(1).getCraftingRemainingItem();
                if (pedestal.removeItem(0, 1).isEmpty()) continue;

                mask |= 1 << index;
                if (!remainder.isEmpty()) {
                    if (pedestal.isEmpty()) {
                        pedestal.setFromInfusion(remainder);
                    } else {
                        Block.popResource(level, pos.above(), remainder);
                    }
                }
                break;
            }
        }

        if (mask == original) return 0;
        consumedHandle.set(infusion, mask);
        return Integer.bitCount(mask & ~original);
    }

    @Nullable
    private static Object infusion(@Nullable BlockEntity matrix) {
        VatBlockEntity vat = vat(matrix);
        VarHandle handle = Handles.VAT_INFUSION;
        return vat == null || handle == null ? null : handle.get(vat);
    }

    /* ------------------------------------------------------ */
    /* Reflection handles                                      */
    /* ------------------------------------------------------ */

    private static final class Handles {

        /** {@code VatBlockEntity.infusion} */
        static final VarHandle VAT_INFUSION;
        /** {@code VatInfusion.instability} */
        static final VarHandle INFUSION_INSTABILITY;
        /** {@code VatInfusion.recipe} */
        static final VarHandle INFUSION_RECIPE;
        /** {@code VatInfusion.pedestals} */
        static final VarHandle INFUSION_PEDESTALS;
        /** {@code VatInfusion.consumed} */
        static final VarHandle INFUSION_CONSUMED;
        /** {@code VatInfusionRecipe.components()} */
        static final MethodHandle RECIPE_COMPONENTS;

        static {
            VarHandle vatInfusion = null;
            VarHandle instability = null;
            VarHandle recipe = null;
            VarHandle pedestals = null;
            VarHandle consumed = null;
            MethodHandle components = null;

            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Class<?> infusionClass = VatBlockEntity.class.getDeclaredField("infusion").getType();
                vatInfusion = MethodHandles.privateLookupIn(VatBlockEntity.class, lookup)
                        .findVarHandle(VatBlockEntity.class, "infusion", infusionClass);

                MethodHandles.Lookup infusionLookup = MethodHandles.privateLookupIn(infusionClass, lookup);
                instability = infusionLookup.findVarHandle(infusionClass, "instability", int.class);
                consumed = infusionLookup.findVarHandle(infusionClass, "consumed", int.class);
                pedestals = infusionLookup.findVarHandle(infusionClass, "pedestals", List.class);

                Class<?> recipeClass = infusionClass.getDeclaredField("recipe").getType();
                recipe = infusionLookup.findVarHandle(infusionClass, "recipe", recipeClass);
                components = infusionLookup.findVirtual(recipeClass, "components", MethodType.methodType(List.class));
            } catch (Throwable error) {
                GoldenTweaks.LOGGER.error("VarHandle: ThaumicHorizons vat infusion", error);
            }

            VAT_INFUSION = vatInfusion;
            INFUSION_INSTABILITY = instability;
            INFUSION_RECIPE = recipe;
            INFUSION_PEDESTALS = pedestals;
            INFUSION_CONSUMED = consumed;
            RECIPE_COMPONENTS = components;
        }

        private Handles() {
        }
    }
}

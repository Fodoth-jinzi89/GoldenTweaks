package net.fodoth.skina.goldentweaks.compat.avaritia_more_items;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.emi.MekanismEmiHelper;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.ae2.GTAe2EmiStacks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Turns {@link GTReactionStack} entries into the real stacks their owning mods understand.
 *
 * <p>Every method here is the only place the optional mods are named. {@link GTReactionStack}
 * itself stays free of them so the recipe keeps loading with AMI alone, and the callers that need a
 * conversion already run on a client (recipe pages) or on a server that has the mod (the machine),
 * where the classes resolve.</p>
 */
public final class GTReactionStacks {

    private static final String MEKANISM = "mekanism";
    private static final String AE2 = "ae2";

    private GTReactionStacks() {
    }

    public static boolean hasMekanism() {
        return ModList.get().isLoaded(MEKANISM);
    }

    public static boolean hasAe2() {
        return ModList.get().isLoaded(AE2);
    }

    /** Resolves a Mekanism chemical stack, or {@code null} when it cannot be represented. */
    @Nullable
    public static ChemicalStack toChemicalStack(GTReactionStack stack) {
        if (stack.kind() != GTReactionStack.Kind.CHEMICAL || !hasMekanism()) {
            return null;
        }

        Holder<Chemical> chemical = MekanismAPI.CHEMICAL_REGISTRY.getHolder(stack.id()).orElse(null);
        if (chemical == null) {
            // Chemical ids are not a stable compile-time surface, so resolve by key and warn rather
            // than throwing: a data pack naming a chemical that the pack does not ship must not
            // take down the whole recipe page.
            GoldenTweaks.LOGGER.warn("[GT] chemical reaction names unknown chemical {}", stack.id());
            return null;
        }

        return new ChemicalStack(chemical, stack.amount());
    }

    /** Resolves an AE key, or {@code null} when it cannot be represented. */
    @Nullable
    public static GenericStack toGenericStack(GTReactionStack stack, HolderLookup.Provider registries) {
        if (stack.kind() != GTReactionStack.Kind.AE || !hasAe2()) {
            return null;
        }

        CompoundTag tag = stack.tag().orElse(null);
        if (tag == null) {
            return null;
        }

        AEKey key = AEKey.fromTagGeneric(registries, tag);
        return key == null ? null : new GenericStack(key, stack.amount());
    }

    /**
     * Converts any entry into an EMI-renderable stack.
     *
     * <p>All four kinds end up as {@code EmiStack} because Mekanism's chemical stack and AE2's
     * generic stacks each already have such a subclass — {@code ChemicalEmiStack} and whatever
     * {@code EmiStackConverters} yields — so the recipe page can draw every slot the same way.</p>
     */
    @Nullable
    public static dev.emi.emi.api.stack.EmiIngredient toEmiIngredient(
            GTReactionStack stack, @Nullable HolderLookup.Provider registries) {
        return switch (stack.kind()) {
            case ITEM -> {
                var item = stack.toItemStack();
                yield item.isEmpty() ? null : dev.emi.emi.api.stack.EmiStack.of(item);
            }
            case FLUID -> {
                FluidStack fluid = stack.toFluidStack();
                yield fluid.isEmpty() ? null : dev.emi.emi.api.neoforge.NeoForgeEmiStack.of(fluid);
            }
            case CHEMICAL -> {
                ChemicalStack chemical = toChemicalStack(stack);
                yield chemical == null || chemical.isEmpty()
                        ? null
                        : MekanismEmiHelper.INSTANCE.createEmiStack(chemical.getChemicalHolder(), chemical.getAmount());
            }
            case AE -> {
                GenericStack generic = registries == null ? null : toGenericStack(stack, registries);
                yield generic == null ? null : GTAe2EmiStacks.toEmiStack(generic.what());
            }
        };
    }
}

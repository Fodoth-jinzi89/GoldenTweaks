package net.fodoth.skina.goldentweaks.compat.mioicif.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;

import java.util.List;

/**
 * EMI page for Singularity Iteration's replicator: UU matter in, one item out.
 *
 * <p>SI ships an EMI plugin of its own, but it only covers the industrial workbench — the replicator
 * has no page anywhere, because it has no recipe list to draw. It prices whatever item it is handed
 * by asking the quote book at run time, so this page is instead generated from the reference price
 * table in SI's own data pack.</p>
 *
 * <p>That table is read client-side on purpose: every quote accessor takes a {@code MinecraftServer}
 * and SI never syncs prices, so no server-side figure is available to a client page even in single
 * player. The reference table is the same source the server seeds its quotes from, so the numbers
 * agree for everything the pack actually prices.</p>
 */
@EmiEntrypoint
public class GTUuReplicationEmiPlugin implements EmiPlugin {

    private static final String SI_MODID = "mio_icif";
    /** The replicator block, which is also the page's catalyst. */
    private static final ResourceLocation REPLICATOR_ID =
            ResourceLocation.fromNamespaceAndPath(SI_MODID, "producer/block_replicator_elc");
    private static final ResourceLocation FLUID_ID =
            ResourceLocation.fromNamespaceAndPath(SI_MODID, "uumatter");

    /**
     * Built lazily: a category holds a stack, and the replicator item does not exist until the item
     * registry is frozen. A static field would capture air at class-load time in some load orders.
     */
    private static EmiRecipeCategory category;

    static EmiRecipeCategory category() {
        if (category == null) {
            category = new EmiRecipeCategory(
                    ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "uu_replication"),
                    EmiStack.of(replicatorStack()));
        }
        return category;
    }

    /** The replicator block as an item stack, or an empty stack when it is not registered. */
    static ItemStack replicatorStack() {
        Item item = BuiltInRegistries.ITEM.get(REPLICATOR_ID);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public void register(EmiRegistry registry) {
        // Guard before touching any SI type: naming one in this class would fail verification
        // outright in a pack that does not ship the mod.
        if (!ModList.get().isLoaded(SI_MODID)) {
            return;
        }

        Fluid uu = BuiltInRegistries.FLUID.get(FLUID_ID);
        if (uu == null) {
            GoldenTweaks.LOGGER.warn("[GT] UU matter fluid is missing; skipping its EMI page.");
            return;
        }

        GTUuReferencePrices.load();
        List<GTUuReferencePrices.Row> rows = GTUuReferencePrices.rows();
        if (rows.isEmpty()) {
            // Nothing to show: an empty category is hidden by EMI anyway, and a warning has already
            // been logged by the reader.
            return;
        }

        EmiRecipeCategory page = category();
        registry.addCategory(page);
        registry.addWorkstation(page, EmiStack.of(replicatorStack()));

        int capacity = GTUuMatter.tankCapacity();
        for (int index = 0; index < rows.size(); index++) {
            GTUuReferencePrices.Row row = rows.get(index);
            registry.addRecipe(new GTUuReplicationEmiRecipe(
                    ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "uu_replication/" + index),
                    uu, row.stack(), row.millibuckets(), capacity));
        }
        GoldenTweaks.LOGGER.info("[GT] UU replication EMI page: {} entries.", rows.size());
    }
}

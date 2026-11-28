package net.fodoth.skina.goldentweaks.content.multiblock.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches a reaction's inputs against what the ports actually hold.
 *
 * <p>Written rather than delegated to AMI's {@code MultiblockRecipeMatcher} for one reason: that
 * matcher compares with {@code ItemStack.isSameItemSameComponents}, so an ingredient either names a
 * component-exact stack or nothing. A coolant cell loses value by gaining a stored-heat component, and
 * "any wear" is exactly the case an exact comparison cannot express.</p>
 *
 * <p>Vanilla's shaped and shapeless matchers have the same restriction, so this follows their shape
 * instead: each entry names a count and a rule, and the pool is drained as the rules are satisfied.
 * What it adds over vanilla is the two rules an item entry may carry — exact components (the default)
 * and id-only ({@code any_components}).</p>
 *
 * <p>Deliberately free of AMI types so it can be reasoned about on its own, and so the machine can
 * use it for matching, for the fit check and for consumption without three different code paths: a
 * craft that passes {@link #plan} is exactly a craft can take.</p>
 */
public final class GTReactionMatcher {

    private GTReactionMatcher() {
    }

    /**
     * How many of each pool entry a satisfied match consumes, or {@code null} when it cannot be met.
     *
     * <p>Exact entries are satisfied first, then the loose ones. Order matters: a loose entry accepts
     * any stack of its item, so letting it run first could take the very stack an exact entry names
     * and leave that entry short — a recipe would then be refused although every ingredient was on
     * hand. Tightest rule first is what keeps the two from competing.</p>
     *
     * <p>Returned rather than applied so the caller can check without mutating anything: the fit check
     * runs this against a copy of the ports, and consumption runs it against the real handlers.</p>
     */
    public static int[] plan(List<GTReactionStack> inputs, long multiplier, List<ItemStack> pool) {
        int[] used = new int[pool.size()];

        for (GTReactionStack entry : inputs) {
            if (entry.kind() != GTReactionStack.Kind.ITEM || entry.anyComponents()) {
                continue;
            }
            if (!take(entry, multiplier, pool, used)) {
                return null;
            }
        }
        for (GTReactionStack entry : inputs) {
            if (entry.kind() != GTReactionStack.Kind.ITEM || !entry.anyComponents()) {
                continue;
            }
            if (!take(entry, multiplier, pool, used)) {
                return null;
            }
        }
        return used;
    }

    /**
     * The most extra copies the ports could possibly support, judged by the inputs alone.
     *
     * <p>Found by division rather than by trying multipliers one at a time: every entry's demand
     * grows linearly with the copy count, so its stock divides straight into a ceiling and the
     * tightest entry wins. This is an upper bound, not an answer — entries may compete for the same
     * stack, and output space is not considered here — which is exactly what a search range needs.</p>
     *
     * <p>{@code baseCopies} is what the recipe is already multiplied by (parallel), so the result is
     * in the same units as the batch multiplier the caller is looking for. A recipe with nothing to
     * divide by — no matching entry — reports {@link Long#MAX_VALUE}.</p>
     */
    public static long capacity(List<GTReactionStack> inputs, long baseCopies,
                                List<ItemStack> itemPool, List<FluidStack> fluidPool) {
        long copies = Math.max(1L, baseCopies);
        long ceiling = Long.MAX_VALUE;
        for (GTReactionStack entry : inputs) {
            long perCopy = entry.amount() * copies;
            if (perCopy <= 0) {
                continue;
            }
            long stock = switch (entry.kind()) {
                case ITEM -> itemStock(entry, itemPool);
                case FLUID -> fluidStock(entry, fluidPool);
                default -> -1L;
            };
            if (stock < 0) {
                continue;
            }
            ceiling = Math.min(ceiling, stock / perCopy);
        }
        return ceiling;
    }

    /** Total count of the stacks this entry accepts, or {@code -1} when its item is unknown. */
    private static long itemStock(GTReactionStack entry, List<ItemStack> pool) {
        Item item = entry.item();
        if (item == null) {
            return -1L;
        }
        long stock = 0;
        for (ItemStack available : pool) {
            if (!available.is(item)) {
                continue;
            }
            if (!entry.anyComponents() && !matchesExactly(entry, available)) {
                continue;
            }
            stock += available.getCount();
        }
        return stock;
    }

    /** Total amount of this entry's fluid, or {@code -1} when its fluid is unknown. */
    private static long fluidStock(GTReactionStack entry, List<FluidStack> pool) {
        Fluid fluid = entry.fluid();
        if (fluid == null) {
            return -1L;
        }
        long stock = 0;
        for (FluidStack available : pool) {
            if (available.getFluid().equals(fluid)) {
                stock += available.getAmount();
            }
        }
        return stock;
    }

    /** Drains one item entry's demand from the pool. */
    private static boolean take(GTReactionStack entry, long multiplier,
                                List<ItemStack> pool, int[] used) {
        Item item = entry.item();
        if (item == null) {
            return false;
        }
        long needed = entry.amount() * multiplier;
        if (needed <= 0) {
            return true;
        }

        for (int index = 0; index < pool.size() && needed > 0; index++) {
            ItemStack available = pool.get(index);
            if (!available.is(item)) {
                continue;
            }
            // plan() already separated the two rules, so this entry's rule is simply its own.
            if (!entry.anyComponents() && !matchesExactly(entry, available)) {
                continue;
            }
            long free = available.getCount() - used[index];
            if (free <= 0) {
                continue;
            }
            long taken = Math.min(needed, free);
            used[index] += (int) taken;
            needed -= taken;
        }

        return needed <= 0;
    }

    /**
     * Whether the pool stack is the exact one the entry names.
     *
     * <p>An entry with no component rule names a plain stack, so a used coolant cell does not satisfy
     * it — that is the whole point of the loose rule existing.</p>
     */
    private static boolean matchesExactly(GTReactionStack entry, ItemStack candidate) {
        ItemStack template = entry.toItemStack();
        return !template.isEmpty()
                && ItemStack.isSameItemSameComponents(template, candidate);
    }

    /**
     * Whether the pool holds enough of every fluid the reaction asks for.
     *
     * <p>Fluids have no component rule, so this is a plain amount check per fluid id.</p>
     */
    public static boolean matchesFluids(List<GTReactionStack> inputs, long multiplier,
                                        List<FluidStack> pool) {
        for (GTReactionStack entry : inputs) {
            if (entry.kind() != GTReactionStack.Kind.FLUID) {
                continue;
            }
            Fluid fluid = entry.fluid();
            if (fluid == null) {
                return false;
            }
            long needed = entry.amount() * multiplier;
            for (FluidStack available : pool) {
                if (needed <= 0) {
                    break;
                }
                if (!available.getFluid().equals(fluid)) {
                    continue;
                }
                needed -= available.getAmount();
            }
            if (needed > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * The item outputs a recipe produces, with the loose entries' templates resolved.
     *
     * <p>An output entry may name the same item as a loose input — a repair turns a used cell into a
     * fresh one — and its template is already the clean stack, so nothing special is needed here.
     * Kept as a method so the machine has one place that turns entries into stacks.</p>
     */
    public static List<ItemStack> itemOutputs(List<GTReactionStack> outputs) {
        List<ItemStack> stacks = new ArrayList<>();
        for (GTReactionStack entry : outputs) {
            if (entry.kind() != GTReactionStack.Kind.ITEM) {
                continue;
            }
            ItemStack stack = entry.toItemStack();
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}

package net.fodoth.skina.goldentweaks.content.multiblock.port;

import net.neoforged.fml.ModList;
import net.fodoth.skina.goldentweaks.content.multiblock.common.GTReactionStack;

/**
 * The amount badge shown beside a port's contents.
 *
 * <p>Delegates to AE2's formatter whenever AE2 is installed, because that is the one its terminals
 * use for every stored entry and the one the mod's own recipe pages already use: amounts that fit in
 * four digits print in full, longer ones are abbreviated by thousands. Sharing the formatter keeps
 * the same quantity from appearing in two shapes a click apart.</p>
 *
 * <p>The AE2 lookup sits behind a mod check and a nested class. AE2 is a compile-time dependency
 * here, but not a required one at runtime, and naming its class from a method that always loads
 * would fail with {@code NoClassDefFoundError} on a pack without it.</p>
 */
public final class GTPortAmounts {

    private GTPortAmounts() {
    }

    /** The label for one row: a count, or an amount in mB for fluids. */
    static String format(GTReactionStack stack) {
        boolean item = stack.kind() == GTReactionStack.Kind.ITEM;
        String formatted = number(stack.amount(), item);
        return item ? formatted : formatted + " mB";
    }

    /**
     * The bare number, for callers that supply their own unit or none at all.
     *
     * <p>The recipe page's slot badge is the caller in question: it shows the same figure as AE's
     * terminals and appends no suffix, since the fluid's own tooltip already names the unit.</p>
     */
    public static String number(long amount, boolean item) {
        return ModList.get().isLoaded("ae2")
                ? Ae2Formatter.format(amount, item)
                : Long.toString(amount);
    }

    /** Isolated so the AE2 classes are only resolved when the mod is actually present. */
    private static final class Ae2Formatter {

        private Ae2Formatter() {
        }

        static String format(long amount, boolean item) {
            var type = item ? appeng.api.stacks.AEKeyType.items()
                    : appeng.api.stacks.AEKeyType.fluids();
            return type.formatAmount(amount, appeng.api.stacks.AmountFormat.SLOT);
        }
    }
}

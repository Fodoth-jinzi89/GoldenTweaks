package net.fodoth.skina.goldentweaks.content.multiblock.client;

import net.fodoth.skina.goldentweaks.util.GTNumberInput;
import net.minecraft.client.gui.components.EditBox;

import java.util.function.Predicate;

/**
 * One text field, and the value it has sent but not yet seen come back.
 *
 * <p>The port entity and the machine both report a field only after the packet lands, so between
 * the edit and the echo the screen still reads the old value. Writing that straight back into the
 * box is what made an edit appear to bounce for a frame or two. Holding the sent text here lets the
 * echo recognise its own stale reading and leave the box alone until the server catches up 鈥?and
 * once it reports a different value, that is the server's normalised answer and it wins.</p>
 *
 * <p>The held value is compared in the form the box displays it, not the form that goes on the
 * wire: a priority box shows {@code 1,600}, so comparing against {@code 1600} would never match and
 * the field would stay pinned to its pending value for good.</p>
 *
 * <p>A numeric field also has a ceiling that can move under it 鈥?pulling an energy hatch lowers the
 * overclock limit, and a stored value above the new top is one the machine would silently reduce.
 * The ceiling is applied on the way in, so the box never offers a number the machine would refuse,
 * and a value that reads back above it is shown as the top rather than as itself.</p>
 */
public final class GTEchoField {

    /**
     * How a field's text relates to what it stores.
     *
     * <p>{@link #TEXT} passes the value through untouched, which is what the port's mode and
     * channel lists need: they only happen to contain digits among separators, so grouping them
     * would be wrong.</p>
     */
    public enum Kind {
        TEXT,
        /** A single integer, shown with thousands separators. */
        NUMBER,
        /** A single integer with a ceiling that may be zero, meaning "no ceiling". */
        NUMBER_CAPPED
    }

    private final EditBox box;
    private final Kind kind;
    private String pending;

    public GTEchoField(EditBox box) {
        this(box, Kind.TEXT);
    }

    public GTEchoField(EditBox box, Kind kind) {
        this.box = box;
        this.kind = kind;
    }

    public EditBox box() {
        return this.box;
    }

    /** Records what was just sent, in the form the box will show it. */
    public void sent(String shownText) {
        this.pending = shownText;
    }

    /** Records what was just sent, from the value that went on the wire. */
    public void sentValue(int value) {
        this.pending = displayText(value);
    }

    /**
     * Adopts the stored text, unless one is still in flight.
     *
     * <p>While a send is outstanding the box keeps the text the player typed. A stored value that
     * matches the pending one means the round trip landed, and ordinary echoing resumes; a different
     * one means the server normalised it, which is applied as the correction.</p>
     */
    public void echo(String stored) {
        echo(stored, stored::equals);
    }

    /**
     * Adopts the stored text, with the caller deciding whether the send has landed.
     *
     * <p>A cleared name is the reason this exists: it does not read back as the empty string but as
     * the block's default wording, so string equality cannot tell the round trip from a stale
     * reading. The test receives what is in flight and answers whether the entity has caught up.</p>
     */
    public void echo(String stored, Predicate<String> landed) {
        if (this.box.isFocused()) {
            return;
        }
        if (this.pending != null) {
            if (!landed.test(this.pending)) {
                return;
            }
            this.pending = null;
        }
        if (!stored.equals(this.box.getValue())) {
            this.box.setValue(stored);
        }
    }

    /**
     * Adopts a stored number, clamped to the ceiling on the way in.
     *
     * @param ceiling the highest value the box may show, or {@code 0} for no ceiling
     */
    public void echoNumber(int stored, int ceiling) {
        int shown = clamp(stored, ceiling);
        if (this.box.isFocused()) {
            return;
        }
        String text = displayText(shown);
        if (this.pending != null) {
            if (!text.equals(this.pending)) {
                return;
            }
            this.pending = null;
        }
        if (!text.equals(this.box.getValue())) {
            this.box.setValue(text);
        }
    }

    /**
     * The number the box currently means.
     *
     * @param fallback what an unparseable entry becomes, which is also the floor: anything below it
     *                 is raised to it, so the box always shows what will actually be stored
     */
    public int value(int fallback, int ceiling) {
        Integer parsed = GTNumberInput.parseInt(this.box.getValue());
        int typed = parsed == null ? fallback : parsed;
        if (typed < fallback) {
            typed = fallback;
        }
        return clamp(typed, ceiling);
    }

    /** Whether a send is still outstanding. */
    public boolean inFlight() {
        return this.pending != null;
    }

    /**
     * Whether a value is already what was last sent.
     *
     * <p>Compared against the in-flight value as well as nothing else, so a re-typed correction
     * is not mistaken for "unchanged" and silently dropped.</p>
     */
    public boolean matches(String value) {
        return this.pending != null && this.pending.equals(value);
    }

    /** The text form of a stored value, which is what both {@code sent} and {@code echo} use. */
    public String displayText(int value) {
        return this.kind == Kind.TEXT ? String.valueOf(value) : GTNumberInput.group(value);
    }

    /** A ceiling of zero means "no ceiling", not "nothing". */
    private static int clamp(int value, int ceiling) {
        return ceiling > 0 && value > ceiling ? ceiling : value;
    }

    /** Whether this field holds one integer rather than a free-form list. */
    public boolean numeric() {
        return this.kind != Kind.TEXT;
    }
}

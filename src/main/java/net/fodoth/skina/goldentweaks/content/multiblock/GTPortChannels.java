package net.fodoth.skina.goldentweaks.content.multiblock;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a port's channel field.
 *
 * <p>The field holds a comma-separated list of integers, e.g. {@code 0}, {@code 1}, {@code 0,1} or
 * {@code -3,4,5}. Anything that does not parse to a non-empty list collapses to {@code "0"}, and
 * {@code 0} is a wildcard: a field containing it keeps only that value, because "every channel"
 * and "channels 0 and 3" mean the same thing and the second is the more confusing way to say it.</p>
 *
 * <p>Parsing is a pure function of the text so the block entity, the machine and the window can all
 * agree on what a field means without storing a second copy of it.</p>
 */
public final class GTPortChannels {

    /** The wildcard channel: a port carrying it joins every group. */
    public static final int WILDCARD = 0;

    /** The canonical text for a wildcard port, and the fallback for anything unparseable. */
    public static final String WILDCARD_TEXT = "0";

    private GTPortChannels() {
    }

    /**
     * Normalises a field's text.
     *
     * <p>Returns the text that should actually be stored: the original when it parses, and
     * {@link #WILDCARD_TEXT} when it does not. A field that names the wildcard collapses to just
     * that, since listing other channels beside it changes nothing.</p>
     */
    public static String normalise(String raw) {
        List<Integer> channels = parse(raw);
        if (channels.isEmpty()) {
            return WILDCARD_TEXT;
        }
        if (channels.contains(WILDCARD)) {
            return WILDCARD_TEXT;
        }
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < channels.size(); index++) {
            if (index > 0) {
                out.append(',');
            }
            out.append(channels.get(index));
        }
        return out.toString();
    }

    /**
     * The channels a field's text names, de-duplicated and in the order written.
     *
     * <p>An empty result means the text is unparseable — callers treat that as the wildcard rather
     * than as "no channels", so a broken field can never silently exclude a port from everything.</p>
     */
    public static List<Integer> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<Integer> out = new ArrayList<>();
        for (String part : raw.split(",", -1)) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                // An empty segment is a malformed list, not a missing entry: "1,,2" is a typo and
                // treating it as "1,2" would hide that from the player.
                return List.of();
            }
            int value;
            try {
                value = Integer.parseInt(trimmed);
            } catch (NumberFormatException rejected) {
                return List.of();
            }
            if (!out.contains(value)) {
                out.add(value);
            }
        }
        return out;
    }

    /** True when the text means "every channel". */
    public static boolean isWildcard(String raw) {
        return parse(raw).contains(WILDCARD);
    }

    /**
     * The channels a port actually answers to.
     *
     * <p>A wildcard port answers every group; otherwise the parsed list is used, and unparseable
     * text again means wildcard.</p>
     */
    public static List<Integer> effective(String raw) {
        List<Integer> parsed = parse(raw);
        if (parsed.isEmpty()) {
            return List.of(WILDCARD);
        }
        return parsed;
    }

    /** Whether a port on these channels takes part in the group for {@code channel}. */
    public static boolean joins(@Nullable String raw, int channel) {
        List<Integer> parsed = effective(raw);
        return parsed.contains(WILDCARD) || parsed.contains(channel);
    }
}

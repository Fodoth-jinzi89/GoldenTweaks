"""Teach GTReactionStack about essentia.

Essentia has no registry entry and no stack class of its own — Thaumcraft names it by a short
tag like "aer" and counts it with an int. So the entry stores the tag in its id field, which
ResourceLocation can carry as a namespace-less path, and the renderer resolves it back through
Aspect.getAspect.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTReactionStack.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    public enum Kind {
        ITEM,
        FLUID,
        CHEMICAL,
        AE
    }''',
'''    public enum Kind {
        ITEM,
        FLUID,
        CHEMICAL,
        /**
         * Thaumcraft essentia.
         *
         * <p>Appended rather than inserted: the ordinal is what the wire codec writes, so
         * reordering would make a client on the old build misread every entry.</p>
         */
        ESSENTIA,
        AE
    }''', 1)

s = s.replace('''    private static final String FIELD_CHEMICAL = "chemical";''',
'''    private static final String FIELD_CHEMICAL = "chemical";
    private static final String FIELD_ESSENTIA = "essentia";''', 1)

s = s.replace('''            return Stream.of(FIELD_ITEM, FIELD_FLUID, FIELD_CHEMICAL, FIELD_AE).map(ops::createString);''',
'''            return Stream.of(FIELD_ITEM, FIELD_FLUID, FIELD_CHEMICAL, FIELD_ESSENTIA, FIELD_AE)
                    .map(ops::createString);''', 1)

s = s.replace('''            if (input.get(FIELD_AE) != null) {
                return decodeAe(ops, input.get(FIELD_AE));
            }
            return DataResult.error(() -> "reaction stack needs one of "
                    + FIELD_ITEM + "/" + FIELD_FLUID + "/" + FIELD_CHEMICAL + "/" + FIELD_AE);''',
'''            if (input.get(FIELD_ESSENTIA) != null) {
                return decodeSimple(ops, input.get(FIELD_ESSENTIA), input, Kind.ESSENTIA);
            }
            if (input.get(FIELD_AE) != null) {
                return decodeAe(ops, input.get(FIELD_AE));
            }
            return DataResult.error(() -> "reaction stack needs one of " + FIELD_ITEM + "/"
                    + FIELD_FLUID + "/" + FIELD_CHEMICAL + "/" + FIELD_ESSENTIA + "/" + FIELD_AE);''', 1)

s = s.replace('''                case CHEMICAL -> {
                    prefix.add(FIELD_CHEMICAL, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }''',
'''                case CHEMICAL -> {
                    prefix.add(FIELD_CHEMICAL, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }
                case ESSENTIA -> {
                    prefix.add(FIELD_ESSENTIA, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }''', 1)

s = s.replace('''    /** The fluid this stack names, or {@code null} when it is another kind or the id is unknown. */''',
'''    /**
     * The essentia tag this stack names, e.g. {@code aer}, or {@code null} for the other kinds.
     *
     * <p>Stored in the id field as a path with no namespace: essentia is not a registry entry, and
     * a bare tag is the only name Thaumcraft has for it.</p>
     */
    public String essentiaTag() {
        return this.kind == Kind.ESSENTIA ? this.id.getPath() : null;
    }

    /** Builds an essentia entry from a Thaumcraft aspect tag. */
    public static GTReactionStack essentia(String tag, int amount) {
        return new GTReactionStack(Kind.ESSENTIA,
                ResourceLocation.fromNamespaceAndPath("thaumcraft", tag),
                amount, Optional.empty());
    }

    /** The fluid this stack names, or {@code null} when it is another kind or the id is unknown. */''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("GTReactionStack knows essentia")

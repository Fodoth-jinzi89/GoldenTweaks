"""A recipe reports the type it was registered under.

`getType()` named the basic type unconditionally. The advanced kettle's recipes reuse this class
(and its serializer), so every advanced recipe claimed to be a basic one — which puts it in the
basic type's list, in the basic EMI page, and makes the advanced machine's own type appear empty.

The type now travels with the recipe. The datapack codec sets it from the file's `type` field,
which the serializer already knows; the stream codec carries it across the wire.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipe.java"
s = io.open(p, encoding="utf-8").read()

# ---- the field -------------------------------------------------------------------------------
old_fields = '''    /** The mode this reaction belongs to; {@link #ANY_MODE} matches every port mode. */
    private final int mode;'''
new_fields = '''    /** The mode this reaction belongs to; {@link #ANY_MODE} matches every port mode. */
    private final int mode;

    /**
     * The registered recipe type this instance belongs to.
     *
     * <p>Carried rather than assumed: the advanced kettle's recipes use this same class and
     * serializer, so reporting a fixed type would file every advanced recipe under the basic type
     * — visible as an empty advanced page and a basic page listing recipes its machine cannot run.
     * The serializer sets it from the file's own {@code type} field.</p>
     */
    private final RecipeType<?> type;'''
assert old_fields in s
s = s.replace(old_fields, new_fields, 1)

# ---- constructors ----------------------------------------------------------------------------
old_ctor4 = '''    @SuppressWarnings("unused")
    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick) {
        this(inputs, outputs, processingTime, energyPerTick, ANY_MODE);
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
        this.mode = mode;
    }'''
new_ctor4 = '''    @SuppressWarnings("unused")
    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick) {
        this(inputs, outputs, processingTime, energyPerTick, ANY_MODE,
                GTChemicalReactionRecipes.TYPE.get());
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode) {
        this(inputs, outputs, processingTime, energyPerTick, mode,
                GTChemicalReactionRecipes.TYPE.get());
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode,
                                    RecipeType<?> type) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
        this.mode = mode;
        this.type = type;
    }'''
assert old_ctor4 in s
s = s.replace(old_ctor4, new_ctor4, 1)

# ---- getType ---------------------------------------------------------------------------------
old_get_type = '''    @Override
    public @NotNull RecipeType<?> getType() {
        return GTChemicalReactionRecipes.TYPE.get();
    }'''
new_get_type = '''    @Override
    public @NotNull RecipeType<?> getType() {
        return this.type;
    }'''
assert old_get_type in s
s = s.replace(old_get_type, new_get_type, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("recipe carries its own type")

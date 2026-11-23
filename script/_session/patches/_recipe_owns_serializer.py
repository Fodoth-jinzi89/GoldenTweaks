"""A recipe reports the serializer it was actually built by.

`getSerializer()` named the basic serializer unconditionally. Deserialisation is driven by the
serializer, so every advanced recipe was stamped with the basic type by the basic serializer — the
advanced registry then held nothing, and all four recipes landed in the basic category.

The serializer now travels with the recipe, exactly as the type does, so the two always agree about
which registry entry produced the instance.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipe.java"
s = io.open(p, encoding="utf-8").read()

# ---- the field ---------------------------------------------------------------------------------
old_fields = '''    /**
     * The recipe type this instance belongs to.
     *
     * <p>Carried rather than assumed. The advanced kettle reuses this class and its serializer, so
     * a fixed type would file every advanced recipe under the basic type — an empty advanced page,
     * and a basic page listing recipes its machine cannot run.</p>
     */
    private final RecipeType<?> type;'''
new_fields = '''    /**
     * The recipe type this instance belongs to.
     *
     * <p>Carried rather than assumed. The advanced kettle reuses this class, so a fixed type would
     * file every advanced recipe under the basic type — an empty advanced page, and a basic page
     * listing recipes its machine cannot run.</p>
     */
    private final RecipeType<?> type;

    /**
     * The serializer that built this instance.
     *
     * <p>Carried for the same reason as {@link #type}: the two registries each own a serializer
     * instance, and {@code getSerializer()} is what the game writes back into a recipe holder. A
     * fixed answer would claim the basic serializer for an advanced recipe.</p>
     */
    private final RecipeSerializer<?> serializer;'''
assert old_fields in s
s = s.replace(old_fields, new_fields, 1)

# ---- constructors ------------------------------------------------------------------------------
old_ctors = '''    @SuppressWarnings("unused")
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
new_ctors = '''    @SuppressWarnings("unused")
    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick) {
        this(inputs, outputs, processingTime, energyPerTick, ANY_MODE,
                GTChemicalReactionRecipes.TYPE.get(), GTChemicalReactionRecipes.SERIALIZER.get());
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode) {
        this(inputs, outputs, processingTime, energyPerTick, mode,
                GTChemicalReactionRecipes.TYPE.get(), GTChemicalReactionRecipes.SERIALIZER.get());
    }

    public GTChemicalReactionRecipe(List<GTReactionStack> inputs,
                                    List<GTReactionStack> outputs,
                                    int processingTime,
                                    int energyPerTick,
                                    int mode,
                                    RecipeType<?> type,
                                    RecipeSerializer<?> serializer) {
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.processingTime = processingTime;
        this.energyPerTick = energyPerTick;
        this.mode = mode;
        this.type = type;
        this.serializer = serializer;
    }'''
assert old_ctors in s
s = s.replace(old_ctors, new_ctors, 1)

# ---- getSerializer -----------------------------------------------------------------------------
old_get = '''    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return GTChemicalReactionRecipes.SERIALIZER.get();
    }'''
new_get = '''    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }'''
assert old_get in s
s = s.replace(old_get, new_get, 1)

# ---- the serializer passes itself --------------------------------------------------------------
s = s.replace('''            ).apply(instance, (inputs, outputs, time, energy, mode) ->
                    new GTChemicalReactionRecipe(inputs, outputs, time, energy, mode, type)));''',
'''            ).apply(instance, (inputs, outputs, time, energy, mode) ->
                    new GTChemicalReactionRecipe(inputs, outputs, time, energy, mode, type, this)));''', 1)

s = s.replace('''                    return new GTChemicalReactionRecipe(inputs, outputs,
                            buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), type);''',
'''                    return new GTChemicalReactionRecipe(inputs, outputs,
                            buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                            type, Serializer.this);''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("recipe carries the serializer that built it")

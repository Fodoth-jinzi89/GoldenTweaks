"""Rewrite the recipe class's serializer so each instance stamps its own type.

A RecipeSerializer gets no context while parsing, so it cannot ask which type is using it.
Registering one instance per type is what the two registries already do; the serializer now takes
that type and applies it to every recipe it produces, from JSON and from the wire alike.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactionRecipe.java"
s = io.open(p, encoding="utf-8").read()

# ---- the class now carries its type ------------------------------------------------------------
old_fields = '''    /** The mode this reaction belongs to; {@link #ANY_MODE} matches every port mode. */
    private final int mode;'''
new_fields = '''    /** The mode this reaction belongs to; {@link #ANY_MODE} matches every port mode. */
    private final int mode;

    /**
     * The recipe type this instance belongs to.
     *
     * <p>Carried rather than assumed. The advanced kettle reuses this class and its serializer, so
     * a fixed type would file every advanced recipe under the basic type — an empty advanced page,
     * and a basic page listing recipes its machine cannot run.</p>
     */
    private final RecipeType<?> type;'''
assert old_fields in s
s = s.replace(old_fields, new_fields, 1)

old_ctors = '''    @SuppressWarnings("unused")
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
new_ctors = '''    @SuppressWarnings("unused")
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
assert old_ctors in s
s = s.replace(old_ctors, new_ctors, 1)

old_get = '''    @Override
    public @NotNull RecipeType<?> getType() {
        return GTChemicalReactionRecipes.TYPE.get();
    }'''
new_get = '''    @Override
    public @NotNull RecipeType<?> getType() {
        return this.type;
    }'''
assert old_get in s
s = s.replace(old_get, new_get, 1)

# ---- serializer carries the type ---------------------------------------------------------------
old_ser = '''    /** Datapack shape: {@code inputs}, {@code outputs}, {@code processing_time}, {@code energy_per_tick}, {@code mode}. */
    public static final class Serializer implements RecipeSerializer<GTChemicalReactionRecipe> {

        private static final MapCodec<GTChemicalReactionRecipe> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        GTReactionStack.CODEC.listOf()
                                .fieldOf("inputs").forGetter(GTChemicalReactionRecipe::inputs),
                        GTReactionStack.CODEC.listOf()
                                .optionalFieldOf("outputs", List.of())
                                .forGetter(GTChemicalReactionRecipe::outputs),
                        Codec.INT.optionalFieldOf("processing_time", 40)
                                .forGetter(GTChemicalReactionRecipe::getProcessingTime),
                        Codec.INT.optionalFieldOf("energy_per_tick", 0)
                                .forGetter(GTChemicalReactionRecipe::getEnergyPerTick),
                        Codec.INT.optionalFieldOf("mode", GTChemicalReactionRecipe.ANY_MODE)
                                .forGetter(GTChemicalReactionRecipe::mode)
                ).apply(instance, GTChemicalReactionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public @NotNull GTChemicalReactionRecipe decode(RegistryFriendlyByteBuf buffer) {
                        List<GTReactionStack> inputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                        List<GTReactionStack> outputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                        return new GTChemicalReactionRecipe(inputs, outputs,
                                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, GTChemicalReactionRecipe recipe) {
                        buffer.writeCollection(recipe.inputs(), GTReactionStack.STREAM_CODEC);
                        buffer.writeCollection(recipe.outputs(), GTReactionStack.STREAM_CODEC);
                        buffer.writeVarInt(recipe.getProcessingTime());
                        buffer.writeVarInt(recipe.getEnergyPerTick());
                        buffer.writeVarInt(recipe.mode());
                    }
                };

        @Override
        public @NotNull MapCodec<GTChemicalReactionRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }'''
new_ser = '''    /**
     * Datapack shape: {@code inputs}, {@code outputs}, {@code processing_time}, {@code energy_per_tick}, {@code mode}.
     *
     * <p>One instance per recipe type: parsing gets no context, so the type has to come from the
     * instance. Both registries construct their own with their own type.</p>
     */
    public static final class Serializer implements RecipeSerializer<GTChemicalReactionRecipe> {

        private final RecipeType<?> type;

        private final MapCodec<GTChemicalReactionRecipe> codec;

        private final StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> streamCodec;

        public Serializer(RecipeType<?> type) {
            this.type = type;

            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    GTReactionStack.CODEC.listOf()
                            .fieldOf("inputs").forGetter(GTChemicalReactionRecipe::inputs),
                    GTReactionStack.CODEC.listOf()
                            .optionalFieldOf("outputs", List.of())
                            .forGetter(GTChemicalReactionRecipe::outputs),
                    Codec.INT.optionalFieldOf("processing_time", 40)
                            .forGetter(GTChemicalReactionRecipe::getProcessingTime),
                    Codec.INT.optionalFieldOf("energy_per_tick", 0)
                            .forGetter(GTChemicalReactionRecipe::getEnergyPerTick),
                    Codec.INT.optionalFieldOf("mode", GTChemicalReactionRecipe.ANY_MODE)
                            .forGetter(GTChemicalReactionRecipe::mode)
            ).apply(instance, (inputs, outputs, time, energy, mode) ->
                    new GTChemicalReactionRecipe(inputs, outputs, time, energy, mode, type)));

            // The wire carries no type: a recipe only ever travels to a client that already has the
            // same type registered, and the instance that encoded it knows which one that is.
            this.streamCodec = new StreamCodec<>() {
                @Override
                public @NotNull GTChemicalReactionRecipe decode(RegistryFriendlyByteBuf buffer) {
                    List<GTReactionStack> inputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                    List<GTReactionStack> outputs = buffer.readList(GTReactionStack.STREAM_CODEC);
                    return new GTChemicalReactionRecipe(inputs, outputs,
                            buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), type);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GTChemicalReactionRecipe recipe) {
                    buffer.writeCollection(recipe.inputs(), GTReactionStack.STREAM_CODEC);
                    buffer.writeCollection(recipe.outputs(), GTReactionStack.STREAM_CODEC);
                    buffer.writeVarInt(recipe.getProcessingTime());
                    buffer.writeVarInt(recipe.getEnergyPerTick());
                    buffer.writeVarInt(recipe.mode());
                }
            };
        }

        @Override
        public @NotNull MapCodec<GTChemicalReactionRecipe> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, GTChemicalReactionRecipe> streamCodec() {
            return this.streamCodec;
        }
    }'''
assert old_ser in s
s = s.replace(old_ser, new_ser, 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("recipe carries its type; serializer carries its type")

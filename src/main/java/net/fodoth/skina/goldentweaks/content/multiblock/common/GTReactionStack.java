package net.fodoth.skina.goldentweaks.content.multiblock.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * One end of a reaction: a kind-independent stack that may be an item, a fluid, a Mekanism chemical
 * or any other AE key.
 *
 * <p>The recipe class lives in the {@code avaritia_more_items} compat package, so it has to stay
 * loadable with AMI alone. Naming {@code ChemicalStack} or {@code AEKey} here would make
 * "AMI but no Mekanism" fail at class load, so only registry ids and a raw NBT tag are stored;
 * {@link GTReactionStacks} turns them into real stacks on the client and in the machine, where the
 * optional mods are already known to be present.</p>
 *
 * <p>Each entry names its own kind with the field key, which keeps the data pack readable:</p>
 * <pre>
 * {"item": "minecraft:iron_ingot", "count": 4}
 * {"fluid": "minecraft:water", "amount": 1000}
 * {"chemical": "mekanism:oxygen", "amount": 500}
 * {"ae": {"key": {...}, "amount": 8}}
 * </pre>
 */
public record GTReactionStack(Kind kind, ResourceLocation id, long amount, Optional<CompoundTag> tag) {

    public enum Kind {
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
    }

    private static final String FIELD_ITEM = "item";
    private static final String FIELD_FLUID = "fluid";
    private static final String FIELD_CHEMICAL = "chemical";
    private static final String FIELD_ESSENTIA = "essentia";
    private static final String FIELD_AE = "ae";
    private static final String FIELD_COUNT = "count";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_KEY = "key";

    /**
     * Marks an item entry as matching by id alone, ignoring data components.
     *
     * <p>Carried inside the existing tag slot rather than as a record component, so the wire format
     * and every constructor stay as they are. The flag is read only for items.</p>
     *
     * <p>What it is for: a used reactor coolant cell differs from a fresh one only by its stored-heat
     * component, so an exact-component match could never accept "any wear". A recipe that repairs such
     * an item asks for it with {@code "any_components": true} and a clean output.</p>
     */
    private static final String FIELD_ANY_COMPONENTS = "any_components";
    private static final String TAG_ANY_COMPONENTS = "goldentweaks:any_components";

    /**
     * Carries an item's data components inside the entry's tag slot.
     *
     * <p>The same slot already holds {@link #TAG_ANY_COMPONENTS}, so the two are kept under separate
     * keys: a recipe never sets both, and a port's snapshot only ever sets this one.</p>
     */
    private static final String TAG_COMPONENTS = "goldentweaks:components";

    /** Placeholder id for AE entries: an AE key carries its own identity inside the NBT tag. */
    public static final ResourceLocation AE_PLACEHOLDER_ID =
            ResourceLocation.fromNamespaceAndPath("ae2", "generic");

    /**
     * Reads whichever kind field is present.
     *
     * <p>Written by hand rather than with {@code Codec.mapEither}: the recorded form has to encode
     * back to the same shape it was read from, and an either-chain always encodes through its first
     * branch.</p>
     */
    public static final MapCodec<GTReactionStack> MAP_CODEC = new MapCodec<>() {

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(FIELD_ITEM, FIELD_FLUID, FIELD_CHEMICAL, FIELD_ESSENTIA, FIELD_AE)
                    .map(ops::createString);
        }

        @Override
        public <T> DataResult<GTReactionStack> decode(DynamicOps<T> ops, MapLike<T> input) {
            if (input.get(FIELD_ITEM) != null) {
                return decodeSimple(ops, input.get(FIELD_ITEM), input, Kind.ITEM);
            }
            if (input.get(FIELD_FLUID) != null) {
                return decodeSimple(ops, input.get(FIELD_FLUID), input, Kind.FLUID);
            }
            if (input.get(FIELD_CHEMICAL) != null) {
                return decodeSimple(ops, input.get(FIELD_CHEMICAL), input, Kind.CHEMICAL);
            }
            if (input.get(FIELD_ESSENTIA) != null) {
                return decodeSimple(ops, input.get(FIELD_ESSENTIA), input, Kind.ESSENTIA);
            }
            if (input.get(FIELD_AE) != null) {
                return decodeAe(ops, input.get(FIELD_AE));
            }
            return DataResult.error(() -> "reaction stack needs one of " + FIELD_ITEM + "/"
                    + FIELD_FLUID + "/" + FIELD_CHEMICAL + "/" + FIELD_ESSENTIA + "/" + FIELD_AE);
        }

        private <T> DataResult<GTReactionStack> decodeSimple(
                DynamicOps<T> ops, T idElement, MapLike<T> input, Kind kind) {
            // Items count in whole items, so they read "count"; the bulk kinds read "amount".
            boolean counted = kind == Kind.ITEM;
            long fallback = counted ? 1L : 1000L;
            T sizeElement = input.get(counted ? FIELD_COUNT : FIELD_AMOUNT);

            DataResult<ResourceLocation> id = ResourceLocation.CODEC.parse(ops, idElement);
            DataResult<Long> size = Codec.LONG.parse(ops,
                    sizeElement == null ? ops.createLong(fallback) : sizeElement);

            // Only items carry components, so the flag is meaningless for the bulk kinds; ignoring it
            // there keeps a stray field from silently changing how a fluid is matched.
            boolean anyComponents = counted && isAnyComponents(ops, input);

            return id.apply2((value, count) -> new GTReactionStack(kind, value, count,
                    anyComponents ? Optional.of(anyComponentsTag()) : Optional.empty()), size);
        }

        /** Reads the {@code any_components} flag, tolerating its absence. */
        private static <T> boolean isAnyComponents(DynamicOps<T> ops, MapLike<T> input) {
            T element = input.get(FIELD_ANY_COMPONENTS);
            if (element == null) {
                return false;
            }
            return Codec.BOOL.parse(ops, element).result().orElse(false);
        }

        /** The marker stored in the tag slot for an any-components entry. */
        private static CompoundTag anyComponentsTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(TAG_ANY_COMPONENTS, true);
            return tag;
        }

        private <T> DataResult<GTReactionStack> decodeAe(DynamicOps<T> ops, T element) {
            DataResult<CompoundTag> object = CompoundTag.CODEC.parse(ops, element);
            return object.flatMap(tag1 -> {
                if (!tag1.contains(FIELD_KEY)) {
                    return DataResult.error(() -> FIELD_AE + " needs a " + FIELD_KEY + " tag");
                }

                long size = tag1.contains(FIELD_AMOUNT) ? tag1.getLong(FIELD_AMOUNT) : 1L;
                return DataResult.success(new GTReactionStack(
                        Kind.AE, AE_PLACEHOLDER_ID, Math.max(1L, size), Optional.of(tag1.getCompound(FIELD_KEY))));
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(GTReactionStack stack, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            switch (stack.kind()) {
                case ITEM -> {
                    prefix.add(FIELD_ITEM, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_COUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                    if (stack.anyComponents()) {
                        prefix.add(FIELD_ANY_COMPONENTS, Codec.BOOL.encodeStart(ops, true));
                    }
                    // A port's snapshot carries the real components; they have to be written back or
                    // the entry would encode as a plain stack and the window would show a fresh part.
                    stack.tag().map(tag -> tag.get(TAG_COMPONENTS))
                            .filter(CompoundTag.class::isInstance)
                            .map(CompoundTag.class::cast)
                            .ifPresent(components -> prefix.add(
                                    TAG_COMPONENTS, CompoundTag.CODEC.encodeStart(ops, components)));
                }
                case FLUID -> {
                    prefix.add(FIELD_FLUID, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }
                case CHEMICAL -> {
                    prefix.add(FIELD_CHEMICAL, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }
                case ESSENTIA -> {
                    prefix.add(FIELD_ESSENTIA, ResourceLocation.CODEC.encodeStart(ops, stack.id()));
                    prefix.add(FIELD_AMOUNT, Codec.LONG.encodeStart(ops, stack.amount()));
                }
                case AE -> {
                    CompoundTag wrapper = new CompoundTag();
                    stack.tag().ifPresent(tag -> wrapper.put(FIELD_KEY, tag));
                    wrapper.putLong(FIELD_AMOUNT, stack.amount());
                    prefix.add(FIELD_AE, CompoundTag.CODEC.encodeStart(ops, wrapper));
                }
            }
            return prefix;
        }
    };

    public static final Codec<GTReactionStack> CODEC = MAP_CODEC.codec();

    /**
     * Wire form.
     *
     * <p>A flat encoding on purpose: the record is small and closed, and a hand-written codec keeps
     * the AE tag from dragging in AE2's own stream codec (which would tie this class to AE2 being
     * installed on both ends of the connection).</p>
     *
     * <p>Typed on plain {@code FriendlyByteBuf} rather than {@code RegistryFriendlyByteBuf}: nothing
     * here resolves through a registry, and the narrower type is what
     * {@code FriendlyByteBuf#readList}/{@code writeCollection} accept.</p>
     */
    public static final StreamCodec<FriendlyByteBuf, GTReactionStack> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull GTReactionStack decode(FriendlyByteBuf buffer) {
                    Kind kind = buffer.readEnum(Kind.class);
                    ResourceLocation id = buffer.readResourceLocation();
                    long amount = buffer.readVarLong();
                    CompoundTag tag = buffer.readNbt();
                    return new GTReactionStack(kind, id, amount, Optional.ofNullable(tag));
                }

                @Override
                public void encode(FriendlyByteBuf buffer, GTReactionStack stack) {
                    buffer.writeEnum(stack.kind());
                    buffer.writeResourceLocation(stack.id());
                    buffer.writeVarLong(stack.amount());
                    buffer.writeNbt(stack.tag().orElse(null));
                }
            };

    public GTReactionStack {
        // Data packs are hand-written, so a bad amount is clamped rather than thrown: an exception
        // here fails the whole recipe file and it would silently vanish from the viewer.
        if (amount <= 0) {
            amount = 1L;
        }
    }

    /** The item this stack names, or {@code null} when it is another kind or the id is unknown. */
    public Item item() {
        if (this.kind != Kind.ITEM) {
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(this.id).orElse(null);
    }

    /**
     * Whether this item entry matches by id alone, ignoring data components.
     *
     * <p>Always false for the non-item kinds: they have no components to ignore.</p>
     */
    public boolean anyComponents() {
        return this.kind == Kind.ITEM
                && this.tag.isPresent()
                && this.tag.get().getBoolean(TAG_ANY_COMPONENTS);
    }

    /**
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

    /**
     * An item entry that remembers the stack's data components.
     *
     * <p>Needed wherever a real stack is being described rather than asked for: the port window lists
     * what it holds, and a worn part must not be listed as a fresh one. A stack with no components of
     * its own carries no tag, so a plain item round-trips exactly as before.</p>
     */
    public static GTReactionStack item(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        CompoundTag components = componentsOf(stack);
        return new GTReactionStack(Kind.ITEM, id, stack.getCount(),
                components == null ? Optional.empty() : Optional.of(components));
    }

    /**
     * The stack's components as a tag, or {@code null} when it has none.
     *
     * <p>Encoded through {@code DataComponents.CODEC} rather than copied by hand so the whole component
     * map travels, including modded ones this class has never heard of.</p>
     */
    @Nullable
    private static CompoundTag componentsOf(ItemStack stack) {
        if (stack.isEmpty() || stack.getComponentsPatch().isEmpty()) {
            return null;
        }
        DataResult<Tag> encoded = DataComponentPatch.CODEC.encodeStart(
                net.minecraft.nbt.NbtOps.INSTANCE, stack.getComponentsPatch());
        Tag tag = encoded.result().orElse(null);
        if (!(tag instanceof CompoundTag compound)) {
            return null;
        }
        CompoundTag wrapper = new CompoundTag();
        wrapper.put(TAG_COMPONENTS, compound);
        return wrapper;
    }

    /** The fluid this stack names, or {@code null} when it is another kind or the id is unknown. */
    public Fluid fluid() {
        if (this.kind != Kind.FLUID) {
            return null;
        }
        return BuiltInRegistries.FLUID.getOptional(this.id).orElse(null);
    }

    /** Expands an item stack, or {@code ItemStack.EMPTY} for the other kinds. */
    public ItemStack toItemStack() {
        Item item = item();
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, (int) Math.min(Integer.MAX_VALUE, this.amount));
        applyComponents(stack);
        return stack;
    }

    /**
     * Puts this entry's recorded components back onto a freshly built stack.
     *
     * <p>A no-op unless the entry was built by {@link #item(ItemStack)} or names components in its
     * recipe: the {@code any_components} marker is not a component set, so it is passed over.</p>
     */
    private void applyComponents(ItemStack stack) {
        CompoundTag wrapper = this.tag.orElse(null);
        if (wrapper == null) {
            return;
        }
        Tag recorded = wrapper.get(TAG_COMPONENTS);
        if (!(recorded instanceof CompoundTag compound)) {
            return;
        }
        DataResult<DataComponentPatch> parsed = DataComponentPatch.CODEC.parse(
                net.minecraft.nbt.NbtOps.INSTANCE, compound);
        parsed.result().ifPresent(stack::applyComponents);
    }

    /** Expands a fluid stack, or {@code FluidStack.EMPTY} for the other kinds. */
    public FluidStack toFluidStack() {
        Fluid fluid = fluid();
        if (fluid == null) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, (int) Math.min(Integer.MAX_VALUE, this.amount));
    }
}

"""The port draws its plate through Create's connected-texture model.

Create's `CTModel` wraps a baked model and, per face, picks a sprite from a `CTSpriteShiftEntry` based
on what the block connects to. The connection question is answered by a `ConnectedTextureBehaviour` we
supply, so one model serves every casing: the behaviour reads the port's own casing and treats the
neighbouring casing walls as connected.

That is the whole reason this beats a model per casing. The behaviour decides, at render time, from
the world — no blockstate enumeration, no copied model files.

The plate still has to come from somewhere. `getShift` receives the sprite the model authored, and the
behaviour returns the shift group for the plate's actual sheet; the port's casing tells it which.
"""
import io

# ---- 1. the port entity carries the casing again -----------------------------------------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s = io.open(p, encoding="utf-8").read()

anchor = '''    private static final String TAG_CHANNEL = "Channel";'''
assert anchor in s

field = '''    private static final String TAG_CHANNEL = "Channel";

    /**
     * The casing this port is set into, as a block id, or {@code null} when it is not part of a
     * machine.
     *
     * <p>On the entity rather than in the block state: a state's value set is closed, and the casing
     * is meant to be open-ended. What reads it is the connected-texture behaviour, which is given the
     * world and the position, so it can ask the entity directly.</p>
     */
    @Nullable
    private ResourceLocation casing;

    /** The casing this port is set into, or {@code null} when it is not part of a machine. */
    @Nullable
    public ResourceLocation casing() {
        return this.casing;
    }

    /** Sets the casing, or clears it with {@code null}. */
    public void setCasing(@Nullable ResourceLocation casing) {
        if (java.util.Objects.equals(this.casing, casing)) {
            return;
        }
        this.casing = casing;
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            // The quads are cached per section, so the rebuild has to be scheduled as well as the
            // data shipped — the second call is what makes the renderer ask again.
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
            this.level.setBlocksDirty(this.worldPosition, getBlockState(), getBlockState());
        }
    }'''
s = s.replace(anchor, field, 1)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("entity carries the casing")

# ---- 2. persistence -----------------------------------------------------------------------------
s = io.open(p, encoding="utf-8").read()
s = s.replace('''    private static final String TAG_CHANNEL = "Channel";''',
'''    private static final String TAG_CHANNEL = "Channel";

    /** The casing this port is set into; absent when it is not part of a machine. */
    private static final String TAG_CASING = "Casing";''', 1)

s = s.replace('''        super.saveAdditional(tag, registries);''',
'''        super.saveAdditional(tag, registries);
        if (this.casing != null) {
            tag.putString(TAG_CASING, this.casing.toString());
        }''', 1)

s = s.replace('''        super.loadAdditional(tag, registries);''',
'''        super.loadAdditional(tag, registries);
        this.casing = tag.contains(TAG_CASING)
                ? ResourceLocation.tryParse(tag.getString(TAG_CASING))
                : null;''', 1)

if "import net.minecraft.resources.ResourceLocation;" not in s:
    s = s.replace("import net.minecraft.nbt.CompoundTag;",
                  "import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.resources.ResourceLocation;", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("casing persisted")

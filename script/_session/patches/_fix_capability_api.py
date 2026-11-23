"""Fix the capability registration to match the real APIs.

`MultiTypeCapability` exposes `block()`, not `getBlockCapability()`, and the essentia capability
wants an `IEssentiaTransport` — which the storage now implements, so the accessor has to hand
that out rather than the raw object.
"""
import io

# ---------------- capabilities ----------------
p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortCapabilities.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''                    mekanism.common.capabilities.Capabilities.CHEMICAL.getBlockCapability(),''',
'''                    mekanism.common.capabilities.Capabilities.CHEMICAL.block(),''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("chemical capability uses block()")

# ---------------- entity accessor for the transport ----------------
p2 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java"
s2 = io.open(p2, encoding="utf-8").read()

s2 = s2.replace('''    /** The essentia container, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object essentiaStorage() {
        ensureHandler();
        return this.essentia;
    }''',
'''    /** The essentia container, or {@code null}. Narrowed by the caller through GTPortStorages. */
    @Nullable
    public Object essentiaStorage() {
        ensureHandler();
        return this.essentia;
    }

    /**
     * The essentia transport the pipe network sees, or {@code null}.
     *
     * <p>Declared with its Thaumcraft type rather than as {@code Object}: this accessor is only
     * called from the capability lambda, which itself only exists inside the Thaumcraft branch of
     * {@code GTPortCapabilities}. Naming the interface here costs nothing on a world without the
     * mod — this class is only linked when the port entity is, and the field it reads stays
     * {@code Object}.</p>
     */
    @Nullable
    public Object essentiaTransportObject() {
        ensureHandler();
        return this.essentia;
    }''', 1)

io.open(p2, "w", encoding="utf-8", newline="\n").write(s2)
print("entity accessor added")

# ---------------- point the capability at the object accessor ----------------
p3 = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortCapabilities.java"
s3 = io.open(p3, encoding="utf-8").read()
s3 = s3.replace('''                    (port, side) -> port.kind() == GTPortKind.ESSENTIA
                            ? port.essentiaTransport() : null);''',
'''                    (port, side) -> port.kind() == GTPortKind.ESSENTIA
                            ? (thaumcraft.api.aspects.IEssentiaTransport) port.essentiaTransportObject()
                            : null);''', 1)
io.open(p3, "w", encoding="utf-8", newline="\n").write(s3)
print("essentia capability cast fixed")

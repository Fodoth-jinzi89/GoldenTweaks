"""Make GTPortTier the single source of each tier's code and colour.

Three places were keeping their own copy of the ladder — the item name, the Jade line and
the block tint — and the last six tiers only existed in some of them. The enum now owns both
the code and the RGB, which is exact for brown and cyan where ChatFormatting had nothing.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortTier.java"
s = io.open(p, encoding="utf-8").read()

# ---------------- constructor takes the colour ----------------
s = s.replace('''    ULV("ulv", "ULV"),
    LV("lv", "LV"),
    MV("mv", "MV"),
    HV("hv", "HV"),
    EV("ev", "EV"),
    IV("iv", "IV"),
    LUV("luv", "LuV"),
    ZPM("zpm", "ZPM"),
    UV("uv", "UV"),''',
'''    ULV("ulv", "ULV", 0xFF5555),
    LV("lv", "LV", 0xAAAAAA),
    MV("mv", "MV", 0xFFAA00),
    HV("hv", "HV", 0xFFFF55),
    EV("ev", "EV", 0x555555),
    IV("iv", "IV", 0x5555FF),
    LUV("luv", "LuV", 0xFF55FF),
    ZPM("zpm", "ZPM", 0x55FFFF),
    UV("uv", "UV", 0x00AA00),''', 1)

s = s.replace('''    UHV("uhv", "UHV"),
    UEV("uev", "UEV"),
    UIV("uiv", "UIV"),
    UMV("umv", "UMV"),
    UXV("uxv", "UXV"),
    MAX("max", "MAX");''',
'''    UHV("uhv", "UHV", 0x8B0000),
    UEV("uev", "UEV", 0x800080),
    UIV("uiv", "UIV", 0x00008B),
    UMV("umv", "UMV", 0x6B4423),
    UXV("uxv", "UXV", 0x00FFFF),
    MAX("max", "MAX", 0xFFFFFF);''', 1)

s = s.replace('''    /** Shorthand the player reads in names, e.g. {@code ULV}. */
    private final String code;

    GTPortTier(String id, String code) {
        this.id = id;
        this.code = code;
    }''',
'''    /** Shorthand the player reads in names, e.g. {@code ULV}. */
    private final String code;
    /**
     * The tier's colour, as {@code 0xRRGGBB}.
     *
     * <p>Plain RGB rather than {@code ChatFormatting}: that enum has sixteen fixed colours and no
     * brown or cyan, so UMV and UXV could only be approximated there. Owning the value here also
     * means the ring on the block, the code in the item name and the line in Jade are all drawn
     * from one number instead of three tables that can drift.</p>
     */
    private final int colour;

    GTPortTier(String id, String code, int colour) {
        this.id = id;
        this.code = code;
        this.colour = colour;
    }''', 1)

s = s.replace('''    public String code() {
        return this.code;
    }''',
'''    public String code() {
        return this.code;
    }

    /** The tier's colour as {@code 0xRRGGBB}, without an alpha channel. */
    public int colour() {
        return this.colour;
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("GTPortTier owns the colours")

"""Extend GTPortTier past UV and clamp the AMI bridge.

AMI's own MultiblockPortTier stops at IX, so every tier above that has to answer IX when
its tier is asked for by the structure scan. Clamping keeps the ladder readable in our
code without reaching past the end of AMI's enum.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortTier.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''    ULV("ulv", "ULV"),
    LV("lv", "LV"),
    MV("mv", "MV"),
    HV("hv", "HV"),
    EV("ev", "EV"),
    IV("iv", "IV"),
    LUV("luv", "LuV"),
    ZPM("zpm", "ZPM"),
    UV("uv", "UV");''',
'''    ULV("ulv", "ULV"),
    LV("lv", "LV"),
    MV("mv", "MV"),
    HV("hv", "HV"),
    EV("ev", "EV"),
    IV("iv", "IV"),
    LUV("luv", "LuV"),
    ZPM("zpm", "ZPM"),
    UV("uv", "UV"),
    // Past AMI's own ladder, which ends at IX. These are ours alone: the structure scan clamps
    // them to IX (see #ami), because a port that the machine accepts as a tier IX hatch is still
    // a hatch, and refusing to assemble around one would help nobody.
    UHV("uhv", "UHV"),
    UEV("uev", "UEV"),
    UIV("uiv", "UIV"),
    UMV("umv", "UMV"),
    UXV("uxv", "UXV"),
    MAX("max", "MAX");''', 1)

s = s.replace('''    /** The AMI tier of the same rank, which the structure scan reads. */
    public MultiblockPortTier ami() {
        return MultiblockPortTier.values()[ordinal()];
    }''',
'''    /**
     * The AMI tier of the same rank, which the structure scan reads.
     *
     * <p>Clamped to AMI's top: its enum ends at IX, and the tiers above UV exist only on our side.
     * Reporting IX keeps them usable as structure parts, which is the only thing this bridge is
     * for.</p>
     */
    public MultiblockPortTier ami() {
        MultiblockPortTier[] all = MultiblockPortTier.values();
        return all[Math.min(all.length - 1, ordinal())];
    }''', 1)

# Throughput tables: the new tiers sit above UV, so every step doubles from there.
s = s.replace('''            case ZPM -> 256;
            case UV -> 1024;
        };
    }''',
'''            case ZPM -> 256;
            case UV -> 1024;
            case UHV -> 4096;
            case UEV -> 16384;
            case UIV -> 65536;
            case UMV -> 262144;
            case UXV -> 1048576;
            case MAX -> Integer.MAX_VALUE;
        };
    }''', 1)

s = s.replace('''            case ZPM -> 256000;
            case UV -> 1024000;
        };
    }''',
'''            case ZPM -> 256000;
            case UV -> 1024000;
            case UHV -> 4096000;
            case UEV -> 16384000;
            case UIV -> 65536000;
            case UMV -> 262144000;
            case UXV -> Integer.MAX_VALUE / 2;
            case MAX -> Integer.MAX_VALUE;
        };
    }''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("GTPortTier extended")

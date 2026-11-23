"""The JEI plugin was still running under TMRV, which is why the advanced page appeared under the
basic category.

TMRV re-implements the JEI API on top of EMI. The existing guard asked `ModList` whether
`toomanyrecipeviewers` was loaded, but TMRV deliberately hides that: JEI's own mod lookups then
report EMI as absent, and the same mechanism keeps the plugin's guard from seeing it either.

The consequence was two pages per recipe — EMI's native one (correctly categorised) and TMRV's
conversion of the JEI category (filed under `goldentweaks:chemical_reaction`, because a JEI recipe
has no `getCategory`) — so the advanced recipe showed up in the basic tab.

The guard now asks the one thing that cannot be hidden: whether EMI's own classes are on the
classpath. TMRV cannot present itself without EMI, and with real JEI alone EMI is absent.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionJeiPlugin.java"
s = io.open(p, encoding="utf-8").read()

old = '''    /** See the class comment: real JEI only, and only when the machine itself exists. */
    private static boolean shouldLoad() {
        return ModList.get().isLoaded(AMI) && !ModList.get().isLoaded(TMRV);
    }'''
new = '''    /**
     * Real JEI only, and only when the machine itself exists.
     *
     * <p>EMI's presence is what decides this, not whether TMRV reports itself: TMRV runs the JEI
     * API on top of EMI and hides its own mod id from the very lookups this check would use. With
     * EMI on the classpath the native pages already cover every recipe, so this plugin stands
     * down; otherwise it is the only viewer integration there is.</p>
     */
    private static boolean shouldLoad() {
        return ModList.get().isLoaded(AMI) && !hasEmi();
    }

    /** Whether EMI is present. Asked of the classpath, which no shim can disguise. */
    private static boolean hasEmi() {
        try {
            Class.forName("dev.emi.emi.api.EmiPlugin", false,
                    GTChemicalReactionJeiPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException absent) {
            return false;
        }
    }'''
assert old in s
s = s.replace(old, new, 1)

# the TMRV constant is no longer read
s = s.replace('''    private static final String TMRV = "toomanyrecipeviewers";
''', "", 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("JEI plugin stands down whenever EMI is present")

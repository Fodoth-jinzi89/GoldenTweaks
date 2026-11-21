import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- constants: geometry shared by both panes ----------------
s = s.replace('''    /** Circuit this pane describes, as written on the item. */''',
'''    /** Circuit this pane describes, as written on the item. */''', 1)

s = s.replace('''    /** The circuit this pane describes, as written on the item. */
    private static final String SIDEBAR_OVERCLOCK_RATIO = "4/2";''',
'''    /** The circuit this pane describes, as written on the item. */
    private static final String SIDEBAR_OVERCLOCK_RATIO = "4/2";

    /**
     * The second pane, stacked directly under the first.
     *
     * <p>Its own artwork is a 32 by 32 icon rather than a frame, so the frame is shared: both panes
     * draw the same tab and panel, and only the icon and the two lines differ.</p>
     */
    private static final ResourceLocation PARALLEL_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "textures/gui/cc_parallel.png");
    /** Vertical gap between the two panes' collapsed tabs. */
    private static final int SIDEBAR_STACK_STEP = SIDEBAR_MIN_HEIGHT + 2;
    /** The parallel icon fills its own 16 px slot, from a 32 px source. */
    private static final int PARALLEL_ICON_SIZE = 16;''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('step1:', 'changed' if s != orig else 'NO CHANGE')

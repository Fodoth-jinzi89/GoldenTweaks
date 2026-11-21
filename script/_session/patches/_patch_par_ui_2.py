import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/client/GTChemicalReactorScreen.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---------------- replace the flat state with two panes ----------------
old_state = '''    /** Whether the side pane is opened out, and whether it has finished opening. */
    private boolean sidebarOpen;
    private boolean sidebarFullyOpen;
    /** Current revealed size, animated between the collapsed and expanded bounds. */
    private int sidebarWidth = SIDEBAR_MIN_WIDTH;
    private int sidebarHeight = SIDEBAR_MIN_HEIGHT;

    /** The overclock ceiling's input box. */
    private EditBox overclockField;

    /**
     * The value sent to the server and not yet read back, or {@code null}.
     *
     * <p>The round trip takes a few ticks, during which the machine still reports the old number.
     * Echoing that back would make the box flash the previous value before the edit lands, so it is
     * held at the sent value until the machine agrees.</p>
     */
    private Integer pendingOverclock;'''

new_state = '''    /**
     * One side pane: its own reveal state, its own box.
     *
     * <p>The two panes share their artwork and their layout, so they share the drawing code; only
     * the icon, the two lines and the number they edit differ. Keeping the animated size per pane is
     * what lets them be opened one at a time, which is how the reference behaves too.</p>
     */
    private static final class Pane {
        boolean open;
        boolean fullyOpen;
        int width = SIDEBAR_MIN_WIDTH;
        int height = SIDEBAR_MIN_HEIGHT;
        @Nullable
        EditBox field;
        /** Value sent to the server and not yet read back. */
        @Nullable
        Integer pending;

        void step() {
            if (this.open) {
                this.width = Math.min(SIDEBAR_MAX_WIDTH, this.width + SIDEBAR_EXPAND_SPEED);
                this.height = Math.min(SIDEBAR_MAX_HEIGHT, this.height + SIDEBAR_EXPAND_SPEED);
            } else {
                this.width = Math.max(SIDEBAR_MIN_WIDTH, this.width - SIDEBAR_EXPAND_SPEED);
                this.height = Math.max(SIDEBAR_MIN_HEIGHT, this.height - SIDEBAR_EXPAND_SPEED);
            }
            this.fullyOpen = this.open
                    && this.width == SIDEBAR_MAX_WIDTH
                    && this.height == SIDEBAR_MAX_HEIGHT;
        }

        boolean collapsed() {
            return this.width == SIDEBAR_MIN_WIDTH && this.height == SIDEBAR_MIN_HEIGHT;
        }
    }

    /** Overclock pane, then parallel pane, top to bottom. */
    private final Pane overclockPane = new Pane();
    private final Pane parallelPane = new Pane();
    private final List<Pane> panes = List.of(this.overclockPane, this.parallelPane);'''

s = s.replace(old_state, new_state, 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

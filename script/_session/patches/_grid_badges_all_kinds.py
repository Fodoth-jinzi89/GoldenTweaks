"""The grid badges every kind that EMI does not badge itself.

EMI prints a count as part of the stack, but only for its item flavour: fluids, chemicals and AE
keys draw an icon and nothing else. The grid therefore adds the label for the kinds that need one,
using the same formatter the port window uses so both screens print a quantity identically.

Essentia counts in whole units like an item; everything else is a bulk amount and gets the fluid
shape, which appends the unit.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/recipe/client/GTChemicalReactionPagedGrid.java"
s = io.open(p, encoding="utf-8").read()

# ---- imports ---------------------------------------------------------------------------------
for imp in [
    "import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTReactionStack;",
]:
    if imp not in s:
        s = s.replace("import net.fodoth.skina.goldentweaks.content.multiblock.GTPortAmounts;",
                      imp + "\nimport net.fodoth.skina.goldentweaks.content.multiblock.GTPortAmounts;", 1)

# ---- fields: keep the data-pack entries alongside the converted stacks -------------------------
s = s.replace('''    private final List<EmiIngredient> stacks;
    private final int x;''',
'''    private final List<EmiIngredient> stacks;
    /** The data-pack entries behind {@link #stacks}, one for one; they carry the kind and amount. */
    private final List<GTReactionStack> entries;
    private final int x;''', 1)

s = s.replace('''    public GTChemicalReactionPagedGrid(List<EmiIngredient> stacks, int x, int y, WidgetHolder holder) {
        this.stacks = List.copyOf(stacks);
        this.x = x;''',
'''    public GTChemicalReactionPagedGrid(List<EmiIngredient> stacks, List<GTReactionStack> entries,
                                       int x, int y, WidgetHolder holder) {
        this.stacks = List.copyOf(stacks);
        this.entries = List.copyOf(entries);
        this.x = x;''', 1)

# ---- applyPage hands the entry to its slot too ------------------------------------------------
s = s.replace('''            this.slots.get(index).setContent(
                    stackIndex < this.stacks.size() ? this.stacks.get(stackIndex) : EmiStack.EMPTY);''',
'''            boolean present = stackIndex < this.stacks.size();
            this.slots.get(index).setContent(
                    present ? this.stacks.get(stackIndex) : EmiStack.EMPTY,
                    present ? this.entries.get(stackIndex) : null);''', 1)

# ---- the slot draws the badge for what EMI leaves unbadged ------------------------------------
old_slot = '''        private EmiIngredient content = EmiStack.EMPTY;

        private PagedSlot(int x, int y) {
            super(EmiStack.EMPTY, x, y);
        }

        private void setContent(EmiIngredient content) {
            this.content = content;
        }'''
new_slot = '''        private EmiIngredient content = EmiStack.EMPTY;

        /** The data-pack entry this cell shows, or {@code null} past the end of the list. */
        private GTReactionStack entry;

        private PagedSlot(int x, int y) {
            super(EmiStack.EMPTY, x, y);
        }

        private void setContent(EmiIngredient content, @Nullable GTReactionStack entry) {
            this.content = content;
            this.entry = entry;
        }'''
assert old_slot in s
s = s.replace(old_slot, new_slot, 1)

old_overlay = '''        /**
         * Adds the amount label for fluids.
         *
         * <p>EMI draws the count as part of the stack itself, and only the item flavour does so:
         * {@code ItemEmiStack.render} prints it, while {@code FluidEmiStack.render} handles just the
         * icon and the remainder marker. That is why an AE key shows its count and a fluid silently
         * does not.</p>
         *
         * <p>The text comes from the shared port formatter, which delegates to AE2's own amount
         * format when AE2 is present and falls back to the plain number otherwise. That keeps this
         * badge identical to the one on the machine's port screen and to what AE's terminals print
         * for the same quantity.</p>
         */
        @Override
        public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.drawOverlay(graphics, mouseX, mouseY, delta);

            for (EmiStack stack : this.content.getEmiStacks()) {
                if (!(stack instanceof FluidEmiStack fluid)) {
                    continue;
                }
                long amount = fluid.getAmount();
                // A single unit prints no label, exactly as an item stack of one behaves.
                if (amount <= 1) {
                    continue;
                }
                EmiRenderHelper.renderAmount(
                        EmiDrawContext.wrap(graphics),
                        getBounds().x(),
                        getBounds().y(),
                        Component.literal(GTPortAmounts.number(amount, false)));
                break;
            }
        }'''
new_overlay = '''        /**
         * Adds the amount label for every kind EMI does not label itself.
         *
         * <p>EMI draws the count as part of the stack, but only its item flavour does so:
         * {@code ItemEmiStack.render} prints it while the fluid, chemical and AE flavours draw an
         * icon and nothing else. Essentia is a stack of this mod's own and prints nothing either.</p>
         *
         * <p>The text comes from the shared port formatter, which delegates to AE2's own amount
         * format when AE2 is present, so the badge matches the machine's port window for the same
         * quantity. Essentia and items are counted in whole units and get the item shape; the bulk
         * kinds get the fluid shape, which appends the unit.</p>
         */
        @Override
        public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.drawOverlay(graphics, mouseX, mouseY, delta);

            GTReactionStack stack = this.entry;
            if (stack == null || stack.kind() == GTReactionStack.Kind.ITEM) {
                // Items are already labelled by EMI, and drawing a second label would double it.
                return;
            }

            long amount = stack.amount();
            // A single unit prints no label, exactly as an item stack of one behaves.
            if (amount <= 1) {
                return;
            }

            boolean counted = stack.kind() == GTReactionStack.Kind.ESSENTIA;
            EmiRenderHelper.renderAmount(
                    EmiDrawContext.wrap(graphics),
                    getBounds().x(),
                    getBounds().y(),
                    Component.literal(GTPortAmounts.number(amount, counted)));
        }'''
assert old_overlay in s
s = s.replace(old_overlay, new_overlay, 1)

if "import org.jetbrains.annotations.Nullable;" not in s:
    lines = s.split("\n")
    last = max(i for i, l in enumerate(lines) if l.startswith("import "))
    lines.insert(last + 1, "import org.jetbrains.annotations.Nullable;")
    s = "\n".join(lines)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("grid badges essentia, fluid, chemical and AE amounts")

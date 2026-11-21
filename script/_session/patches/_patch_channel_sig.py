import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# field for the signature
s = s.replace("""    @Nullable
    private Integer selectedChannel;""",
"""    @Nullable
    private Integer selectedChannel;

    /** Channel text of every port as of the last tick, used to notice edits. */
    private String lastChannelSignature = "";""", 1)

# call the check at the top of tickRecipe's idle branch
s = s.replace("""        if (this.activeRecipe == null) {
            // Off means no new work; it does not stop a craft that is already running.
            if (!mayStartRecipe()) {
                return;
            }""",
"""        if (this.activeRecipe == null) {
            // Off means no new work; it does not stop a craft that is already running.
            if (!mayStartRecipe()) {
                return;
            }
            // A player may have re-channeled a port since the last attempt. The cache is keyed on
            // contents, which a channel edit does not touch, so it has to be dropped here or the
            // machine would keep matching against the old grouping.
            invalidateIfChannelsChanged();""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

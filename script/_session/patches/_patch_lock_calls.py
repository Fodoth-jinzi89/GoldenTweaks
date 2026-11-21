import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# lock after first start
s = s.replace("""            // Pin the group this recipe came from. Everything the craft does from here — drawing
            // energy, delivering products — stays inside it, so a later rearrangement of the ports
            // cannot move the results to a different production line.
            this.activeChannel = this.selectedChannel;""",
"""            // Pin the group this recipe came from. Everything the craft does from here — drawing
            // energy, delivering products — stays inside it, so a later rearrangement of the ports
            // cannot move the results to a different production line.
            this.activeChannel = this.selectedChannel;
            setChannelsLocked(true);""", 1)

# lock after restart in the same tick
s = s.replace("""        this.activeChannel = this.selectedChannel;
        consumeInputs(nextBatched);""",
"""        this.activeChannel = this.selectedChannel;
        setChannelsLocked(true);
        consumeInputs(nextBatched);""", 1)

# unlock when a craft completes
s = s.replace("""        this.activeChannel = null;
        this.selectedChannel = null;""",
"""        this.activeChannel = null;
        this.selectedChannel = null;
        setChannelsLocked(false);""", 1)

# helper
s = s.replace("""    /** Whether a port may be used under the current selection. */
    private boolean portAllowed(MultiblockPortBinding port) {""",
"""    /**
     * Freezes or unfreezes every port's channel field.
     *
     * <p>Done while a craft runs so the grouping it was matched against cannot change underneath it.
     * The lock is also released whenever the machine stops for any reason, because a port left
     * frozen would look broken to the player.</p>
     */
    private void setChannelsLocked(boolean locked) {
        if (this.structure == null || this.level == null) {
            return;
        }
        for (MultiblockPortBinding port : this.structure.getPorts()) {
            if (this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                entity.setChannelLocked(locked);
            }
        }
    }

    /** Whether a port may be used under the current selection. */
    private boolean portAllowed(MultiblockPortBinding port) {""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

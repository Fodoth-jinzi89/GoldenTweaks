import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# lock accessor + guard in setText
s = s.replace("""    /** The channels this port takes part in, as parsed from its field. */
    public List<Integer> channels() {""",
"""    /** Whether the channel field is currently frozen by a running craft. */
    public boolean isChannelLocked() {
        return this.channelLocked;
    }

    /**
     * Freezes or unfreezes the channel field.
     *
     * <p>Called by the machine when a craft starts and ends. The window reads it to grey the field
     * out, and {@link #setText} refuses the edit outright so a stale packet cannot slip through.</p>
     */
    public void setChannelLocked(boolean locked) {
        if (this.channelLocked == locked) {
            return;
        }
        this.channelLocked = locked;
        onContentChanged();
    }

    /** The channels this port takes part in, as parsed from its field. */
    public List<Integer> channels() {""", 1)

# refuse channel edits while locked
s = s.replace("""        String text = value.length() > TEXT_LIMIT ? value.substring(0, TEXT_LIMIT) : value;
        if (field == Field.CHANNEL) {
            text = GTPortChannels.normalise(text);
        }""",
"""        // A channel edit during a run is refused rather than applied: the craft was matched against
        // the ports as they were, and re-grouping them now would redirect its outputs.
        if (field == Field.CHANNEL && this.channelLocked) {
            return;
        }
        String text = value.length() > TEXT_LIMIT ? value.substring(0, TEXT_LIMIT) : value;
        if (field == Field.CHANNEL) {
            text = GTPortChannels.normalise(text);
        }""", 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

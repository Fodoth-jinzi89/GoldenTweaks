import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# custom name field + tag key
s = s.replace('''    private static final String TAG_ENABLED = "Enabled";''',
'''    private static final String TAG_ENABLED = "Enabled";
    private static final String TAG_CUSTOM_NAME = "CustomName";''', 1)

s = s.replace('''    private String mode = DEFAULT_TEXT;''',
'''    /**
     * The name the player gave this port, or {@code null} for the default one.
     *
     * <p>Held as a component rather than a plain string so it keeps its styling, and so the same
     * value can be written straight onto the dropped item's {@code CUSTOM_NAME}.</p>
     */
    @Nullable
    private Component customName;

    private String mode = DEFAULT_TEXT;''', 1)

# accessors beside the other text accessors
s = s.replace('''    /** The {@code mode} field's text. */''',
'''    /** The name to show, either the player's or the block's own. */
    @Override
    public Component getDisplayName() {
        return this.customName != null ? this.customName
                : Component.translatable("block.goldentweaks.gt_port_"
                        + kind().id() + "_" + mode().name().toLowerCase(java.util.Locale.ROOT));
    }

    /** The player-given name, or {@code null} when the port still uses its default. */
    @Nullable
    public Component customName() {
        return this.customName;
    }

    /**
     * Renames the port, or restores the default wording when given {@code null}.
     *
     * <p>An empty name clears the override rather than storing a blank line, which is what a player
     * deleting the text expects.</p>
     */
    public void setCustomName(@Nullable Component name) {
        this.customName = name;
        onContentChanged();
    }

    /** The {@code mode} field's text. */''', 1)

# drop the old getDisplayName, now replaced above
s = s.replace('''    @Override
    public Component getDisplayName() {
        return Component.translatable("block.goldentweaks.gt_port_"
                + kind().id() + "_" + mode().name().toLowerCase(java.util.Locale.ROOT));
    }

''', '', 1)

# NAME field in the enum
s = s.replace('''    public enum Field {
        MODE,
        CHANNEL,
        PRIORITY
    }''',
'''    public enum Field {
        NAME,
        MODE,
        CHANNEL,
        PRIORITY
    }''', 1)

# setText handles NAME
s = s.replace('''        String text = value.length() > TEXT_LIMIT ? value.substring(0, TEXT_LIMIT) : value;
        if (field == Field.CHANNEL) {
            text = GTPortChannels.normalise(text);
        }
        switch (field) {
            case MODE -> this.mode = text;
            case CHANNEL -> this.channel = text;
            case PRIORITY -> this.priority = text;
        }''',
'''        String text = value.length() > TEXT_LIMIT ? value.substring(0, TEXT_LIMIT) : value;
        if (field == Field.NAME) {
            // The name is not a free-form counter: it is the port's identity, so an empty box puts
            // the default wording back instead of leaving the port unnamed.
            this.customName = text.isBlank() ? null : Component.literal(text);
            onContentChanged();
            return;
        }
        if (field == Field.CHANNEL) {
            text = GTPortChannels.normalise(text);
        }
        switch (field) {
            case MODE -> this.mode = text;
            case CHANNEL -> this.channel = text;
            case PRIORITY -> this.priority = text;
            case NAME -> {
            }
        }''', 1)

# nbt save
s = s.replace('''        tag.putBoolean(TAG_ENABLED, this.enabled);''',
'''        tag.putBoolean(TAG_ENABLED, this.enabled);
        if (this.customName != null) {
            tag.putString(TAG_CUSTOM_NAME,
                    Component.Serializer.toJson(this.customName, registries));
        }''', 1)

# nbt load
s = s.replace('''        this.enabled = !tag.contains(TAG_ENABLED) || tag.getBoolean(TAG_ENABLED);''',
'''        this.enabled = !tag.contains(TAG_ENABLED) || tag.getBoolean(TAG_ENABLED);
        try {
            this.customName = tag.contains(TAG_CUSTOM_NAME)
                    ? Component.Serializer.fromJson(tag.getString(TAG_CUSTOM_NAME), registries)
                    : null;
        } catch (RuntimeException malformed) {
            // A hand-edited or corrupt name must not take the whole block entity down with it.
            this.customName = null;
        }''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# signature must notice mode edits too
s = s.replace('''            if (this.level != null
                    && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                out.append(entity.channelText());
            } else {
                out.append(GTPortChannels.WILDCARD_TEXT);
            }
            out.append(';');''',
'''            if (this.level != null
                    && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                // Mode is part of the signature as well: it decides which recipes a group may run,
                // so editing it has to drop the cached match exactly like a channel edit does.
                out.append(entity.channelText()).append('/').append(entity.modeText());
            } else {
                out.append(GTPortChannels.WILDCARD_TEXT).append('/')
                        .append(GTPortChannels.WILDCARD_TEXT);
            }
            out.append(';');''', 1)

# rename for accuracy: it now tracks mode too
s = s.replace('private String channelSignature() {', 'private String groupingSignature() {', 1)
s = s.replace('private void invalidateIfChannelsChanged() {', 'private void invalidateIfGroupingChanged() {', 1)
s = s.replace('String signature = channelSignature();', 'String signature = groupingSignature();', 1)
s = s.replace('invalidateIfChannelsChanged();', 'invalidateIfGroupingChanged();', 1)
s = s.replace('''     * A description of every port's channel field, used to notice edits.''',
              '''     * A description of every port's channel and mode fields, used to notice edits.''', 1)
s = s.replace('''     * <p>The recipe cache is keyed on what the ports hold, which does not change when a player edits
     * a channel. Concatenating the channel text into this signature and comparing it every tick is
     * what makes an edit invalidate that cache, so the next look at the machine re-groups its ports
     * instead of reusing a match made against the old layout.</p>''',
'''     * <p>The recipe cache is keyed on what the ports hold, which does not change when a player edits
     * a channel or a mode. Folding both fields into this signature and comparing it every tick is
     * what makes such an edit invalidate that cache, so the next look at the machine re-groups its
     * ports instead of reusing a match made against the old layout.</p>''', 1)
s = s.replace('''     * <p>Called before the search runs. Clearing the same fields AMI clears on a reload is what
     * makes the next {@code findRecipe} look again rather than answer from its memoised match.</p>''',
'''     * <p>Called before the search runs. Clearing the same fields AMI clears on a reload is what
     * makes the next search look again rather than reuse a stale one.</p>''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('leftover old names:', s.count('channelSignature'), s.count('invalidateIfChannelsChanged'))

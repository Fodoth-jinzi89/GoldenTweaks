import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

s = s.replace('''    /**
     * A description of every port's channel and mode fields, used to notice edits.''',
'''    /**
     * A description of every port's channel, mode and run switch, used to notice edits.
     *
     * <p>The switch belongs here for the same reason the channel does: flipping it changes which
     * ports take part, so the cached grouping and port order no longer describe the machine.</p>
     */
    /**
     * A description of every port's channel and mode fields, used to notice edits.''', 1)

s = s.replace('''            if (this.level != null
                    && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                // Mode is part of the signature as well: it decides which recipes a group may run,
                // so editing it has to drop the cached match exactly like a channel edit does.
                out.append(entity.channelText()).append('/').append(entity.modeText());
            } else {''',
'''            if (this.level != null
                    && this.level.getBlockEntity(port.pos()) instanceof GTPortBlockEntity entity) {
                // Mode and the run switch are part of the signature as well: they decide which
                // recipes a group may run and which ports take part, so editing either has to drop
                // the cached match exactly like a channel edit does.
                out.append(entity.channelText()).append('/').append(entity.modeText())
                        .append('/').append(entity.isEnabled() ? '1' : '0');
            } else {''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

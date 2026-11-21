import io

# 1) field name mismatch in the machine
p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
s = s.replace('this.cachedPortGeneration', 'this.portGenerationCache')
s = s.replace('''    /** Bumped whenever the cached order is dropped, so a stale group check cannot reuse it. */
    private int portGeneration;''',
'''    /** Bumped whenever the cached order is dropped, so a stale group check cannot reuse it. */
    private int portGeneration;

    /** Value of {@link #portGeneration} the cached order was built at. */
    private int portGenerationCache = -1;''')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('machine patched')

# 2) TooltipBake needs a constructor since its fields are final
p2 = r'src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiSearchEngine.java'
s2 = io.open(p2, encoding='utf-8').read()
s2 = s2.replace('''    /** 一个分帧进行的 tooltip 烘焙任务。 */
    private static final class TooltipBake {

        private final Index target;
        private final List<EmiStack> stacks;
        private final GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();
        private final long started = System.currentTimeMillis();
        private int cursor;
    }''',
'''    /** 一个分帧进行的 tooltip 烘焙任务。 */
    private static final class TooltipBake {

        private final Index target;
        private final List<EmiStack> stacks;
        private final GTEmiSubstringIndex.Builder builder = GTEmiSubstringIndex.builder();
        private final long started = System.currentTimeMillis();
        private int cursor;

        private TooltipBake(Index target, List<EmiStack> stacks) {
            this.target = target;
            this.stacks = stacks;
        }
    }''')
s2 = s2.replace('''        TooltipBake bake = new TooltipBake();
        bake.target = target;
        bake.stacks = List.copyOf(DEFERRED_TOOLTIP_STACKS);
        pendingTooltipBake = bake;''',
'''        pendingTooltipBake = new TooltipBake(target, List.copyOf(DEFERRED_TOOLTIP_STACKS));''')
io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
print('bake patched')

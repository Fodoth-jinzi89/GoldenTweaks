import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# ---- collapse the scaled lists before they are matched, delivered or listed ----
s = s.replace('''        List<ItemStack> inputs = scaleItems(recipe.itemInputs(), multiplier);
        List<FluidStack> fluidInputs = scaleFluids(recipe.fluidInputs(), multiplier);
        List<ItemStack> outputs = scaleItems(recipe.itemOutputs(), multiplier);
        List<FluidStack> fluidOutputs = scaleFluids(recipe.fluidOutputs(), multiplier);''',
'''        // Merged, not merely scaled: a batch that produces four stacks of one item should present as
        // one entry of four hundred, otherwise the list, the space check and the delivery all see a
        // scatter of identical rows competing for separate slots.
        List<ItemStack> inputs = mergeItems(scaleItems(recipe.itemInputs(), multiplier));
        List<FluidStack> fluidInputs = mergeFluids(scaleFluids(recipe.fluidInputs(), multiplier));
        List<ItemStack> outputs = mergeItems(scaleItems(recipe.itemOutputs(), multiplier));
        List<FluidStack> fluidOutputs = mergeFluids(scaleFluids(recipe.fluidOutputs(), multiplier));''', 1)

# the feasibility probe scales too, so it must merge the same way
s = s.replace('''        if (!MultiblockRecipeMatcher.matchesItems(
                scaleItems(recipe.itemInputs(), multiplier), available)) {
            return false;
        }
        if (!MultiblockRecipeMatcher.matchesFluids(
                scaleFluids(recipe.fluidInputs(), multiplier), available)) {
            return false;
        }

        List<ItemStack> outputs = scaleItems(recipe.itemOutputs(), multiplier);
        List<FluidStack> fluidOutputs = scaleFluids(recipe.fluidOutputs(), multiplier);''',
'''        if (!MultiblockRecipeMatcher.matchesItems(
                mergeItems(scaleItems(recipe.itemInputs(), multiplier)), available)) {
            return false;
        }
        if (!MultiblockRecipeMatcher.matchesFluids(
                mergeFluids(scaleFluids(recipe.fluidInputs(), multiplier)), available)) {
            return false;
        }

        List<ItemStack> outputs = mergeItems(scaleItems(recipe.itemOutputs(), multiplier));
        List<FluidStack> fluidOutputs = mergeFluids(scaleFluids(recipe.fluidOutputs(), multiplier));''', 1)

# ---- the two merge helpers ----
s = s.replace('''    /** Multiplies item amounts, leaving empty entries alone. */''',
'''    /**
     * Folds entries of the same item into one, summing their counts.
     *
     * <p>Not the same thing as merging stacks down to the slot limit: this keeps the total in a
     * single entry so the rest of the machine reasons about "how much of this item" rather than
     * about how many rows it happened to be written as.</p>
     */
    private static List<ItemStack> mergeItems(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean folded = false;
            for (int index = 0; index < merged.size(); index++) {
                ItemStack existing = merged.get(index);
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    existing.setCount((int) Math.min(Integer.MAX_VALUE,
                            (long) existing.getCount() + stack.getCount()));
                    folded = true;
                    break;
                }
            }
            if (!folded) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }

    /** Fluid counterpart of {@link #mergeItems}. */
    private static List<FluidStack> mergeFluids(List<FluidStack> stacks) {
        List<FluidStack> merged = new ArrayList<>();
        for (FluidStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean folded = false;
            for (int index = 0; index < merged.size(); index++) {
                FluidStack existing = merged.get(index);
                if (existing.is(stack.getFluid())) {
                    existing.setAmount((int) Math.min(Integer.MAX_VALUE,
                            (long) existing.getAmount() + stack.getAmount()));
                    folded = true;
                    break;
                }
            }
            if (!folded) {
                merged.add(stack.copy());
            }
        }
        return merged;
    }

    /** Multiplies item amounts, leaving empty entries alone. */''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

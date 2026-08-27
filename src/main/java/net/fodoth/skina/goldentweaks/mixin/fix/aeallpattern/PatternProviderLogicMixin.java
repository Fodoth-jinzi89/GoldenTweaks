package net.fodoth.skina.goldentweaks.mixin.fix.aeallpattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import io.github.langqi99.aeallpattern.aggregate.AggregatePatternExpander;
import net.fodoth.skina.goldentweaks.compat.aeallpattern.TechStartPatternCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(value = PatternProviderLogic.class, priority = 1100)
public abstract class PatternProviderLogicMixin {
    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    @Final
    private IManagedGridNode mainNode;

    @Shadow
    @Final
    private AppEngInternalInventory patternInventory;

    @Shadow
    @Final
    private List<IPatternDetails> patterns;

    @Shadow
    @Final
    private Set<AEKey> patternInputs;

    @Inject(
            method = "updatePatterns",
            at = @At("HEAD"),
            order = 0,
            cancellable = true
    )
    private void gt$expandAggregatePatterns(CallbackInfo ci) {
        var level = host.getBlockEntity().getLevel();
        patterns.clear();
        patternInputs.clear();

        for (var stack : patternInventory) {
            List<IPatternDetails> expanded = TechStartPatternCompat.expand(stack, level);
            if (expanded.isEmpty()) {
                expanded = AggregatePatternExpander.expand(stack, level);
            }
            if (expanded.isEmpty()) {
                IPatternDetails decoded = PatternDetailsHelper.decodePattern(stack, level);
                if (decoded != null) {
                    gt$addPattern(decoded);
                }
            } else {
                expanded.forEach(this::gt$addPattern);
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
        ci.cancel();
    }

    @Unique
    private void gt$addPattern(IPatternDetails pattern) {
        patterns.add(pattern);
        for (var input : pattern.getInputs()) {
            for (var possibleInput : input.getPossibleInputs()) {
                patternInputs.add(possibleInput.what().dropSecondary());
            }
        }
    }
}

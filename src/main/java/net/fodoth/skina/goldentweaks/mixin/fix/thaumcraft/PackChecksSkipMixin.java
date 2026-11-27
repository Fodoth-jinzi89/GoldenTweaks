package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.lib.packcheck.PackCheckRules;

import java.util.List;

/**
 * Skips Thaumcraft's automatic pack check.
 *
 * <p>The mod validates its whole datapack every time a world is entered and after every {@code /reload}:
 * it walks every recipe, research entry, loot table and tag, cross-references them, and logs a warning
 * for each inconsistency. In this pack that is thousands of findings — the log line quoted below is one
 * of many — and the work is pure diagnostics: nothing it computes is consumed by gameplay, and refusing
 * to run it changes no behaviour.</p>
 *
 * <pre>[Thaumcraft pack check] arcane recipe #739 (thaumicbases:aura_linker, addon):
 * research TB.AuraLinker does not exist, so no player can craft it</pre>
 *
 * <p>Only {@code runAndLog} is intercepted, which is what the two event hooks call. The
 * {@code /thaumcraft packcheck} command goes through {@code run()} and {@code log()} directly, so the
 * report is still available on demand — this removes the automatic scan, not the capability.</p>
 */
@Mixin(targets = "thaumcraft.common.lib.packcheck.PackChecks", remap = false)
public class PackChecksSkipMixin {

    /**
     * Returns an empty finding list instead of scanning.
     *
     * <p>The return value is discarded at both call sites, so the empty list is only there to satisfy
     * the signature. Starting from {@code runAndLog} rather than {@code run} matters: the outer method
     * also wraps the scan in a try/catch that logs a failure, and skipping at the entry point avoids
     * that path entirely.</p>
     */
    @Inject(method = "runAndLog", at = @At("HEAD"), cancellable = true)
    private static void gt$skipPackCheck(String reason, CallbackInfoReturnable<List<PackCheckRules.Finding>> cir) {
        cir.setReturnValue(List.of());
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.research.ResearchAspectPageLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

@Mixin(value = ResearchAspectPageLayout.class, remap = false)
public abstract class ResearchAspectPageLayoutMixin {

    @Inject(method = "pages", at = @At("RETURN"), cancellable = true)
    private static void gt$tenAspectsPerPage(Collection<String> discovered, java.util.Map<String, Integer> pools,
                                               CallbackInfoReturnable<List<ResearchAspectPageLayout.Page>> cir) {
        List<ResearchAspectPageLayout.Entry> entries = new ArrayList<>();
        for (ResearchAspectPageLayout.Page page : cir.getReturnValue()) {
            entries.addAll(page.entries());
        }
        entries.sort(Comparator.comparingInt((ResearchAspectPageLayout.Entry e) -> gt$tier(e.aspect()))
                .thenComparing(e -> e.aspect().tag()));
        List<ResearchAspectPageLayout.Page> pages = new ArrayList<>();
        for (int start = 0; start < entries.size(); start += 10) {
            pages.add(new ResearchAspectPageLayout.Page(new ArrayList<>(entries.subList(start, Math.min(start + 10, entries.size())))));
        }
        cir.setReturnValue(pages);
    }

    private static int gt$tier(Aspect aspect) {
        if (aspect.isPrimal()) {
            return 1;
        }
        int max = 1;
        for (Aspect component : aspect.components()) {
            max = Math.max(max, gt$tier(component));
        }
        return max + 1;
    }
}

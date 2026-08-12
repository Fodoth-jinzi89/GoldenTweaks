package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public final class GTThaumcraftResearch {

    private static boolean registered;

    private GTThaumcraftResearch() {
    }

    public static void register() {
        if (registered) return;
        registered = true;

        AspectList tags = new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.TOOL, 4)
                .add(Aspect.MECHANISM, 4);

        ResearchItem research = new ResearchItem(
                "GT_INFUSION_INTERCEPTER",
                "ARTIFICE",
                tags,
                -4,  // column (same as INFUSION)
                6,   // row (below INFUSION)
                3,   // complexity
                new ItemStack(GTThaumcraftAdditionalBlocks.INFUSION_INTERCEPTER_ITEM.get())
        )
                .setParents("INFUSION")
                .setConcealed()
                .setPages(
                        new ResearchPage("tc.research_page.GT_INFUSION_INTERCEPTER.1"),
                        new ResearchPage("tc.research_page.GT_INFUSION_INTERCEPTER.2")
                )
                .registerResearchItem();

        GoldenTweaks.LOGGER.info("Registered Thaumcraft research: {}", research.key);

        // Warp Theory Cleanser
        AspectList cleanserTags = new AspectList()
                .add(Aspect.MAGIC, 5)
                .add(Aspect.HEAL, 5)
                .add(Aspect.AURA, 3)
                .add(Aspect.ORDER, 3);

        ResearchItem cleanserResearch = new ResearchItem(
                "GT_WARPTHEORY_CLEANSER",
                "ALCHEMY",
                cleanserTags,
                -4,  // column (right of ARCANESPA)
                -5,  // row (same row as ARCANESPA)
                2,   // complexity
                new ItemStack(GTThaumcraftAdditionalItems.WARPTHEORY_CLEANSER.get())
        )
                .setParents("ARCANESPA")
                .setConcealed()
                .setPages(
                        new ResearchPage("tc.research_page.GT_WARPTHEORY_CLEANSER.1")
                )
                .registerResearchItem();

        GoldenTweaks.LOGGER.info("Registered Thaumcraft research: {}", cleanserResearch.key);
    }
}

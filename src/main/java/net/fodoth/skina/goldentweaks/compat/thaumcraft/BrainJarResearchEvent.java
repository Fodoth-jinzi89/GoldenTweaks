package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.common.blockentities.BrainJarBlockEntity;
import thaumcraft.common.blocks.BrainJarBlock;
import thaumcraft.common.items.ResearchNotesItem;
import thaumcraft.common.research.ResearchNoteData;
import thaumcraft.common.research.TCResearchIndex;
import thaumcraft.common.research.TCResearchManager;
import net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft.accessor.BrainJarBlockEntityAccessor;
import net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft.accessor.ResearchNoteDataAccessor;

public final class BrainJarResearchEvent {
    public static final String RESEARCH_KEY = "GT_BRAIN_JAR_RESEARCHER";
    private static final int XP_PER_COMPLEXITY = 100;

    private BrainJarResearchEvent() {
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player instanceof FakePlayer
                || !(player.level().getBlockState(event.getPos()).getBlock() instanceof BrainJarBlock)
                || !TCResearchManager.has(player, RESEARCH_KEY)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ResearchNotesItem)) {
            return;
        }
        ResearchNoteData note = ResearchNoteData.read(stack);
        if (note == null || note.complete()) {
            return;
        }
        TCResearchIndex.Entry entry = TCResearchIndex.entry(note.key()).orElse(null);
        if (entry == null) {
            return;
        }

        BrainJarBlockEntity brain = player.level().getBlockEntity(event.getPos()) instanceof BrainJarBlockEntity value ? value : null;
        if (brain == null) {
            return;
        }
        int cost = Math.max(1, entry.complexity()) * XP_PER_COMPLEXITY;
        BrainJarBlockEntityAccessor accessor = (BrainJarBlockEntityAccessor) (Object) brain;
        int brainXp = accessor.gt$getXp();
        if ((long) brainXp + player.totalExperience < cost) {
            player.displayClientMessage(Component.translatable("goldentweaks.brain_jar_research.insufficient_xp", cost), true);
        } else {
            int brainPayment = Math.min(brainXp, cost);
            accessor.gt$setXp(brainXp - brainPayment);
            player.giveExperiencePoints(brainPayment - cost);
            ((ResearchNoteDataAccessor) (Object) note).gt$setComplete(true);
            note.write(stack);
            accessor.gt$sync();
            player.displayClientMessage(Component.translatable("goldentweaks.brain_jar_research.completed"), true);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}

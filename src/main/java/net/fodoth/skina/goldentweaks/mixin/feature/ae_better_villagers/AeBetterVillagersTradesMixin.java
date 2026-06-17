package net.fodoth.skina.goldentweaks.mixin.feature.ae_better_villagers;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import cn.dancingsnow.neoecoae.all.NEBlocks;
import cn.dancingsnow.neoecoae.all.NEItems;
import com.glodblock.github.extendedae.common.EAESingletons;
import com.reggarf.mods.aebettervillagers.init.AeBetterVillagersModTrades;
import com.reggarf.mods.aebettervillagers.init.AeBetterVillagersModVillagerProfessions;
import io.github.lounode.ae2cs.common.init.AECSBlocks;
import io.github.lounode.ae2cs.common.init.AECSItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rearth.ae2helpers.ae2helpers;
import thelm.packagedauto.item.PackagedAutoItems;

import java.util.List;

@Mixin(AeBetterVillagersModTrades.class)
public class AeBetterVillagersTradesMixin {

    @Inject(method = "registerTrades", at = @At("HEAD"), cancellable = true)
    private static void rewriteTrades(VillagerTradesEvent event, CallbackInfo ci) {

        var trades = event.getTrades();

        // =========================
        // TOOLSMITH
        // =========================
        if (event.getType() == AeBetterVillagersModVillagerProfessions.AE_TOOLSMITH.get()) {

            clear(trades);

            // T1
            addTool(trades, 1, 4,
                    new ItemStack(AEItems.NETHER_QUARTZ_WRENCH),
                    new ItemStack(AEItems.CERTUS_QUARTZ_WRENCH),
                    new ItemStack(AEItems.CERTUS_QUARTZ_KNIFE),
                    new ItemStack(AEItems.NETHER_QUARTZ_KNIFE));

            // T2
            addTool(trades, 2, 8,
                    new ItemStack(AEItems.CHARGED_STAFF),
                    new ItemStack(AEItems.ENTROPY_MANIPULATOR),
                    new ItemStack(AEItems.COLOR_APPLICATOR),
                    new ItemStack(AEItems.MATTER_CANNON),
                    new ItemStack(AEItems.NETWORK_TOOL),
                    new ItemStack(AEItems.MEMORY_CARD));

            // T3
            addTool(trades, 3, 6,
                    new ItemStack(AEItems.FLUIX_HOE),
                    new ItemStack(AEItems.FLUIX_PICK),
                    new ItemStack(AEItems.FLUIX_AXE),
                    new ItemStack(AEItems.FLUIX_SHOVEL),
                    new ItemStack(AEItems.FLUIX_SWORD));

            // T4
            addTool(trades, 4, 12,
                    new ItemStack(AECSItems.ENDER_CRYSTAL_SWORD.get()),
                    new ItemStack(AECSItems.ENDER_CRYSTAL_PICKAXE.get()),
                    new ItemStack(AECSItems.ENDER_CRYSTAL_AXE.get()),
                    new ItemStack(AECSItems.ENDER_CRYSTAL_SHOVEL.get()),
                    new ItemStack(AECSItems.ENDER_CRYSTAL_HOE.get()));

            // T5
            addTool(trades, 5, 24,
                    new ItemStack(AECSItems.RESONATING_CRYSTAL_SWORD.get()),
                    new ItemStack(AECSItems.RESONATING_CRYSTAL_PICKAXE.get()),
                    new ItemStack(AECSItems.RESONATING_CRYSTAL_AXE.get()),
                    new ItemStack(AECSItems.RESONATING_CRYSTAL_SHOVEL.get()),
                    new ItemStack(AECSItems.RESONATING_CRYSTAL_HOE.get()));

            ci.cancel();
            return;
        }

        // =========================
        // ENGINEER
        // =========================
        if (event.getType() == AeBetterVillagersModVillagerProfessions.AE_ENGINEER.get()) {

            clear(trades);

            addSimple(trades, 1, 4, 16,
                    new ItemStack(AEBlocks.QUARTZ_GLASS, 16),
                    new ItemStack(AEItems.BLANK_PATTERN, 8));

            addSimple(trades, 2, 4, 16,
                    new ItemStack(AEParts.QUARTZ_FIBER, 16),
                    new ItemStack(AEParts.CABLE_ANCHOR, 16));

            addSimple(trades, 2, 8, 16,
                    new ItemStack(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 16),
                    new ItemStack(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT), 16));

            addSimple(trades, 3, 12, 16,
                    new ItemStack(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT), 16));

            addSimple(trades, 3, 32, 16,
                    new ItemStack(AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT), 16));

            addSimple(trades, 4, 6, 16,
                    new ItemStack(AEBlocks.CONTROLLER),
                    new ItemStack(AEBlocks.DRIVE));

            addSimple(trades, 4, 16, 16,
                    new ItemStack(AEParts.PATTERN_PROVIDER, 4),
                    new ItemStack(AEParts.INTERFACE, 4),
                    new ItemStack(AEParts.STORAGE_BUS, 4),
                    new ItemStack(AEParts.IMPORT_BUS, 4),
                    new ItemStack(AEParts.EXPORT_BUS, 4));

            addSimple(trades, 5, 16, 16,
                    new ItemStack(EAESingletons.WIRELESS_CONNECTOR, 2),
                    new ItemStack(AECSBlocks.ENDER_BROADCASTER_BLOCK));

            ci.cancel();
            return;
        }

        // =========================
        // SPECIALIST
        // =========================
        if (event.getType() == AeBetterVillagersModVillagerProfessions.AE_SPECIALIST.get()) {

            clear(trades);

            // T1 cards
            addSimple(trades, 1, 5, 16,
                    new ItemStack(AEItems.REDSTONE_CARD, 4),
                    new ItemStack(AEItems.CAPACITY_CARD, 4),
                    new ItemStack(AEItems.VOID_CARD, 4),
                    new ItemStack(AEItems.CRAFTING_CARD, 4));

            addSimple(trades, 1, 8, 16,
                    new ItemStack(AEItems.FUZZY_CARD, 4),
                    new ItemStack(AEItems.SPEED_CARD, 4),
                    new ItemStack(AEItems.INVERTER_CARD, 4),
                    new ItemStack(AEItems.EQUAL_DISTRIBUTION_CARD, 4),
                    new ItemStack(AEItems.ENERGY_CARD, 4),
                    new ItemStack(ae2helpers.RESULT_IMPORT_CARD.get(), 4),
                    new ItemStack(AECSItems.crystalGrowthCard.get(), 4));


            addSimple(trades, 2, 8, 16,
                    new ItemStack(EAESingletons.EX_PATTERN_PROVIDER),
                    new ItemStack(EAESingletons.EX_INTERFACE),
                    new ItemStack(EAESingletons.EX_DRIVE),
                    new ItemStack(EAESingletons.EX_INSCRIBER),
                    new ItemStack(EAESingletons.EX_ASSEMBLER),
                    new ItemStack(EAESingletons.EX_CHARGER),
                    new ItemStack(EAESingletons.EX_IO_PORT),
                    new ItemStack(EAESingletons.EX_EXPORT_BUS),
                    new ItemStack(EAESingletons.EX_IMPORT_BUS)
            );

            addSimple(trades, 2, 16, 16,
                    new ItemStack(AECSBlocks.ENDER_INTERFACE_BLOCK),
                    new ItemStack(AECSBlocks.RESONATING_PATTERN_PROVIDER_BLOCK),
                    new ItemStack(AECSBlocks.INTEGRATED_INTERFACE_BLOCK)
            );

            addSimple(trades, 3, 8, 16,
                    new ItemStack(PackagedAutoItems.PACKAGER.get()),
                    new ItemStack(PackagedAutoItems.UNPACKAGER.get()),
                    new ItemStack(PackagedAutoItems.PACKAGING_PROVIDER.get()),
                    new ItemStack(PackagedAutoItems.PACKAGER_EXTENSION.get()),
                    new ItemStack(PackagedAutoItems.RECIPE_HOLDER.get(), 2),
                    new ItemStack(NEItems.CRYOTHEUM_CRYSTAL.get())
            );


            addSimple(trades, 3, 32, 16,
                    new ItemStack(AECSBlocks.EX_ENDER_INTERFACE_BLOCK),
                    new ItemStack(AECSBlocks.EX_INTEGRATED_INTERFACE_BLOCK),
                    new ItemStack(AECSBlocks.EX_RESONATING_PATTERN_PROVIDER_BLOCK)
            );

            addSimple(trades, 4, 32, 16,
                    new ItemStack(NEItems.SUPERCONDUCTING_PROCESSOR.get()),
                    new ItemStack(NEBlocks.ALUMINUM_ALLOY_CASING),
                    new ItemStack(NEBlocks.BLACK_TUNGSTEN_ALLOY_CASING)
            );


            addSimpleBlock(trades, 5, 32, 16,
                    new ItemStack(NEItems.ECO_CELL_COMPONENT_16M.get()),
                    new ItemStack(NEItems.ECO_COMPUTATION_CELL_L4.get())
            );


            ci.cancel();
        }
    }

    // =========================
    // helpers
    // =========================

    @Unique
    private static void clear(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        for (int i = 1; i <= 5; i++) {
            trades.get(i).clear();
        }
    }

    @Unique
    private static void addSimple(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
            int tier,
            int emerald,
            int maxUses,
            ItemStack... results
    ) {
        int xp = 5 * tier;
        for (ItemStack result : results) {
            trades.get(tier).add(new BasicItemListing(
                    new ItemStack(Items.EMERALD, emerald),
                    ItemStack.EMPTY,
                    result,
                    maxUses,
                    xp,
                    0.05F
            ));
        }
    }

    @Unique
    private static void addSimpleBlock(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
            int tier,
            int emeraldBlock,
            int maxUses,
            ItemStack... results
    ) {
        int xp = 5 * tier;
        for (ItemStack result : results) {
            trades.get(tier).add(new BasicItemListing(
                    new ItemStack(Blocks.EMERALD_BLOCK, emeraldBlock),
                    ItemStack.EMPTY,
                    result,
                    maxUses,
                    xp,
                    0.05F
            ));
        }
    }

    @Unique
    private static void addTool(
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades,
            int tier,
            int emerald,
            ItemStack... results
    ) {
        int xp = 5 * tier;
        for (ItemStack result : results) {
            trades.get(tier).add(new BasicItemListing(
                    new ItemStack(Items.EMERALD, emerald),
                    ItemStack.EMPTY,
                    result,
                    16,
                    xp,
                    0.05F
            ));
        }
    }
}
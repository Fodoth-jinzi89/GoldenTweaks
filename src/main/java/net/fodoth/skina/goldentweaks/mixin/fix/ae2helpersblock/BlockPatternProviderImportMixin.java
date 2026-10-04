package net.fodoth.skina.goldentweaks.mixin.fix.ae2helpersblock;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.parts.automation.StackWorldBehaviors;
import net.fodoth.skina.goldentweaks.compat.appflux.IGTImportUpgradeHost;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rearth.ae2helpers.ae2helpers;
import rearth.ae2helpers.util.IPatternProviderUpgradeHost;
import rearth.ae2helpers.util.ImportCardConfig;
import rearth.ae2helpers.util.PatternProviderImportContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * GoldenTweaks-owned implementation of the ae2helpers "crafting result import card" behaviour.
 * <p>
 * ae2helpers' own logic bails out unless the provider has exactly one push target (so AE2's block pattern provider,
 * whose default push direction is ALL, never imports). This implementation handles both the block form (importing
 * from every adjacent block, each honouring the card's direction setting) and the panel/cable form.
 * <p>
 * It keeps its own one-slot upgrade inventory, exposed to the menu via {@link IGTImportUpgradeHost}. The card item,
 * its right-click config screen and the {@code ImportCardConfig} component are still provided by ae2helpers.
 */
@Mixin(PatternProviderLogic.class)
public abstract class BlockPatternProviderImportMixin implements IGTImportUpgradeHost {

    @Shadow
    @Final
    private PatternProviderLogicHost host;

    @Shadow
    @Final
    private IManagedGridNode mainNode;

    @Shadow
    @Final
    private IActionSource actionSource;

    @Unique
    private IUpgradeInventory gt$importUpgrades = UpgradeInventories.empty();

    @Unique
    private final Map<AEKey, Long> gt$expectedResults = new HashMap<>();

    @Unique
    private int gt$debugLogs;

    @Unique
    private int gt$cyclesSinceCheck;

    @Unique
    private float gt$cycleDelay = 1f;

    private static final int GT$MAX_CYCLE_DELAY = 10;

    private static final String GT$NBT_UPGRADES = "gt_import_upgrade";

    private static final String GT$NBT_RESULTS = "gt_import_expected";

    @Override
    public IUpgradeInventory gt$getImportUpgrades() {
        return gt$importUpgrades;
    }

    @Inject(
            method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V",
            at = @At("TAIL"))
    private void gt$initImportUpgrades(IManagedGridNode mainNode, PatternProviderLogicHost host,
                                       int patternInventorySize, CallbackInfo ci) {
        Item card = gt$importCard();
        if (card != null) {
            this.gt$importUpgrades = UpgradeInventories.forMachine(card, 1, () -> this.host.saveChanges());
        }
        GoldenTweaks.LOGGER.info("[GT/import] init: card={} inventorySize={}", card, gt$importUpgrades.size());
    }

    @Inject(method = "pushPattern", at = @At("RETURN"))
    private void gt$trackExpectedResults(IPatternDetails patternDetails, KeyCounter[] inputHolder,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !gt$hasImportCard()) {
            return;
        }
        for (var output : patternDetails.getOutputs()) {
            if (output != null) {
                gt$expectedResults.merge(output.what(), output.amount(), Long::sum);
            }
        }
        gt$cycleDelay = 1f;
        gt$cyclesSinceCheck = 0;
        host.saveChanges();
        mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    @Inject(method = "hasWorkToDo", at = @At("RETURN"), cancellable = true)
    private void gt$keepAwakeForImport(CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue()) || !gt$hasImportCard()) {
            return;
        }
        ImportCardConfig config = gt$getConfig();
        if (!config.resultsOnly() || !gt$expectedResults.isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "doWork", at = @At("HEAD"))
    private void gt$logDoWork(CallbackInfoReturnable<Boolean> cir) {
        if (gt$debugLogs < 8) {
            gt$debugLogs++;
            var own = gt$importUpgrades.getStackInSlot(0);
            var ae2Slot = (Object) this instanceof IPatternProviderUpgradeHost host2
                    ? host2.ae2helpers$getUpgradeInventory().getStackInSlot(0)
                    : null;
            GoldenTweaks.LOGGER.info(
                    "[GT/import] doWork: active={} ownSlot0={} ae2Slot0={} hasCard={} targets={} expected={}",
                    mainNode.isActive(),
                    own.isEmpty() ? "<empty>" : own.getDescriptionId(),
                    ae2Slot == null ? "<n/a>" : (ae2Slot.isEmpty() ? "<empty>" : ae2Slot.getDescriptionId()),
                    gt$hasImportCard(), host.getTargets().size(), gt$expectedResults.size());
        }
    }

    @Inject(method = "doWork", at = @At("RETURN"))
    private void gt$importResults(CallbackInfoReturnable<Boolean> cir) {
        if (!mainNode.isActive() || !gt$hasImportCard()) {
            if (!gt$expectedResults.isEmpty()) {
                gt$expectedResults.clear();
                host.saveChanges();
            }
            return;
        }

        ImportCardConfig config = gt$getConfig();
        if (gt$debugLogs < 5) {
            gt$debugLogs++;
            GoldenTweaks.LOGGER.info("[GT/import] card present: active={} targets={} resultsOnly={} expected={}",
                    mainNode.isActive(), host.getTargets().size(), config.resultsOnly(), gt$expectedResults.size());
        }
        if (config.resultsOnly() && gt$expectedResults.isEmpty()) {
            return;
        }

        // Exponential backoff: the provider stays awake, but only build import facades every N ticks.
        gt$cyclesSinceCheck++;
        if (gt$cyclesSinceCheck < (int) gt$cycleDelay) {
            return;
        }
        gt$cyclesSinceCheck = 0;

        Set<Direction> targets = host.getTargets();
        if (targets.isEmpty()) {
            return;
        }

        var grid = mainNode.getGrid();
        BlockEntity blockEntity = host.getBlockEntity();
        if (grid == null || blockEntity == null || !(blockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        boolean imported = false;
        for (Direction side : targets) {
            BlockPos machinePos = pos.relative(side);
            if (level.getBlockEntity(machinePos) == null) {
                continue;
            }

            Direction importSide = config.overriddenDirection();
            if (importSide == null) {
                importSide = side.getOpposite();
            }

            StackImportStrategy strategy = StackWorldBehaviors.createImportFacade(
                    level, machinePos, importSide, type -> true);
            if (strategy == null) {
                continue;
            }

            var context = new PatternProviderImportContext(
                    grid.getStorageService(), grid.getEnergyService(), actionSource,
                    gt$expectedResults, config.resultsOnly());
            strategy.transfer(context);
            gt$consumeImported(context.getImportedItems());
            imported = true;
        }

        if (imported) {
            gt$cycleDelay = 1f;
        } else {
            gt$cycleDelay = Math.min(GT$MAX_CYCLE_DELAY, gt$cycleDelay * 1.15f);
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void gt$writeNbt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        gt$importUpgrades.writeToNBT(tag, GT$NBT_UPGRADES, registries);
        if (!gt$expectedResults.isEmpty()) {
            ListTag list = new ListTag();
            gt$expectedResults.forEach((key, amount) ->
                    list.add(GenericStack.writeTag(registries, new GenericStack(key, amount))));
            tag.put(GT$NBT_RESULTS, list);
        }
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void gt$readNbt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        gt$importUpgrades.readFromNBT(tag, GT$NBT_UPGRADES, registries);
        gt$expectedResults.clear();
        if (tag.contains(GT$NBT_RESULTS)) {
            ListTag list = tag.getList(GT$NBT_RESULTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                GenericStack stack = GenericStack.readTag(registries, list.getCompound(i));
                if (stack != null) {
                    gt$expectedResults.put(stack.what(), stack.amount());
                }
            }
        }
    }

    @Inject(method = "addDrops", at = @At("TAIL"))
    private void gt$dropImportUpgrade(java.util.List<net.minecraft.world.item.ItemStack> drops, CallbackInfo ci) {
        for (var stack : gt$importUpgrades) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
    }

    @Inject(method = "clearContent", at = @At("HEAD"))
    private void gt$clearImport(CallbackInfo ci) {
        gt$expectedResults.clear();
        gt$importUpgrades.clear();
    }

    @Unique
    private void gt$consumeImported(Map<AEKey, Long> importedItems) {
        if (importedItems.isEmpty() || gt$expectedResults.isEmpty()) {
            return;
        }
        var iterator = gt$expectedResults.entrySet().iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long actuallyImported = importedItems.getOrDefault(entry.getKey(), 0L);
            if (actuallyImported <= 0) {
                continue;
            }
            long remaining = entry.getValue() - actuallyImported;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
            changed = true;
        }
        if (changed) {
            host.saveChanges();
        }
    }

    @Unique
    private boolean gt$hasImportCard() {
        return !gt$importCardStack().isEmpty();
    }

    @Unique
    private ImportCardConfig gt$getConfig() {
        return gt$importCardStack().getOrDefault(ae2helpers.IMPORT_CARD_CONFIG.get(), ImportCardConfig.DEFAULT);
    }

    /**
     * All pattern-provider upgrade slots accept any upgrade card, so the import card can end up in the AppliedFlux /
     * extendedae_plus upgrade bar (their own inventory) rather than our dedicated slot. Search every upgrade inventory
     * the provider exposes so the card works no matter which of the slots it was placed in.
     */
    @Unique
    private ItemStack gt$importCardStack() {
        Item card = gt$importCard();
        ItemStack found = gt$findCard(gt$importUpgrades, card);
        if (!found.isEmpty()) {
            return found;
        }
        if ((Object) this instanceof IPatternProviderUpgradeHost ae2Host) {
            found = gt$findCard(ae2Host.ae2helpers$getUpgradeInventory(), card);
            if (!found.isEmpty()) {
                return found;
            }
        }
        if ((Object) this instanceof IUpgradeableObject upgradeable) {
            return gt$findCard(upgradeable.getUpgrades(), card);
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private ItemStack gt$findCard(IUpgradeInventory inventory, Item card) {
        if (card == null) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(card)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Item gt$importCard() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("ae2helpers:result_import_card"))
                .orElse(null);
    }
}

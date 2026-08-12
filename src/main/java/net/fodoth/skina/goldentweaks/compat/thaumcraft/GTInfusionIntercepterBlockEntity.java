package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.blockentities.InfusionMatrixBlockEntity;
import thaumcraft.common.blockentities.PedestalBlockEntity;
import thaumcraft.common.research.PlayerProgressData;

import com.mojang.authlib.GameProfile;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GTInfusionIntercepterBlockEntity extends BlockEntity
        implements IAspectContainer, IEssentiaTransport {

    private static final int STABILITY_AMOUNT = 20;
    private static final int MATRIX_SCAN_MIN = 3;
    private static final int MATRIX_SCAN_MAX = 10;
    private static final int PEDESTAL_SCAN_MIN = 1;
    private static final int PEDESTAL_SCAN_MAX = 8;
    private static final int SOURCE_SCAN_RADIUS = 6;
    private static final int SOURCE_SCAN_HEIGHT = 3;
    private static final int SOURCE_SCAN_INTERVAL = 200; // 10s
    private static final int PEDESTAL_CHECK_INTERVAL = 20; // 1s
    private static final int CRAFT_TRIGGER_DELAY = 20; // 1s after item change

    private static volatile VarHandle MATRIX_INSTABILITY;
    private static volatile VarHandle MATRIX_RECIPE_ESSENTIA;
    private static volatile VarHandle MATRIX_RECIPE_INGREDIENTS;
    private static volatile VarHandle MATRIX_PEDESTALS;

    private AspectList myAspects = new AspectList();
    private Aspect currentSuction;

    // Bound blocks
    @Nullable private BlockPos boundMatrixPos;
    @Nullable private BlockPos boundPedestalPos;
    private boolean stabilityHasBeenAdded;

    // Owner
    @Nullable private UUID ownerUuid;
    @Nullable private String ownerName;

    // Cached research (for offline infusion triggering)
    @Nullable private CompoundTag cachedResearchData;

    // Cached essentia sources
    private final List<BlockPos> cachedSources = new ArrayList<>();
    private int sourceScanCooldown;

    // Pedestal item change detection
    private ItemStack lastPedestalItem = ItemStack.EMPTY;
    private int pedestalCheckCooldown;
    private int craftTriggerDelay = -1; // -1=idle, >0=counting down to trigger

    // Particle timers (for one-shot effects)
    private int matrixBindParticles;
    private int pedestalBindParticles;

    public GTInfusionIntercepterBlockEntity(BlockPos pos, BlockState state) {
        super(GTThaumcraftAdditionalBlockEntities.INFUSION_INTERCEPTER.get(), pos, state);
    }

    /** Called from {@link GTInfusionIntercepterBlock#setPlacedBy} */
    public void setOwner(@Nullable Player player) {
        if (player == null) return;
        this.ownerUuid = player.getUUID();
        this.ownerName = player.getName().getString();

        // Cache research progress for offline infusion triggering
        PlayerProgressData data = PlayerProgressData.get(player);
        if (data != null && level != null) {
            this.cachedResearchData = data.serializeNBT(level.registryAccess());
        }
        setChanged();
    }

    /* ------------------------------------------------------ */
    /* VarHandle initialization                                 */
    /* ------------------------------------------------------ */

    private static VarHandle matrixInstabilityHandle() {
        VarHandle h = MATRIX_INSTABILITY;
        if (h == null) {
            synchronized (GTInfusionIntercepterBlockEntity.class) {
                h = MATRIX_INSTABILITY;
                if (h == null) {
                    try {
                        h = MethodHandles.privateLookupIn(InfusionMatrixBlockEntity.class, MethodHandles.lookup())
                                .findVarHandle(InfusionMatrixBlockEntity.class, "instability", int.class);
                        MATRIX_INSTABILITY = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error("VarHandle: InfusionMatrixBlockEntity.instability", e);
                    }
                }
            }
        }
        return h;
    }

    private static VarHandle matrixRecipeEssentiaHandle() {
        VarHandle h = MATRIX_RECIPE_ESSENTIA;
        if (h == null) {
            synchronized (GTInfusionIntercepterBlockEntity.class) {
                h = MATRIX_RECIPE_ESSENTIA;
                if (h == null) {
                    try {
                        h = MethodHandles.privateLookupIn(InfusionMatrixBlockEntity.class, MethodHandles.lookup())
                                .findVarHandle(InfusionMatrixBlockEntity.class, "recipeEssentia", AspectList.class);
                        MATRIX_RECIPE_ESSENTIA = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error("VarHandle: InfusionMatrixBlockEntity.recipeEssentia", e);
                    }
                }
            }
        }
        return h;
    }

    private static int getMatrixInstability(InfusionMatrixBlockEntity matrix) {
        VarHandle h = matrixInstabilityHandle();
        return h != null ? (int) h.get(matrix) : 0;
    }

    private static void setMatrixInstability(InfusionMatrixBlockEntity matrix, int value) {
        VarHandle h = matrixInstabilityHandle();
        if (h != null) h.set(matrix, value);
    }

    private static AspectList getMatrixRecipeEssentia(InfusionMatrixBlockEntity matrix) {
        VarHandle h = matrixRecipeEssentiaHandle();
        return h != null ? (AspectList) h.get(matrix) : null;
    }

    private static VarHandle matrixRecipeIngredientsHandle() {
        VarHandle h = MATRIX_RECIPE_INGREDIENTS;
        if (h == null) {
            synchronized (GTInfusionIntercepterBlockEntity.class) {
                h = MATRIX_RECIPE_INGREDIENTS;
                if (h == null) {
                    try {
                        h = MethodHandles.privateLookupIn(InfusionMatrixBlockEntity.class, MethodHandles.lookup())
                                .findVarHandle(InfusionMatrixBlockEntity.class, "recipeIngredients", ArrayList.class);
                        MATRIX_RECIPE_INGREDIENTS = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error("VarHandle: InfusionMatrixBlockEntity.recipeIngredients", e);
                    }
                }
            }
        }
        return h;
    }

    private static VarHandle matrixPedestalsHandle() {
        VarHandle h = MATRIX_PEDESTALS;
        if (h == null) {
            synchronized (GTInfusionIntercepterBlockEntity.class) {
                h = MATRIX_PEDESTALS;
                if (h == null) {
                    try {
                        h = MethodHandles.privateLookupIn(InfusionMatrixBlockEntity.class, MethodHandles.lookup())
                                .findVarHandle(InfusionMatrixBlockEntity.class, "pedestals", ArrayList.class);
                        MATRIX_PEDESTALS = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error("VarHandle: InfusionMatrixBlockEntity.pedestals", e);
                    }
                }
            }
        }
        return h;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<ItemStack> getMatrixRecipeIngredients(InfusionMatrixBlockEntity matrix) {
        VarHandle h = matrixRecipeIngredientsHandle();
        return h != null ? (ArrayList<ItemStack>) h.get(matrix) : null;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<BlockPos> getMatrixPedestals(InfusionMatrixBlockEntity matrix) {
        VarHandle h = matrixPedestalsHandle();
        return h != null ? (ArrayList<BlockPos>) h.get(matrix) : null;
    }

    /* ------------------------------------------------------ */
    /* Tick                                                    */
    /* ------------------------------------------------------ */

    public static void tick(Level level, BlockPos pos, BlockState state, GTInfusionIntercepterBlockEntity be) {
        if (level.isClientSide()) return;

        be.maintainMatrixBinding();
        be.maintainPedestalBinding();
        be.scanEssentiaSources();
        be.checkPedestalAndTrigger();
        be.tickCraftTrigger();
        be.findAndFeedEssentia();
        be.fastForwardItems();
        be.tickParticles();
    }

    private void tickParticles() {
        if (matrixBindParticles > 0) {
            matrixBindParticles--;
            if (boundMatrixPos != null && level instanceof ServerLevel sl) {
                spawnBindingParticles(sl, boundMatrixPos, ParticleTypes.WITCH, 8);
            }
        }
        if (pedestalBindParticles > 0) {
            pedestalBindParticles--;
            if (boundPedestalPos != null && level instanceof ServerLevel sl) {
                spawnBindingParticles(sl, boundPedestalPos, ParticleTypes.ENCHANT, 5);
            }
        }
    }

    private void spawnBindingParticles(ServerLevel level, BlockPos target, net.minecraft.core.particles.ParticleOptions particle, int count) {
        double cx = target.getX() + 0.5;
        double cy = target.getY() + 0.5;
        double cz = target.getZ() + 0.5;
        level.sendParticles(particle, cx, cy, cz, count, 0.25, 0.25, 0.25, 0.02);
    }

    /* ------------------------------------------------------ */
    /* Matrix binding (scan y+3..10, bind nearest)             */
    /* ------------------------------------------------------ */

    private void maintainMatrixBinding() {
        if (level == null) return;

        if (boundMatrixPos != null) {
            BlockEntity be = level.getBlockEntity(boundMatrixPos);
            if (be instanceof InfusionMatrixBlockEntity) return;
            clearBinding();
        }
        scanForMatrix();
    }

    private void scanForMatrix() {
        BlockPos nearest = null;
        int nearestDist = Integer.MAX_VALUE;

        for (int dy = MATRIX_SCAN_MIN; dy <= MATRIX_SCAN_MAX; dy++) {
            BlockPos check = worldPosition.above(dy);
            if (level.getBlockEntity(check) instanceof InfusionMatrixBlockEntity && dy < nearestDist) {
                nearest = check;
                nearestDist = dy;
            }
        }

        if (nearest != null) {
            boolean isNew = boundMatrixPos == null || !boundMatrixPos.equals(nearest);
            boundMatrixPos = nearest.immutable();

            InfusionMatrixBlockEntity matrix = getBoundMatrix();
            if (matrix != null && !stabilityHasBeenAdded) {
                addStability(matrix);
                stabilityHasBeenAdded = true;
            }

            if (isNew) {
                matrixBindParticles = 20; // show particles for 1 second
            }
            setChanged();
        }
    }

    @Nullable
    private InfusionMatrixBlockEntity getBoundMatrix() {
        if (boundMatrixPos == null || level == null) return null;
        BlockEntity be = level.getBlockEntity(boundMatrixPos);
        return be instanceof InfusionMatrixBlockEntity m ? m : null;
    }

    private void clearBinding() {
        if (stabilityHasBeenAdded) {
            InfusionMatrixBlockEntity matrix = getBoundMatrix();
            if (matrix != null) {
                setMatrixInstability(matrix, getMatrixInstability(matrix) + STABILITY_AMOUNT);
            }
        }
        boundMatrixPos = null;
        stabilityHasBeenAdded = false;
        setChanged();
    }

    /* ------------------------------------------------------ */
    /* Pedestal binding (scan y+1..8, bind nearest)            */
    /* ------------------------------------------------------ */

    private void maintainPedestalBinding() {
        if (level == null) return;

        if (boundPedestalPos != null) {
            BlockEntity be = level.getBlockEntity(boundPedestalPos);
            if (be instanceof PedestalBlockEntity) return;
            boundPedestalPos = null;
        }
        scanForPedestal();
    }

    private void scanForPedestal() {
        BlockPos nearest = null;
        int nearestDist = Integer.MAX_VALUE;

        for (int dy = PEDESTAL_SCAN_MIN; dy <= PEDESTAL_SCAN_MAX; dy++) {
            BlockPos check = worldPosition.above(dy);
            if (level.getBlockEntity(check) instanceof PedestalBlockEntity && dy < nearestDist) {
                nearest = check;
                nearestDist = dy;
            }
        }

        if (nearest != null) {
            boolean isNew = boundPedestalPos == null || !boundPedestalPos.equals(nearest);
            boundPedestalPos = nearest.immutable();
            lastPedestalItem = ItemStack.EMPTY; // force re-check
            if (isNew) {
                pedestalBindParticles = 20;
            }
            setChanged();
        }
    }

    @Nullable
    private PedestalBlockEntity getBoundPedestal() {
        if (boundPedestalPos == null || level == null) return null;
        BlockEntity be = level.getBlockEntity(boundPedestalPos);
        return be instanceof PedestalBlockEntity p ? p : null;
    }

    /* ------------------------------------------------------ */
    /* Pedestal item detection → trigger infusion              */
    /* ------------------------------------------------------ */

    private void checkPedestalAndTrigger() {
        if (pedestalCheckCooldown > 0) {
            pedestalCheckCooldown--;
            return;
        }
        pedestalCheckCooldown = PEDESTAL_CHECK_INTERVAL;

        InfusionMatrixBlockEntity matrix = getBoundMatrix();
        if (matrix == null) return;

        PedestalBlockEntity pedestal = getBoundPedestal();
        if (pedestal == null) return;

        ItemStack current = pedestal.heldStack();
        if (ItemStack.isSameItemSameComponents(current, lastPedestalItem)) return;

        lastPedestalItem = current.copy();

        if (current.isEmpty()) return;
        if (matrix.crafting()) return;

        // Start 1-second countdown before triggering
        craftTriggerDelay = CRAFT_TRIGGER_DELAY;
    }

    private void tickCraftTrigger() {
        if (craftTriggerDelay < 0) return;

        craftTriggerDelay--;
        if (craftTriggerDelay != 0) return;

        craftTriggerDelay = -1;

        InfusionMatrixBlockEntity matrix = getBoundMatrix();
        if (matrix == null || matrix.crafting()) return;

        matrix.activate();
        tryCraftingStart(matrix);
    }

    /**
     * Try to start infusion, preferring online player.
     * Falls back to FakePlayer with cached research if owner is offline.
     */
    private void tryCraftingStart(InfusionMatrixBlockEntity matrix) {
        // 1) Owner online → use directly
        Player owner = getOwnerPlayer();
        if (owner != null) {
            // Sync latest research
            PlayerProgressData data = PlayerProgressData.get(owner);
            if (data != null && level != null) {
                cachedResearchData = data.serializeNBT(level.registryAccess());
            }
            matrix.craftingStart(owner);
            return;
        }

        // 2) Owner offline, cached research exists → FakePlayer
        if (cachedResearchData != null && ownerName != null && level instanceof ServerLevel sl) {
            GameProfile profile = new GameProfile(ownerUuid, ownerName);
            FakePlayer fake = FakePlayerFactory.get(sl, profile);
            if (fake != null) {
                PlayerProgressData fakeData = PlayerProgressData.get(fake);
                if (fakeData != null) {
                    fakeData.deserializeNBT(sl.registryAccess(), cachedResearchData);
                }
                matrix.craftingStart(fake);
                return;
            }
        }

        // 3) Offline and no cache → cannot start
        GoldenTweaks.LOGGER.warn(
                "InfusionIntercepter at {}: cannot start infusion — owner offline and no cached research",
                worldPosition);
    }

    @Nullable
    private Player getOwnerPlayer() {
        if (ownerUuid == null || level == null) return null;
        return level.getPlayerByUUID(ownerUuid);
    }

    /* ------------------------------------------------------ */
    /* Essentia source scanning (every 10s, 13×13×3 area)      */
    /* ------------------------------------------------------ */

    private void scanEssentiaSources() {
        if (level == null) return;

        if (sourceScanCooldown > 0) {
            sourceScanCooldown--;
            return;
        }
        sourceScanCooldown = SOURCE_SCAN_INTERVAL;

        BlockPos center = worldPosition.below();
        int cy = center.getY();
        List<BlockPos> newSources = new ArrayList<>();

        for (int dx = -SOURCE_SCAN_RADIUS; dx <= SOURCE_SCAN_RADIUS; dx++) {
            for (int dz = -SOURCE_SCAN_RADIUS; dz <= SOURCE_SCAN_RADIUS; dz++) {
                for (int dy = -(SOURCE_SCAN_HEIGHT - 1); dy <= 0; dy++) {
                    BlockPos check = new BlockPos(center.getX() + dx, cy + dy, center.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(check);
                    if (be instanceof IEssentiaTransport transport && transport.canOutputTo(Direction.UP)) {
                        BlockPos immutable = check.immutable();
                        if (!cachedSources.contains(immutable)) {
                            cachedSources.add(immutable);
                            newSources.add(immutable);
                        }
                    }
                }
            }
        }

        // Remove stale sources
        cachedSources.removeIf(pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            return !(be instanceof IEssentiaTransport t && t.canOutputTo(Direction.UP));
        });

        // Particle effect at each newly bound source
        if (!newSources.isEmpty() && level instanceof ServerLevel sl) {
            for (BlockPos pos : newSources) {
                spawnBindingParticles(sl, pos, ParticleTypes.WAX_OFF, 3);
            }
        }
    }

    /* ------------------------------------------------------ */
    /* Essentia feeding                                        */
    /* ------------------------------------------------------ */

    private void findAndFeedEssentia() {
        if (level == null) return;

        InfusionMatrixBlockEntity matrix = getBoundMatrix();
        if (matrix == null || !matrix.crafting()) {
            if (!myAspects.isEmpty()) {
                myAspects = new AspectList();
                currentSuction = null;
                setChanged();
            }
            return;
        }

        AspectList recipeEssentia = getMatrixRecipeEssentia(matrix);
        if (recipeEssentia == null || recipeEssentia.isEmpty()) {
            pushToMatrix(matrix);
            return;
        }

        boolean changed = false;
        List<BlockPos> sources = getCachedSources();

        if (!sources.isEmpty()) {
            for (Aspect aspect : recipeEssentia.aspects()) {
                int needed = recipeEssentia.amount(aspect);
                if (needed <= 0) continue;

                currentSuction = aspect;
                int remaining = tryPullFromSources(aspect, needed, sources);
                if (remaining < needed) {
                    recipeEssentia.remove(aspect, needed - remaining);
                    changed = true;
                }
            }
        }

        if (!myAspects.isEmpty()) {
            changed |= pushHeldToMatrix(matrix);
        }

        if (changed) setChanged();
    }

    private int tryPullFromSources(Aspect aspect, int needed, List<BlockPos> sources) {
        int remaining = needed;

        for (BlockPos sourcePos : sources) {
            if (remaining <= 0) break;

            BlockEntity be = level.getBlockEntity(sourcePos);
            if (!(be instanceof IEssentiaTransport source)) continue;

            int available = source.getEssentiaAmount(Direction.UP);
            Aspect sourceType = source.getEssentiaType(Direction.UP);

            if (sourceType != null && sourceType.equals(aspect) && available > 0) {
                int taken = source.takeEssentia(aspect, Math.min(remaining, available), Direction.UP);
                remaining -= taken;
            }
        }
        return remaining;
    }

    private List<BlockPos> getCachedSources() {
        if (cachedSources.isEmpty()) {
            BlockPos below = worldPosition.below();
            BlockEntity be = level.getBlockEntity(below);
            if (be instanceof IEssentiaTransport t && t.canOutputTo(Direction.UP)) {
                List<BlockPos> fb = new ArrayList<>();
                fb.add(below.immutable());
                return fb;
            }
        }
        return cachedSources;
    }

    private boolean pushHeldToMatrix(InfusionMatrixBlockEntity matrix) {
        if (myAspects.isEmpty()) return false;

        AspectList recipeEssentia = getMatrixRecipeEssentia(matrix);
        if (recipeEssentia == null || recipeEssentia.isEmpty()) {
            myAspects = new AspectList();
            currentSuction = null;
            return true;
        }

        boolean changed = false;
        for (Aspect aspect : myAspects.aspects()) {
            int held = myAspects.amount(aspect);
            if (held <= 0) continue;
            int needed = recipeEssentia.amount(aspect);
            if (needed <= 0) continue;

            int transfer = Math.min(held, needed);
            recipeEssentia.remove(aspect, transfer);
            myAspects.remove(aspect, transfer);
            changed = true;
        }

        if (myAspects.isEmpty()) currentSuction = null;
        return changed;
    }

    private void pushToMatrix(InfusionMatrixBlockEntity matrix) {
        pushHeldToMatrix(matrix);
    }

    /* ------------------------------------------------------ */
    /* Item fast-forward (consume all items at once)            */
    /* ------------------------------------------------------ */

    /**
     * When the matrix is in the item-consumption phase (all essentia satisfied),
     * consume all remaining ingredients from their pedestals simultaneously
     * instead of one-by-one, then the matrix will naturally finish the craft
     * on its next cycle.
     */
    private void fastForwardItems() {
        if (level == null) return;

        InfusionMatrixBlockEntity matrix = getBoundMatrix();
        if (matrix == null || !matrix.crafting()) return;

        // Only act when essentia phase is complete
        AspectList recipeEssentia = getMatrixRecipeEssentia(matrix);
        if (recipeEssentia != null && !recipeEssentia.isEmpty()) return;

        ArrayList<ItemStack> ingredients = getMatrixRecipeIngredients(matrix);
        if (ingredients == null || ingredients.isEmpty()) return;

        ArrayList<BlockPos> pedestals = getMatrixPedestals(matrix);
        if (pedestals == null) return;

        // Consume all remaining ingredients from their pedestals
        List<ItemStack> toRemove = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            for (BlockPos pos : pedestals) {
                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof PedestalBlockEntity pedestal)) continue;

                ItemStack held = pedestal.heldStack();
                if (ItemStack.isSameItemSameComponents(held, ingredient)) {
                    held.shrink(1);
                    pedestal.setItem(0, held);
                    toRemove.add(ingredient);
                    break;
                }
            }
        }

        ingredients.removeAll(toRemove);
    }

    /* ------------------------------------------------------ */
    /* Stability                                               */
    /* ------------------------------------------------------ */

    private void addStability(InfusionMatrixBlockEntity matrix) {
        int current = getMatrixInstability(matrix);
        if (current < STABILITY_AMOUNT) return;
        setMatrixInstability(matrix, current - STABILITY_AMOUNT);
    }

    public void removeStability() {
        if (!stabilityHasBeenAdded || level == null) return;
        InfusionMatrixBlockEntity matrix = getBoundMatrix();
        if (matrix != null) {
            setMatrixInstability(matrix, getMatrixInstability(matrix) + STABILITY_AMOUNT);
        }
        stabilityHasBeenAdded = false;
        boundMatrixPos = null;
        setChanged();
    }

    /* ------------------------------------------------------ */
    /* IAspectContainer                                        */
    /* ------------------------------------------------------ */

    @Override public AspectList getAspects() { return myAspects; }

    @Override
    public void setAspects(AspectList aspects) {
        this.myAspects = aspects != null ? aspects : new AspectList();
        setChanged();
    }

    @Override public boolean doesContainerAccept(Aspect aspect) { return true; }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount > 0) { myAspects.add(aspect, amount); setChanged(); return 0; }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        boolean had = doesContainerContainAmount(aspect, amount);
        if (had) { myAspects.remove(aspect, amount); setChanged(); }
        return had;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) { return myAspects.amount(aspect) >= amount; }

    @Override public int containerContains(Aspect aspect) { return myAspects.amount(aspect); }

    /* ------------------------------------------------------ */
    /* IEssentiaTransport                                      */
    /* ------------------------------------------------------ */

    @Override public boolean isConnectable(Direction face) { return face == Direction.DOWN; }
    @Override public boolean canInputFrom(Direction face) { return face == Direction.DOWN; }
    @Override public boolean canOutputTo(Direction face) { return false; }
    @Override public void setSuction(Aspect aspect, int amount) { this.currentSuction = aspect; }

    @Override @Nullable
    public Aspect getSuctionType(Direction face) { return currentSuction; }

    @Override
    public int getSuctionAmount(Direction face) { return currentSuction != null ? 128 : 0; }

    @Override public int takeEssentia(Aspect aspect, int amount, Direction face) { return 0; }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (aspect == currentSuction && canInputFrom(face)) {
            return amount - addToContainer(aspect, amount);
        }
        return 0;
    }

    @Override @Nullable
    public Aspect getEssentiaType(Direction face) {
        if (myAspects.isEmpty()) return null;
        return myAspects.aspects().isEmpty() ? null : myAspects.aspects().getFirst();
    }

    @Override public int getEssentiaAmount(Direction face) { return myAspects.visSize(); }
    @Override public int getMinimumSuction() { return 0; }

    /* ------------------------------------------------------ */
    /* Save / Load                                              */
    /* ------------------------------------------------------ */

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!myAspects.isEmpty()) myAspects.writeToNBT(tag);
        tag.putBoolean("stabilityAdded", stabilityHasBeenAdded);
        if (boundMatrixPos != null) {
            tag.putInt("mx", boundMatrixPos.getX());
            tag.putInt("my", boundMatrixPos.getY());
            tag.putInt("mz", boundMatrixPos.getZ());
        }
        if (boundPedestalPos != null) {
            tag.putInt("px", boundPedestalPos.getX());
            tag.putInt("py", boundPedestalPos.getY());
            tag.putInt("pz", boundPedestalPos.getZ());
        }
        if (ownerUuid != null) {
            tag.putUUID("owner", ownerUuid);
        }
        if (ownerName != null) {
            tag.putString("ownerName", ownerName);
        }
        if (cachedResearchData != null) {
            tag.put("research", cachedResearchData);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        myAspects = new AspectList();
        myAspects.readFromNBT(tag);
        stabilityHasBeenAdded = tag.getBoolean("stabilityAdded");
        if (tag.contains("mx")) {
            boundMatrixPos = new BlockPos(tag.getInt("mx"), tag.getInt("my"), tag.getInt("mz"));
        }
        if (tag.contains("px")) {
            boundPedestalPos = new BlockPos(tag.getInt("px"), tag.getInt("py"), tag.getInt("pz"));
        }
        if (tag.hasUUID("owner")) {
            ownerUuid = tag.getUUID("owner");
        }
        if (tag.contains("ownerName")) {
            ownerName = tag.getString("ownerName");
        }
        if (tag.contains("research")) {
            cachedResearchData = tag.getCompound("research");
        }
    }
}

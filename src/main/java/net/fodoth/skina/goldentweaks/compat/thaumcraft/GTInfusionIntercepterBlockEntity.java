package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.blockentities.InfusionMatrixBlockEntity;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class GTInfusionIntercepterBlockEntity extends BlockEntity
        implements IAspectContainer, IEssentiaTransport {

    private static final int STABILITY_AMOUNT = 20;
    private static final int MATRIX_OFFSET_Y = 3;

    // VarHandles for accessing final class private fields
    private static volatile VarHandle MATRIX_INSTABILITY;
    private static volatile VarHandle MATRIX_RECIPE_ESSENTIA;

    private AspectList myAspects = new AspectList();
    private Aspect currentSuction;
    private boolean stabilityHasBeenAdded;

    public GTInfusionIntercepterBlockEntity(BlockPos pos, BlockState state) {
        super(GTThaumcraftAdditionalBlockEntities.INFUSION_INTERCEPTER.get(), pos, state);
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
                        h = MethodHandles.privateLookupIn(
                                        InfusionMatrixBlockEntity.class,
                                        MethodHandles.lookup())
                                .findVarHandle(
                                        InfusionMatrixBlockEntity.class,
                                        "instability",
                                        int.class);
                        MATRIX_INSTABILITY = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error(
                                "Failed to find VarHandle for InfusionMatrixBlockEntity.instability", e);
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
                        h = MethodHandles.privateLookupIn(
                                        InfusionMatrixBlockEntity.class,
                                        MethodHandles.lookup())
                                .findVarHandle(
                                        InfusionMatrixBlockEntity.class,
                                        "recipeEssentia",
                                        AspectList.class);
                        MATRIX_RECIPE_ESSENTIA = h;
                    } catch (Exception e) {
                        GoldenTweaks.LOGGER.error(
                                "Failed to find VarHandle for InfusionMatrixBlockEntity.recipeEssentia", e);
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
        if (h != null) {
            h.set(matrix, value);
        }
    }

    private static AspectList getMatrixRecipeEssentia(InfusionMatrixBlockEntity matrix) {
        VarHandle h = matrixRecipeEssentiaHandle();
        return h != null ? (AspectList) h.get(matrix) : null;
    }

    /* ------------------------------------------------------ */
    /* Tick                                                    */
    /* ------------------------------------------------------ */

    public static void tick(Level level, BlockPos pos, BlockState state, GTInfusionIntercepterBlockEntity be) {
        if (level.isClientSide()) return;

        be.findMatrix();
        be.findAndFeedEssentia();
    }

    private void findMatrix() {
        if (level == null) return;

        BlockPos matrixPos = worldPosition.above(MATRIX_OFFSET_Y);
        BlockEntity be = level.getBlockEntity(matrixPos);

        if (be instanceof InfusionMatrixBlockEntity matrix) {
            if (!stabilityHasBeenAdded) {
                addStability(matrix);
                stabilityHasBeenAdded = true;
                setChanged();
            }
        } else {
            if (stabilityHasBeenAdded) {
                stabilityHasBeenAdded = false;
                setChanged();
            }
        }
    }

    private void findAndFeedEssentia() {
        if (level == null) return;

        // Get the infusion matrix above
        BlockPos matrixPos = worldPosition.above(MATRIX_OFFSET_Y);
        BlockEntity be = level.getBlockEntity(matrixPos);
        if (!(be instanceof InfusionMatrixBlockEntity matrix)) return;

        // Only operate while the matrix is crafting
        if (!matrix.crafting()) {
            if (!myAspects.isEmpty()) {
                myAspects = new AspectList();
                currentSuction = null;
                setChanged();
            }
            return;
        }

        // Get the essentia source below
        BlockPos sourcePos = worldPosition.below();
        BlockEntity sourceBe = level.getBlockEntity(sourcePos);
        if (!(sourceBe instanceof IEssentiaTransport source)) {
            pushToMatrix(matrix);
            return;
        }

        // Get what the matrix still needs (via reflection since it's a private final class field)
        AspectList recipeEssentia = getMatrixRecipeEssentia(matrix);
        if (recipeEssentia == null || recipeEssentia.isEmpty()) {
            pushToMatrix(matrix);
            return;
        }

        boolean changed = false;

        for (Aspect aspect : recipeEssentia.aspects()) {
            int needed = recipeEssentia.amount(aspect);
            if (needed <= 0) continue;

            currentSuction = aspect;

            int available = source.getEssentiaAmount(Direction.UP);
            Aspect sourceType = source.getEssentiaType(Direction.UP);

            if (sourceType != null && sourceType.equals(aspect) && available > 0) {
                int take = Math.min(needed, available);
                int taken = source.takeEssentia(aspect, take, Direction.UP);
                if (taken > 0) {
                    recipeEssentia.remove(aspect, taken);
                    changed = true;
                }
            }
        }

        if (!myAspects.isEmpty()) {
            changed |= pushHeldToMatrix(matrix);
        }

        if (changed) {
            setChanged();
        }
    }

    /**
     * Push essentia we've collected directly into the matrix's recipeEssentia,
     * mimicking the original Thaumic Insurgence intercepter behavior.
     */
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

        if (myAspects.isEmpty()) {
            currentSuction = null;
        }

        return changed;
    }

    private void pushToMatrix(InfusionMatrixBlockEntity matrix) {
        pushHeldToMatrix(matrix);
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

        BlockPos matrixPos = worldPosition.above(MATRIX_OFFSET_Y);
        BlockEntity be = level.getBlockEntity(matrixPos);
        if (be instanceof InfusionMatrixBlockEntity matrix) {
            int current = getMatrixInstability(matrix);
            setMatrixInstability(matrix, current + STABILITY_AMOUNT);
        }
        stabilityHasBeenAdded = false;
        setChanged();
    }

    /* ------------------------------------------------------ */
    /* IAspectContainer                                        */
    /* ------------------------------------------------------ */

    @Override
    public AspectList getAspects() {
        return myAspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.myAspects = aspects != null ? aspects : new AspectList();
        setChanged();
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount > 0) {
            myAspects.add(aspect, amount);
            setChanged();
            return 0;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        boolean had = doesContainerContainAmount(aspect, amount);
        if (had) {
            myAspects.remove(aspect, amount);
            setChanged();
        }
        return had;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return myAspects.amount(aspect) >= amount;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return myAspects.amount(aspect);
    }

    /* ------------------------------------------------------ */
    /* IEssentiaTransport                                      */
    /* ------------------------------------------------------ */

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
        this.currentSuction = aspect;
    }

    @Override
    @Nullable
    public Aspect getSuctionType(Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return currentSuction != null ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (aspect == currentSuction && canInputFrom(face)) {
            return amount - addToContainer(aspect, amount);
        }
        return 0;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(Direction face) {
        if (myAspects.isEmpty()) return null;
        return myAspects.aspects().isEmpty() ? null : myAspects.aspects().getFirst();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return myAspects.visSize();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    /* ------------------------------------------------------ */
    /* Save / Load                                              */
    /* ------------------------------------------------------ */

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!myAspects.isEmpty()) {
            myAspects.writeToNBT(tag);
        }
        tag.putBoolean("stabilityAdded", stabilityHasBeenAdded);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        myAspects = new AspectList();
        myAspects.readFromNBT(tag);
        stabilityHasBeenAdded = tag.getBoolean("stabilityAdded");
    }
}

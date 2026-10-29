package net.fodoth.skina.goldentweaks.compat.thermal;

import cofh.lib.api.fluid.IFluidStackHolder;
import cofh.lib.api.inventory.IItemStackHolder;
import cofh.lib.common.inventory.ItemStorageCoFH;
import cofh.thermal.core.util.managers.machine.CrystallizerRecipeManager;
import cofh.thermal.lib.util.recipes.IMachineInventory;
import cofh.thermal.lib.util.recipes.IThermalInventory;
import cofh.thermal.lib.util.recipes.internal.BaseMachineRecipe;
import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumAdditive;
import de.dafuqs.spectrum.recipe.crystallarieum.CrystallarieumRecipe;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 把一条光谱 {@code spectrum:crystallarieum_growing} 配方包装成 Thermal 结晶器可运行的机器配方。
 *
 * <p>输入种子物品、添加剂与培养液（各配方不同：液态水晶 / 水 / 熔岩 / 午夜溶液 等）：培养液只要求罐里存在，
 * 不消耗（墨水整条链路都跳过）；添加剂照原版
 * <b>每秒一次、按 {@code consume_chance_per_second} 概率吃掉 1 个</b>，并按 {@code growth_acceleration_mod}
 * 加速配方（原版是每秒推进 {@code 20 × 倍率} 个生长 ticks）。产出该配方最终生长阶段对应的结晶簇方块。</p>
 */
public class SpectrumCrystallizerRecipe extends BaseMachineRecipe {

    private static final int TICKS_PER_SECOND = 20;

    private final CrystallarieumRecipe source;

    private SpectrumCrystallizerRecipe(int energy, CrystallarieumRecipe source, ItemStack seedStack, FluidStack fluid, ItemStack result) {
        super(energy, 0.0F,
                List.of(seedStack),
                List.of(fluid),
                List.of(result),
                List.of(1.0F),
                List.of());
        this.source = source;
    }

    /**
     * 结晶器自身配方都匹配不上时，按「输入槽里的种子 + 输入罐里的培养液」去匹配一条光谱配方；匹配不到返回 null。
     * 若配方必须有添加剂而机器里没有，则先不开始（与原版 {@code canWork=false} 一致）。
     */
    public static SpectrumCrystallizerRecipe findFor(IThermalInventory inventory, CrystallizerRecipeManager manager) {

        if (!(inventory instanceof BlockEntity blockEntity)) {
            return null;
        }
        Level level = blockEntity.getLevel();
        if (level == null) {
            return null;
        }

        FluidStack tankFluid = FluidStack.EMPTY;
        for (IFluidStackHolder tank : inventory.inputTanks()) {
            if (!tank.getFluidStack().isEmpty()) {
                tankFluid = tank.getFluidStack();
                break;
            }
        }
        if (tankFluid.isEmpty()) {
            return null;
        }

        // 添加剂与种子各占一个输入槽，谁在哪个槽都行，所以逐个尝试
        for (IItemStackHolder slot : inventory.inputSlots()) {
            ItemStack seedStack = slot.getItemStack();
            if (seedStack.isEmpty()) {
                continue;
            }

            CrystallarieumRecipe recipe = SpectrumCrystallarieumRecipes.find(seedStack, tankFluid);
            if (recipe == null) {
                continue;
            }
            if (!recipe.growsWithoutAdditive() && findAdditiveSlot(inventory, recipe) == null) {
                continue;
            }

            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.isEmpty()) {
                continue;
            }

            // 原版每个生长阶段耗时 secondsPerGrowthStage 秒，全部阶段走完才算一条配方
            int stages = Math.max(1, recipe.getGrowthStages().size());
            int seconds = Math.max(1, recipe.getSecondsPerGrowthStage());
            int energy = stages * seconds * TICKS_PER_SECOND * Math.max(1, manager.getBasePower());

            return new SpectrumCrystallizerRecipe(energy, recipe, seedStack.copyWithCount(1), tankFluid.copyWithAmount(1), result);
        }
        return null;
    }

    /** 当前槽里的添加剂；没有则 null。 */
    public CrystallarieumAdditive additiveIn(IMachineInventory inventory) {
        IItemStackHolder slot = findAdditiveSlot(inventory, source);
        return slot == null ? null : additiveFor(source, slot.getItemStack());
    }

    /** 生长加速倍率：没有添加剂时为 1。 */
    public float growthAcceleration(IMachineInventory inventory) {
        CrystallarieumAdditive additive = additiveIn(inventory);
        return additive == null ? 1.0F : Math.max(0.01F, additive.growthAccelerationMod());
    }

    /** 原版每秒一次的消耗判定：命中就扣掉 1 个添加剂。 */
    public void consumeAdditive(IMachineInventory inventory, RandomSource random) {
        IItemStackHolder slot = findAdditiveSlot(inventory, source);
        if (slot == null) {
            return;
        }
        ItemStack stack = slot.getItemStack();
        CrystallarieumAdditive additive = additiveFor(source, stack);
        if (additive == null) {
            return;
        }
        // 添加剂与晶种是同一个物品时（如铋：spectrum:bismuth_flake）两者共用同一格，得给晶种留 1 个
        if (source.getIngredientStack().test(stack) && stack.getCount() <= 1) {
            return;
        }
        if (random.nextFloat() >= additive.consumeChancePerSecond()) {
            return;
        }
        if (slot instanceof ItemStorageCoFH storage) {
            storage.consume(1);
        }
    }

    /**
     * 候选添加剂槽：能匹配配方某条添加剂的格子。
     *
     * <p>优先非种子槽；都没有时退回种子槽——有些配方（如铋）晶种本身就是添加剂，
     * 结晶器两格又不允许放同一种物品，只能让同一叠物品兼作两者：晶种吃 1 个，其余按添加剂消耗。</p>
     */
    private static IItemStackHolder findAdditiveSlot(IThermalInventory inventory, CrystallarieumRecipe recipe) {
        IItemStackHolder seedSlot = null;
        for (IItemStackHolder slot : inventory.inputSlots()) {
            ItemStack stack = slot.getItemStack();
            if (stack.isEmpty() || additiveFor(recipe, stack) == null) {
                continue;
            }
            if (!recipe.getIngredientStack().test(stack)) {
                return slot;
            }
            if (seedSlot == null) {
                seedSlot = slot;
            }
        }
        return seedSlot;
    }

    private static CrystallarieumAdditive additiveFor(CrystallarieumRecipe recipe, ItemStack stack) {
        for (CrystallarieumAdditive additive : recipe.getAdditives()) {
            if (additive.ingredient().test(stack)) {
                return additive;
            }
        }
        return null;
    }

    /**
     * 只消耗一个种子：按槽位逐个判断是否命中种子，命中即消耗 1 个（添加剂由 {@code consumeAdditive} 按秒自行消耗）。
     */
    @Override
    public List<Integer> getInputItemCounts(IMachineInventory inventory) {

        Ingredient seed = source.getIngredientStack();
        List<? extends IItemStackHolder> slots = inventory.inputSlots();
        List<Integer> counts = new ArrayList<>(slots.size());
        boolean consumed = false;
        for (IItemStackHolder slot : slots) {
            ItemStack stack = slot.getItemStack();
            if (!consumed && !stack.isEmpty() && seed.test(stack)) {
                counts.add(1);
                consumed = true;
            } else {
                counts.add(0);
            }
        }
        return counts;
    }

    /** 培养液只要求存在，不让机器扣掉它。 */
    @Override
    public List<Integer> getInputFluidCounts(IMachineInventory inventory) {
        return Collections.emptyList();
    }
}

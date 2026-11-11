package net.fodoth.skina.goldentweaks.mixin.fix.mioicif;

import com.miophas.singularity_iteration.common.blockentity.producer.mio_icif_electrolyzer_elc;
import com.miophas.singularity_iteration.common.recipe.generic.ElectrolyzerRecipe;
import com.miophas.singularity_iteration.common.recipe.generic.ElectrolyzerRecipes;
import com.miophas.singularity_iteration.common.recipe.mio_icif_SingleItemRecipeInput;
import com.miophas.singularity_iteration.core.prefab.inventory.MachineItemHandler;
import com.miophas.singularity_iteration.core.runtime.processing.RecipeSlots;
import net.fodoth.skina.goldentweaks.compat.mioicif.GTElectrolyzerInputCount;
import net.fodoth.skina.goldentweaks.mixin.fix.mioicif.accessor.MachineItemHandlerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * 电解机（{@code mio_icif:electrolyzer}）的三点数据包/行为修正：
 *
 * <ol>
 *   <li><b>输入数量</b>：机器固定每次只从输入槽扣 1 个（{@code RecipeSlots.prepare(..., 1, ...)}），
 *       数据包无法表达「4 个 → 1 个」这类比例。这里拦截该扣料调用，按配方 {@code group} 里的
 *       {@code input_count=N} 覆盖消耗数量（见 {@link GTElectrolyzerInputCount}）。</li>
 *   <li><b>不产能量</b>：带 {@code no_energy} 标记的配方在 {@code doWork} 结算时把这次写入的
 *       化学能回滚，机器只消耗 EU、不储存/输出能量。</li>
 *   <li><b>不因缓存卡死</b>：化学能缓存上限只有 400，原版 {@code canWork} 在缓存填满后会直接
 *       停机；这里放开该限制并在写入后把超出上限的部分丢弃，机器在没有相邻储电方块时也能持续工作。
 *       带有 {@code no_energy} 的配方本身不产能量，不受此限。</li>
 * </ol>
 *
 * <p>注意：{@code RecipeSlots.prepare} 与 {@code Math.max} 都是静态方法，Mixin 要求重定向
 * handler 必须是静态的，因此无法直接访问实例；配方查找通过当前服务端的 {@code RecipeManager}
 * 完成（合成表是全服共享的），不依赖具体维度。</p>
 */
@Mixin(value = mio_icif_electrolyzer_elc.class, remap = false)
public abstract class MioIcifElectrolyzerInputCountMixin {

    @Shadow
    protected long chemicalEnergy;

    @Unique
    private long gt$chemicalBefore;

    @Unique
    private boolean gt$noEnergy;

    @Redirect(
            method = "prepareCell",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/miophas/singularity_iteration/core/runtime/processing/RecipeSlots;prepare(Lcom/miophas/singularity_iteration/core/prefab/inventory/MachineItemHandler;II[ILjava/util/List;)Ljava/util/Optional;"
            )
    )
    private static Optional<RecipeSlots.Prepared> gt$consumeConfiguredInputCount(
            MachineItemHandler handler, int inputSlot, int consume, int[] outputSlots, List<ItemStack> results) {
        int adjusted = GTElectrolyzerInputCount.parse(gt$groupOf(handler, inputSlot));
        return RecipeSlots.prepare(handler, inputSlot, adjusted, outputSlots, results);
    }

    @Redirect(
            method = "canWork",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(JJ)J", ordinal = 0)
    )
    private static long gt$ignoreChemicalBufferLimit(long a, long b) {
        return Long.MAX_VALUE;
    }

    @Inject(method = "doWork", at = @At("HEAD"))
    private void gt$beforeWork(CallbackInfo ci) {
        this.gt$chemicalBefore = this.chemicalEnergy;
        this.gt$noEnergy = GTElectrolyzerInputCount.noEnergy(gt$groupOf(
                ((MachineItemHandlerAccessor) (Object) this).gt$getItemHandler(),
                mio_icif_electrolyzer_elc.INPUT_SLOT));
    }

    @Inject(method = "doWork", at = @At("RETURN"))
    private void gt$applyEnergyPolicy(CallbackInfo ci) {
        if (this.gt$noEnergy) {
            this.chemicalEnergy = this.gt$chemicalBefore;
        } else {
            long max = ((mio_icif_electrolyzer_elc) (Object) this).getMaxChemicalEnergy();
            if (this.chemicalEnergy > max) {
                this.chemicalEnergy = max;
            }
        }
    }

    @Unique
    private static String gt$groupOf(IItemHandler handler, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        ItemStack input = handler.getStackInSlot(slot);
        if (input.isEmpty()) {
            return null;
        }
        Optional<RecipeHolder<ElectrolyzerRecipe>> recipe = server.getRecipeManager().getRecipeFor(
                ElectrolyzerRecipes.ELECTROLYZER_TYPE.get(),
                new mio_icif_SingleItemRecipeInput(input),
                server.overworld());
        return recipe.map(holder -> holder.value().getGroup()).orElse(null);
    }
}

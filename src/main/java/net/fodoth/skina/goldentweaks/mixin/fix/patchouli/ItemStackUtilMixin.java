package net.fodoth.skina.goldentweaks.mixin.fix.patchouli;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.common.util.ItemStackUtil;

/**
 * Patchouli 在收到进度同步包（{@code ClientAdvancements.onClientPacket} → {@code ClientBookRegistry.reload}）
 * 时会立刻重编所有书。此时笔记本书页若在 build 阶段就解析物品（如 Thermal 的
 * {@code thermal:catalyst_page}），传入的 {@link HolderLookup.Provider} 还没有 {@code minecraft:item}
 * 注册表，{@code ItemParser} 会抛 {@code IllegalStateException: Registry minecraft:item not found}，
 * 导致整本书加载失败。
 * <p>
 * 这里在注册表缺失时直接返回空气，避免异常；等下次带完整注册表的重载再正常解析。
 */
@Mixin(value = ItemStackUtil.class, remap = false)
public abstract class ItemStackUtilMixin {

    @Inject(method = "deserializeStack", at = @At("HEAD"), cancellable = true)
    private static void gt$guardMissingItemRegistry(String stack, HolderLookup.Provider registries,
                                                    CallbackInfoReturnable<Triple<Holder<Item>, DataComponentPatch, Integer>> cir) {
        if (registries == null || registries.lookup(Registries.ITEM).isEmpty()) {
            cir.setReturnValue(Triple.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), DataComponentPatch.EMPTY, 1));
        }
    }
}

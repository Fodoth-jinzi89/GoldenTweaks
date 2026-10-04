package net.fodoth.skina.goldentweaks.mixin.fix.fidworkblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 「烟火（风味人间日常）」内嵌的子模组 <b>fidworkblock 1.0.0</b> 砧板（choppingboard）旧存档崩溃修复。
 *
 * <p>砧板物品栏现在是 3 格（{@code SLOT_INGREDIENT = 0} / {@code SLOT_SKIN = 1} /
 * {@code SLOT_FILLING = 2}），但 {@link ItemStackHandler#deserializeNBT} 会照存档里的
 * {@code "Size"} 重建容量；旧版本存档记录的是 1，载入后物品栏便只剩 1 格。</p>
 *
 * <p>此后 {@code getSkinItem()}（第 1 格）、{@code getFillingItem()}（第 2 格）、{@code isEmpty()}、
 * {@code removeSkin()}、{@code canWrap()} 等全部越界，客户端渲染砧板时直接抛
 * {@code Slot 1 not in valid range - [0,1)} 崩溃。</p>
 *
 * <p>这里在 {@code loadAdditional} 结束后把不足 3 格的物品栏补足，原有物品（第 0 格材料）原样保留。
 * 服务端从区块存档载入与客户端接收方块实体数据包都会走 {@code loadAdditional}，两侧一并修好。</p>
 */
@Mixin(targets = "com.fidtest.block.entity.ChoppingboardBlockEntity", remap = false)
public abstract class ChoppingboardBlockEntityMixin {

    /** 与 {@code ChoppingboardBlockEntity.SLOT_FILLING + 1} 对应：物品栏至少需要 3 格。 */
    @Unique
    private static final int GT_CHOPPINGBOARD_SLOTS = 3;

    @Shadow @Final private ItemStackHandler inventory;

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void gt$growLegacyInventory(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci) {

        int slots = this.inventory.getSlots();
        if (slots >= GT_CHOPPINGBOARD_SLOTS) {
            return;
        }

        ItemStack[] saved = new ItemStack[slots];
        for (int i = 0; i < slots; i++) {
            saved[i] = this.inventory.getStackInSlot(i);
        }

        this.inventory.setSize(GT_CHOPPINGBOARD_SLOTS);
        for (int i = 0; i < slots; i++) {
            this.inventory.setStackInSlot(i, saved[i]);
        }
    }
}

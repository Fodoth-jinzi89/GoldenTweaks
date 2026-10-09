package net.fodoth.skina.goldentweaks.mixin.fix.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.item.AbstractStoreMaidItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 女仆照片/智能板放置崩溃修复。
 * <p>
 * 从备份复原的女仆物品，其 MAID_INFO 里的 {@code Owner} 字段可能缺失或损坏
 * （典型是女仆本体因 NBT 数据错误消失后再复原）。
 * 此时 {@code spawnFromStore} 里的 {@code CompoundTag#getUUID("Owner")} 会拿到 null，
 * 进而在 {@code NbtUtils.loadUUID} 抛 NPE 崩溃。
 * <p>
 * 这里在进入原逻辑前补全 Owner：谁放置就归属谁，让残损的女仆物品仍能被正常复原。
 */
@Mixin(AbstractStoreMaidItem.class)
public class AbstractStoreMaidItemMixin {

    @Inject(method = "spawnFromStore", at = @At("HEAD"))
    private void goldenTweaks$repairMissingOwner(UseOnContext context, Player player, Level worldIn,
                                                 EntityMaid maid, Runnable runnable,
                                                 CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();
        CustomData data = stack.get(InitDataComponent.MAID_INFO.get());
        if (data == null) {
            return;
        }

        CompoundTag tag = data.copyTag();
        if (gt$hasValidOwner(tag)) {
            return;
        }

        tag.putUUID("Owner", player.getUUID());
        stack.set(InitDataComponent.MAID_INFO.get(), CustomData.of(tag));
    }

    /**
     * 与 {@code NbtUtils.loadUUID} 的判定保持一致：必须是长度为 4 的 int 数组，
     * 否则 {@code getUUID} 仍会抛异常。
     */
    @Unique
    private static boolean gt$hasValidOwner(CompoundTag tag) {
        Tag owner = tag.get("Owner");
        return owner instanceof IntArrayTag array && array.getAsIntArray().length == 4;
    }
}

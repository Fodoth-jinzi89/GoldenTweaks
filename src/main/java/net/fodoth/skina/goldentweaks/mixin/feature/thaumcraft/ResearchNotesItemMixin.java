package net.fodoth.skina.goldentweaks.mixin.feature.thaumcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.blockentities.ResearchTableBlockEntity;
import thaumcraft.common.items.ResearchNotesItem;

@Mixin(ResearchNotesItem.class)
public abstract class ResearchNotesItemMixin {
    @Unique
    private static final int gt$NEARBY_RESEARCH_TABLE = Integer.MAX_VALUE;
    @Unique
    private static final ThreadLocal<ResearchTableBlockEntity> gt$SELECTED_RESEARCH_TABLE = new ThreadLocal<>();

    @Inject(method = "requestFor", at = @At("HEAD"), cancellable = true)
    private static void gt$ignoreFakePlayers(Player player, String research, CallbackInfoReturnable<Boolean> cir) {
        gt$SELECTED_RESEARCH_TABLE.remove();
        if (player instanceof FakePlayer) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
            method = "requestFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lthaumcraft/common/items/ResearchNotesItem;findUsable(Lnet/minecraft/world/entity/player/Player;Z)I",
                    ordinal = 1
            )
    )
    private static int gt$findNearbyResearchTableInk(Player player, boolean tool, Operation<Integer> original) {
        int slot = original.call(player, tool);
        if (slot >= 0) {
            return slot;
        }
        ResearchTableBlockEntity table = gt$findResearchTable(player);
        if (table == null) {
            return slot;
        }
        gt$SELECTED_RESEARCH_TABLE.set(table);
        return gt$NEARBY_RESEARCH_TABLE;
    }

    @WrapOperation(
            method = "requestFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 1
            )
    )
    private static ItemStack gt$useNearbyResearchTableInk(Inventory inventory, int slot, Operation<ItemStack> original) {
        if (slot != gt$NEARBY_RESEARCH_TABLE) {
            return original.call(inventory, slot);
        }
        ResearchTableBlockEntity table = gt$SELECTED_RESEARCH_TABLE.get();
        return table == null ? ItemStack.EMPTY : table.getItem(ResearchTableBlockEntity.TOOLS_SLOT).copy();
    }

    @Inject(
            method = "requestFor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static void gt$consumeNearbyResearchTableInk(Player player, String research, CallbackInfoReturnable<Boolean> cir) {
        ResearchTableBlockEntity table = gt$SELECTED_RESEARCH_TABLE.get();
        if (table != null && !player.getAbilities().instabuild) {
            ItemStack stack = table.getItem(ResearchTableBlockEntity.TOOLS_SLOT);
            stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + 1));
            table.setItem(ResearchTableBlockEntity.TOOLS_SLOT, stack);
        }
        gt$SELECTED_RESEARCH_TABLE.remove();
    }

    @Unique
    private static ResearchTableBlockEntity gt$findResearchTable(Player player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            if (player.level().getBlockEntity(pos) instanceof ResearchTableBlockEntity table
                    && table.hasUsableInk()) {
                return table;
            }
        }
        return null;
    }
}

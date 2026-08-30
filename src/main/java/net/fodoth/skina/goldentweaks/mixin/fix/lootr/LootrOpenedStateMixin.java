package net.fodoth.skina.goldentweaks.mixin.fix.lootr;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity;
import net.fodoth.skina.goldentweaks.compat.lootr.LootrOpenedStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin({LootrChestBlockEntity.class, LootrBarrelBlockEntity.class, LootrShulkerBlockEntity.class})
public abstract class LootrOpenedStateMixin implements LootrOpenedStateAccess {
    @Unique private static final String GT_OPENED = "GoldenTweaksHasBeenOpened";
    @Unique private boolean gt$opened;
    @Unique private final Set<UUID> gt$openedPlayers = new HashSet<>();

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void gt$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        gt$opened = tag.contains(GT_OPENED) ? tag.getBoolean(GT_OPENED)
                : tag.getBoolean("LootrHasBeenOpened");
        gt$openedPlayers.clear();
        ListTag players = tag.getList("GoldenTweaksOpenedPlayers", Tag.TAG_STRING);
        for (int i = 0; i < players.size(); i++) {
            try { gt$openedPlayers.add(UUID.fromString(players.getString(i))); }
            catch (IllegalArgumentException ignored) { }
        }
        ((noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity) this).setHasBeenOpened(gt$opened);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void gt$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean(GT_OPENED, gt$opened);
        ListTag players = new ListTag();
        for (UUID player : gt$openedPlayers) players.add(net.minecraft.nbt.StringTag.valueOf(player.toString()));
        tag.put("GoldenTweaksOpenedPlayers", players);
        ((noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity) this).setHasBeenOpened(gt$opened);
    }

    @Inject(method = "defaultTick", at = @At("TAIL"), require = 0)
    private void gt$forceState(net.minecraft.world.level.Level level,
                               net.minecraft.core.BlockPos pos,
                               net.minecraft.world.level.block.state.BlockState state,
                               CallbackInfo ci) {
        ((noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity) this).setHasBeenOpened(gt$opened);
    }

    @Override
    public boolean gt$isOpened() { return gt$opened; }

    @Override
    public void gt$setOpened(boolean opened) {
        gt$opened = opened;
        ((noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity) this).setHasBeenOpened(opened);
    }

    @Override
    public boolean gt$hasOpened(UUID player) { return gt$openedPlayers.contains(player); }

    @Override
    public void gt$markOpened(UUID player) {
        gt$openedPlayers.add(player);
        gt$opened = true;
        ((noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity) this).setHasBeenOpened(true);
    }
}

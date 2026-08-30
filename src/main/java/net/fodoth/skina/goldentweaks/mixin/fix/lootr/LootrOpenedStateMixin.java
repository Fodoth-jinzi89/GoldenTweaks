package net.fodoth.skina.goldentweaks.mixin.fix.lootr;

import net.fodoth.skina.goldentweaks.compat.lootr.LootrOpenedStateAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrBarrelBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.common.block.entity.LootrShulkerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 为 Lootr 的三种容器方块实体（箱子、桶、潜影盒）注入额外的“已开启”状态。
 * <p>
 * Lootr 自带的 hasBeenOpened 会在某些情况下被重置，这里额外保存一份 GoldenTweaks 自己的
 * 状态（{@code GoldenTweaksHasBeenOpened}）以及已开启玩家的 UUID 列表，
 * 并在 NBT 读写时与 Lootr 的状态互相同步。已开启状态用于快速拾取后容器不重复刷新的判定。
 */
@Mixin({LootrChestBlockEntity.class, LootrBarrelBlockEntity.class, LootrShulkerBlockEntity.class})
public abstract class LootrOpenedStateMixin implements LootrOpenedStateAccess {

    /** 自定义“已开启”标记的 NBT 键名。 */
    @Unique private static final String GT_OPENED = "GoldenTweaksHasBeenOpened";
    /** 已开启玩家 UUID 列表的 NBT 键名。 */
    @Unique private static final String GT_OPENED_PLAYERS = "GoldenTweaksOpenedPlayers";
    /** Lootr 自带的旧版标记，读取时用于兼容旧存档。 */
    @Unique private static final String LOOTR_OPENED = "LootrHasBeenOpened";

    @Unique private boolean gt$opened;
    @Unique private final Set<UUID> gt$openedPlayers = new HashSet<>();

    /** 读取 NBT：优先读自定义标记，其次兼容 Lootr 旧标记。 */
    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void gt$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        gt$opened = tag.contains(GT_OPENED) ? tag.getBoolean(GT_OPENED) : tag.getBoolean(LOOTR_OPENED);
        gt$openedPlayers.clear();
        ListTag players = tag.getList(GT_OPENED_PLAYERS, Tag.TAG_STRING);
        for (int i = 0; i < players.size(); i++) {
            try {
                gt$openedPlayers.add(UUID.fromString(players.getString(i)));
            } catch (IllegalArgumentException ignored) {
                // 忽略损坏的 UUID 条目，避免单个坏数据导致整个容器无法加载
            }
        }
        ((ILootrBlockEntity) this).setHasBeenOpened(gt$opened);
    }

    /** 保存 NBT：写出自定义标记和已开启玩家列表，并同步回 Lootr。 */
    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void gt$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean(GT_OPENED, gt$opened);
        ListTag players = new ListTag();
        for (UUID player : gt$openedPlayers) {
            players.add(StringTag.valueOf(player.toString()));
        }
        tag.put(GT_OPENED_PLAYERS, players);
        ((ILootrBlockEntity) this).setHasBeenOpened(gt$opened);
    }

    @Override
    public boolean gt$isOpened() {
        return gt$opened;
    }

    @Override
    public void gt$setOpened(boolean opened) {
        gt$opened = opened;
        ((ILootrBlockEntity) this).setHasBeenOpened(opened);
    }

    @Override
    public boolean gt$hasOpened(UUID player) {
        return gt$openedPlayers.contains(player);
    }

    @Override
    public void gt$markOpened(UUID player) {
        gt$openedPlayers.add(player);
        gt$opened = true;
        ((ILootrBlockEntity) this).setHasBeenOpened(true);
    }
}

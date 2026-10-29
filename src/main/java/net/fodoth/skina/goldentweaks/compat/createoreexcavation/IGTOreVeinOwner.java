package net.fodoth.skina.goldentweaks.compat.createoreexcavation;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Implemented (via mixin) on Create Ore Excavation's {@code ExcavatingBlockEntity}，用来记录「这台矿机是谁放的」。
 *
 * <p>Create Ore Excavation 本身不记录矿机的所有者，所以矿脉分享加成无法判断同一个矿脉上有没有不同玩家。
 * 这里在方块实体上补记一个所有者 UUID（写入 NBT 并随方块实体同步下发），放置/首次交互时补齐，
 * 见 {@code mixin.balance.createoreexcavation} 下的两个 mixin。</p>
 */
public interface IGTOreVeinOwner {

    @Nullable
    UUID gt$getVeinOwner();

    void gt$setVeinOwner(@Nullable UUID owner);
}

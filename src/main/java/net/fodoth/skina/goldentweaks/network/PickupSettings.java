package net.fodoth.skina.goldentweaks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * 玩家在客户端配置的右键拾取设定，由客户端同步给服务端。
 * <p>
 * 服务端只信任 {@link #clamped()} 裁剪后的值：每个字段都被限制在合理范围内，
 * 客户端即使发送越界数值，也只是被压回服务端允许的上限（拾取距离、延迟阈值、Lootr 组数等）。
 * 因此各玩家可以各自配置，服务端仍能防止滥用。
 */
public record PickupSettings(
        boolean enablePickup,
        boolean allowStandingPickup,
        boolean allowSneakPickup,
        boolean allowFullInventoryPickup,
        double maxReach,
        boolean allowInfiniteDelay,
        int pickupDelayThreshold,
        boolean allowExperienceOrbPickup,
        boolean lootrQuickLoot,
        boolean lootrPlayOpenAnimation,
        boolean lootrShowFlyingItems,
        int lootrPickupGroups,
        int lootrHoldPickupInterval
) {

    /** 拾取距离硬上限：等于模组配置允许的最大值（基础 128 + 扩展 128）。 */
    public static final double MAX_REACH_LIMIT = 256.0D;

    /** 拾取延迟阈值上限，与 vanilla 的「无限延迟」相隔一档。 */
    public static final int PICKUP_DELAY_LIMIT = 32766;

    /** Lootr 单次拾取组数上限。 */
    public static final int LOOTR_GROUPS_LIMIT = 64;

    /** Lootr 长按拾取间隔上限（tick）。 */
    public static final int LOOTR_INTERVAL_LIMIT = 1200;

    /** 尚未同步设定的玩家使用的保守默认值（整个右键拾取功能关闭）。 */
    public static final PickupSettings DEFAULT = new PickupSettings(
            false, true, false, false, 5.0D, false, PICKUP_DELAY_LIMIT, true, false, true, true, 0, 5);

    public static final StreamCodec<FriendlyByteBuf, PickupSettings> STREAM_CODEC = StreamCodec.of(
            PickupSettings::encode,
            PickupSettings::decode
    );

    private static void encode(FriendlyByteBuf buf, PickupSettings s) {
        buf.writeBoolean(s.enablePickup);
        buf.writeBoolean(s.allowStandingPickup);
        buf.writeBoolean(s.allowSneakPickup);
        buf.writeBoolean(s.allowFullInventoryPickup);
        buf.writeDouble(s.maxReach);
        buf.writeBoolean(s.allowInfiniteDelay);
        buf.writeVarInt(s.pickupDelayThreshold);
        buf.writeBoolean(s.allowExperienceOrbPickup);
        buf.writeBoolean(s.lootrQuickLoot);
        buf.writeBoolean(s.lootrPlayOpenAnimation);
        buf.writeBoolean(s.lootrShowFlyingItems);
        buf.writeVarInt(s.lootrPickupGroups);
        buf.writeVarInt(s.lootrHoldPickupInterval);
    }

    private static PickupSettings decode(FriendlyByteBuf buf) {
        return new PickupSettings(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    /** 把各字段裁剪到服务端允许的合理范围。 */
    public PickupSettings clamped() {
        return new PickupSettings(
                enablePickup,
                allowStandingPickup,
                allowSneakPickup,
                allowFullInventoryPickup,
                Mth.clamp(maxReach, 1.0D, MAX_REACH_LIMIT),
                allowInfiniteDelay,
                Mth.clamp(pickupDelayThreshold, 0, PICKUP_DELAY_LIMIT),
                allowExperienceOrbPickup,
                lootrQuickLoot,
                lootrPlayOpenAnimation,
                lootrShowFlyingItems,
                Mth.clamp(lootrPickupGroups, 0, LOOTR_GROUPS_LIMIT),
                Mth.clamp(lootrHoldPickupInterval, 1, LOOTR_INTERVAL_LIMIT)
        );
    }
}

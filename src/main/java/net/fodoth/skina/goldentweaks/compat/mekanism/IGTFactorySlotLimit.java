package net.fodoth.skina.goldentweaks.compat.mekanism;

/**
 * Implemented (via mixin) on Mekanism's {@code BasicInventorySlot} so工厂槽位可以拥有超过物品默认堆叠上限的容量。
 * mekanism_extras 的工厂槽位自带 {@code getLimit() * (8 << 等级)} 的放大逻辑，而 evolvedmekanism
 * 直接复用原版槽位、上限被限制在 64，导致高级工厂切 8 个西瓜（72 片切片）时输出槽放不下。
 */
public interface IGTFactorySlotLimit {

    int goldentweaks$getStackLimitMultiplier();

    void goldentweaks$setStackLimitMultiplier(int multiplier);
}

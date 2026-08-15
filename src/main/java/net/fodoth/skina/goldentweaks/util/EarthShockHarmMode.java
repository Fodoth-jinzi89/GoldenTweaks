package net.fodoth.skina.goldentweaks.util;

/**
 * 震荡波（Earth Shock）可伤害实体的模式（移植自 TC4Tweaks 的 EarthShockHarmMode）。
 */
public enum EarthShockHarmMode {

    /** 只伤害生物（牛、玩家、僵尸等），最符合直觉。 */
    OnlyLiving,

    /** 除掉落物和经验球外全部伤害（含物品展示框等）。 */
    ExceptItemXp,

    /** 全部伤害，同移植版原样行为。 */
    AllEntity
}

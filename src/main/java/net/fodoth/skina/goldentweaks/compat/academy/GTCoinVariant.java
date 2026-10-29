package net.fodoth.skina.goldentweaks.compat.academy;

/**
 * 让学院（AcademyCraft）抛出的硬币实体携带「来源硬币」的物品 id，用于：
 *
 * <ul>
 *     <li>客户端按该硬币自身的贴图/颜色渲染飞行中的硬币；</li>
 *     <li>服务端按该硬币的倍率缩放超电磁炮的伤害、破坏与光束；</li>
 *     <li>硬币落回时退回原本抛出的那枚硬币。</li>
 * </ul>
 *
 * <p>空串表示学院原本的 {@code academy:coin}，保持原版行为。</p>
 */
public interface GTCoinVariant {

    String gt$getCoinVariant();

    void gt$setCoinVariant(String variant);
}

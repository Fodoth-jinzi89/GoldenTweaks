package net.fodoth.skina.goldentweaks.compat.academy;

/**
 * 让学院（AcademyCraft）的超电磁炮光束实体携带「粗细倍率」，供客户端渲染时按倍率缩放光束直径。
 * 默认 1.0 表示原版粗细。
 */
public interface GTBeamScale {

    float gt$getBeamScale();

    void gt$setBeamScale(float scale);
}

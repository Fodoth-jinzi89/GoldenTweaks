package net.fodoth.skina.goldentweaks.compat.ftbquests.client;

/**
 * 章节图片上「绑定配方 id」的两个字段。字段本体与读写放在 {@code ChapterImageRecipeBindingMixin}
 * （common，保证专用服务器也能存盘存档），配置界面放在 {@code ChapterImageRecipeBindingConfigMixin}
 * （client，因为 {@code ChapterImage#fillConfigGroup} 本身带 {@code @OnlyIn(CLIENT)}）。
 *
 * <p>两个字段填的都是 EMI 的配方 id，形如 {@code namespace:path}（例如 {@code minecraft:acacia_boat}）。
 * 填了之后按 R/U 会优先跳转这个配方；没填或找不到时退回原来的「按物品列出配方」。</p>
 */
public interface GTQuestImageRecipeBinding {

    /** 按 R 时直接跳转的配方 id，空串表示未绑定。 */
    String goldentweaks$imageRecipeId();

    /** 按 U 时直接跳转的配方 id，空串表示未绑定。 */
    String goldentweaks$imageUsesId();

    void goldentweaks$setImageRecipeId(String recipeId);

    void goldentweaks$setImageUsesId(String usesId);
}

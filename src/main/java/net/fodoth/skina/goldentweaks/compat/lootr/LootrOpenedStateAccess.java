package net.fodoth.skina.goldentweaks.compat.lootr;

import java.util.UUID;

/**
 * Lootr 容器的“已开启”状态访问接口。
 * <p>
 * 由 {@code LootrOpenedStateMixin} 注入到 Lootr 的主要容器方块实体（箱子、桶、潜影盒）上，
 * 用于在 Lootr 自带的 hasBeenOpened 之外额外记录：
 * <ul>
 *   <li>该容器是否被 GoldenTweaks 标记为已开启；</li>
 *   <li>哪些玩家已经开启过该容器（用于快速拾取后的状态持久化）。</li>
 * </ul>
 */
public interface LootrOpenedStateAccess {

    /** @return 该容器是否已被 GoldenTweaks 标记为已开启 */
    boolean gt$isOpened();

    /** 直接设置已开启状态，并同步到 Lootr 的 hasBeenOpened 标志。 */
    void gt$setOpened(boolean opened);

    /** @return 指定玩家是否已经开启过该容器 */
    boolean gt$hasOpened(UUID player);

    /** 记录指定玩家已开启，并将容器标记为已开启。 */
    void gt$markOpened(UUID player);
}

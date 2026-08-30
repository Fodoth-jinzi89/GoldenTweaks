package net.fodoth.skina.goldentweaks.compat.lootr;

import java.util.UUID;

public interface LootrOpenedStateAccess {
    boolean gt$isOpened();
    void gt$setOpened(boolean opened);
    boolean gt$hasOpened(UUID player);
    void gt$markOpened(UUID player);
}

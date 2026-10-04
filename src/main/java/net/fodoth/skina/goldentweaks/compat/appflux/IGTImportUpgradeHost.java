package net.fodoth.skina.goldentweaks.compat.appflux;

import appeng.api.upgrades.IUpgradeInventory;

/**
 * Implemented on {@code PatternProviderLogic} by GoldenTweaks so the pattern-provider menu can add a slot for the
 * result-import card. Replaces ae2helpers' own host interface, which we no longer rely on.
 */
public interface IGTImportUpgradeHost {

    IUpgradeInventory gt$getImportUpgrades();
}

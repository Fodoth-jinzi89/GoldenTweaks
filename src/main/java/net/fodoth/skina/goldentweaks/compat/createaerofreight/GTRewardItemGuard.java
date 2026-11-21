package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightRewardsConfig;
import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import cn.blockforge.generated.createareofreight.compat.migration.NamespaceMigration;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class GTRewardItemGuard {

    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private GTRewardItemGuard() {
    }

    public static @NotNull List<String> filter(@NotNull List<String> rows, boolean special) {
        return filter(rows, special, id -> BuiltInRegistries.ITEM.containsKey(id)
                && !id.equals(ResourceLocation.withDefaultNamespace("air")));
    }

    static @NotNull List<String> filter(@NotNull List<String> rows, boolean special,
                                      @NotNull Predicate<ResourceLocation> available) {
        return rows.stream().filter(raw -> {
            RewardEntry entry;
            if (special) {
                var parsed = FreightRewardsConfig.parseSpecialEntry(raw);
                entry = parsed == null ? null : parsed.getValue();
            } else {
                entry = FreightRewardsConfig.parseGeneralEntry(raw);
            }
            // Keep malformed rows visible for upstream format validation instead of silently deleting them.
            if (entry == null) {
                return true;
            }
            ResourceLocation id = NamespaceMigration.canonical(ResourceLocation.parse(entry.itemId()));
            if (available.test(id)) {
                return true;
            }
            if (REPORTED.add(raw)) {
                GoldenTweaks.LOGGER.warn("[AeroFreight] Skipping nonexistent reward item {} in editor; entry: {}", id, raw);
            }
            return false;
        }).toList();
    }
}

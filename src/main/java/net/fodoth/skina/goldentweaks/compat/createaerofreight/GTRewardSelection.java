package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import cn.blockforge.generated.createareofreight.application.order.WeightedSelector;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GTRewardSelection {
    private GTRewardSelection() { }

    // Rounded, clipped N(6, 3²); clipping retains a median of six.
    public static int specialtyCount(long seed) {
        return (int)Math.clamp(Math.round(6 + new Random(seed ^ 0x4754524557415244L).nextGaussian() * 3), 1, 18);
    }

    public static List<RewardEntry> choose(List<RewardEntry> entries, long seed) {
        int budget = 18;
        List<RewardEntry> selected = new ArrayList<>(entries.stream().filter(RewardEntry::guaranteed).limit(budget).toList());
        List<RewardEntry> pool = new ArrayList<>(entries.stream().filter(entry -> !entry.guaranteed()).toList());
        Random random = new Random(seed);
        int count = Math.min(budget, Math.max(selected.size(), specialtyCount(seed)));
        while (selected.size() < count && !pool.isEmpty()) {
            RewardEntry entry = WeightedSelector.select(pool, RewardEntry::chanceWeight, random::nextDouble);
            if (entry == null) break;
            selected.add(entry);
            pool.remove(entry);
        }
        return List.copyOf(selected);
    }

    public static <T> List<T> specialtiesFirst(List<T> rewards, int generalCount) {
        int general = Math.min(generalCount, rewards.size());
        List<T> result = new ArrayList<>(rewards.subList(general, Math.min(rewards.size(), general + 18)));
        result.addAll(rewards.subList(0, general));
        return List.copyOf(result);
    }

    public static <T> List<T> page(List<T> entries, int capacity, long milliseconds) {
        int visible = Math.clamp(capacity, 0, 6);
        if (visible == 0 || entries.isEmpty()) return List.of();
        int pages = (entries.size() + visible - 1) / visible;
        int start = (int)Math.floorMod(milliseconds / 2000, pages) * visible;
        return entries.subList(start, Math.min(start + visible, entries.size()));
    }
}

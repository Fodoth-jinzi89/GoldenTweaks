package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import cn.blockforge.generated.createareofreight.FreightRewardsConfig;
import cn.blockforge.generated.createareofreight.RewardPoolWeightsConfig;
import cn.blockforge.generated.createareofreight.ModifierOrderConfig;
import cn.blockforge.generated.createareofreight.SpecialFreightOrdersConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import cn.blockforge.generated.createareofreight.api.reward.RewardEntry;
import cn.blockforge.generated.createareofreight.infrastructure.config.RewardPoolFileConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GTBountifulRewardPools {

    private GTBountifulRewardPools() {
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!GoldenTweaksCommonConfig.GENERATE_AEROFREIGHT_LIST.get()) {
            return;
        }
        ResourceManager resources = event.getServer().getResourceManager();
        Set<String> poolNames = new TreeSet<>();
        for (Map.Entry<ResourceLocation, Resource> decree : resources.listResources(
                "bounty_decrees/goldentweaks", id -> id.getNamespace().equals("bountiful")
                        && id.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = decree.getValue().openAsReader()) {
                JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
                if (data.has("rewards")) {
                    for (JsonElement reward : data.getAsJsonArray("rewards")) {
                        String name = reward.getAsString();
                        if (name.matches("[a-z0-9_]+")) {
                            poolNames.add(name);
                        }
                    }
                }
            } catch (IOException | RuntimeException exception) {
                GoldenTweaks.LOGGER.error("Cannot read Bountiful decree {}", decree.getKey(), exception);
            }
        }

        // Initialize upstream defaults before adding files: its initializer stops at the first TOML.
        RewardPoolFileConfig.current();
        int generated = 0;
        List<String> professions = new ArrayList<>();
        for (String name : poolNames) {
            Path target = RewardPoolFileConfig.directory().resolve("goldentweaks-bountiful-" + name + ".toml");
            ResourceLocation source = ResourceLocation.fromNamespaceAndPath(
                    "bountiful", "bounty_pools/goldentweaks/" + name + ".json");
            try (Reader reader = resources.getResourceOrThrow(source).openAsReader()) {
                List<String> rewards = rewardRows(JsonParser.parseReader(reader).getAsJsonObject(),
                        item -> BuiltInRegistries.ITEM.containsKey(item)
                                && !item.equals(ResourceLocation.withDefaultNamespace("air")));
                if (rewards.isEmpty()) {
                    continue;
                }
                String text = "# Generated from " + source + "; regenerated on world start when enabled.\n"
                        + "# chanceWeight = weightMult scaled to integers per pool; quantityWeight = (amount.min + amount.max) / 2.\n"
                        + normalizeRewardWeights(RewardPoolFileConfig.serialize("goldentweaks_bountiful_" + name, false, rewards));
                Files.writeString(target, text, StandardCharsets.UTF_8);
                if (name.endsWith("_sell")) {
                    professions.add(name);
                }
                generated++;
            } catch (IOException | RuntimeException exception) {
                GoldenTweaks.LOGGER.error("Cannot generate AeroFreight reward pool from {}", source, exception);
            }
        }
        if (generated > 0) {
            try {
                List<String> general = generalRewardRows().stream().filter(row -> {
                    RewardEntry entry = FreightRewardsConfig.parseGeneralEntry(row);
                    ResourceLocation id = ResourceLocation.parse(entry.itemId());
                    if (BuiltInRegistries.ITEM.containsKey(id)) {
                        return true;
                    }
                    GoldenTweaks.LOGGER.warn("[AeroFreight] Skipping nonexistent general reward item {}", id);
                    return false;
                }).toList();
                if (!general.isEmpty()) {
                    Path target = RewardPoolFileConfig.directory().resolve("general.toml");
                    backupOtherGeneralFiles(target);
                    Files.writeString(target, RewardPoolFileConfig.serialize("general", true, general), StandardCharsets.UTF_8);
                }
                RewardPoolFileConfig.apply(RewardPoolFileConfig.prepareReload());
                RewardPoolWeightsConfig.POOL_WEIGHTS.set(equalPoolWeights(RewardPoolFileConfig.poolIds()));
                RewardPoolWeightsConfig.POOL_WEIGHTS.save();
                try (var stream = GTBountifulRewardPools.class.getResourceAsStream("/assets/bountiful/lang/zh_cn.json")) {
                    JsonObject names = stream == null ? new JsonObject()
                            : JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                    ModifierOrderConfig.REWARD_POOL_MODIFIERS.set(professionModifiers(professions, names));
                    ModifierOrderConfig.REWARD_POOL_MODIFIERS.save();
                    GTProfessionIntroductions.generate(professions, names);
                    List<String> specialOrders = new ArrayList<>(SpecialFreightOrdersConfig.ORDERS.get());
                    specialOrders.removeIf(row -> row.startsWith("goldentweaks_profession_"));
                    specialOrders.addAll(professionOrders(professions, names));
                    SpecialFreightOrdersConfig.ORDERS.set(specialOrders);
                    SpecialFreightOrdersConfig.ORDERS.save();
                    GoldenTweaks.LOGGER.info("[AeroFreight] Generated {} profession special order templates", professions.size());
                }
                GoldenTweaks.LOGGER.info("Generated or migrated {} Bountiful reward pools for AeroFreight", generated);
            } catch (IOException | RuntimeException exception) {
                GoldenTweaks.LOGGER.error("Cannot reload generated AeroFreight reward pools", exception);
            }
        }
    }

    static @NotNull List<String> professionOrders(@NotNull List<String> professions, @NotNull JsonObject names) {
        return professions.stream().filter(name -> name.endsWith("_sell")).sorted().map(name -> {
            String key = "bountiful.decree." + name + ".name";
            String label = names.has(key) ? names.get(key).getAsString() : name.substring(0, name.length() - 5);
            return "goldentweaks_profession_" + name + "|cargo=" + label
                    + "|size=random|type=random|chance=1.0|destination=auto"
                    + "|rewardPool=goldentweaks_bountiful_" + name
                    + "|ticketRefreshDisabled=false|lockIntroduction=true|introductionTemplate="
                    + GTProfessionIntroductions.TEMPLATE + "|introductionContent=" + name;
        }).toList();
    }

    static @NotNull List<String> professionModifiers(@NotNull List<String> professions, @NotNull JsonObject names) {
        return professions.stream().filter(name -> name.endsWith("_sell")).sorted().map(name -> {
            String key = "bountiful.decree." + name + ".name";
            String label = names.has(key) ? names.get(key).getAsString() : name.substring(0, name.length() - 5);
            return label + "|score=20|rewardPool=goldentweaks-bountiful-" + name + ".toml";
        }).toList();
    }

    static @NotNull List<String> equalPoolWeights(@NotNull List<String> ids) {
        return ids.stream().map(id -> id + "|weight=1.0").toList();
    }

    static @NotNull List<String> generalRewardRows() {
        return List.of("questshop:coin=1.0", "goldentweaks:radiant_gold=0.4", "goldentweaks:brilliant_gold=0.2");
    }

    private static void backupOtherGeneralFiles(Path target) throws IOException {
        try (var files = Files.list(target.getParent())) {
            for (Path file : files.filter(path -> path.toString().endsWith(".toml") && !path.equals(target)).toList()) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    if (!"general".equals(new TomlParser().parse(reader).get("kind"))) {
                        continue;
                    }
                }
                Path backup = file.resolveSibling(file.getFileName() + ".bak");
                int suffix = 1;
                while (Files.exists(backup)) {
                    backup = file.resolveSibling(file.getFileName() + "." + suffix++ + ".bak");
                }
                Files.move(file, backup);
                GoldenTweaks.LOGGER.info("[AeroFreight] Backed up previous general reward file to {}", backup);
            }
        }
    }

    static @NotNull String migrateLegacyPoolId(@NotNull String text, @NotNull String name) {
        // The editor flattens entries as pool:item=weights and splits at the FIRST colon.
        return text.replaceAll("(?m)^id = \"goldentweaks:bountiful/" + name + "\"\\r?$",
                "id = \"goldentweaks_bountiful_" + name + "\"");
    }

    static @NotNull String normalizeRewardWeights(@NotNull String text) {
        Pattern row = Pattern.compile("(?m)(^\\h*\"[^\"=\\r\\n]+=)([0-9.eE+\\-]+),([0-9.eE+\\-]+)");
        Matcher matcher = row.matcher(text);
        int scale = 0;
        while (matcher.find()) {
            scale = Math.max(scale, new BigDecimal(matcher.group(2)).stripTrailingZeros().scale());
        }
        matcher.reset();
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String chance = new BigDecimal(matcher.group(2)).movePointRight(scale).toBigIntegerExact().toString();
            String quantity = new BigDecimal(matcher.group(3)).stripTrailingZeros().toPlainString();
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1) + chance + "," + quantity));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    static @NotNull List<String> rewardRows(@NotNull JsonObject pool, @NotNull Predicate<ResourceLocation> available) {
        List<String> rows = new ArrayList<>();
        for (JsonElement value : pool.getAsJsonObject("content").asMap().values()) {
            JsonObject entry = value.getAsJsonObject();
            if (!"item".equals(entry.get("type").getAsString())) {
                continue;
            }
            ResourceLocation item = ResourceLocation.parse(entry.get("content").getAsString());
            if (!available.test(item)) {
                continue;
            }
            JsonObject amount = entry.getAsJsonObject("amount");
            int min = amount.get("min").getAsInt();
            int max = amount.get("max").getAsInt();
            double chance = entry.has("weightMult") ? entry.get("weightMult").getAsDouble() : 1.0;
            if (min < 1 || max < min || !Double.isFinite(chance) || chance < 0) {
                throw new IllegalArgumentException("Invalid Bountiful reward: " + item);
            }
            if (chance > 0) {
                RewardEntry reward = new RewardEntry(item.toString(), chance, ((double) min + max) / 2.0);
                rows.add(FreightRewardsConfig.serializePoolEntry(reward));
            }
        }
        return rows;
    }
}

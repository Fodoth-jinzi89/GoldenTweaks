package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.jarhandling.SecureJar;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTAspectRecipeClientTooltip;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTAspectRecipeTooltip;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class GTThaumcraftCompat {

    private GTThaumcraftCompat() {
    }

    public static void register(IEventBus modEventBus, ModContainer container) {
        GTThaumcraftAdditionalBlocks.BLOCKS.register(modEventBus);
        GTThaumcraftAdditionalBlocks.ITEMS.register(modEventBus);
        GTThaumcraftAdditionalItems.ITEMS.register(modEventBus);
        GTThaumcraftAdditionalBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        GTThaumcraftAdditionalTabs.register(modEventBus);
        GTAspectPhialColors.register(modEventBus);
        modEventBus.addListener(RegisterClientTooltipComponentFactoriesEvent.class, e ->
                e.register(GTAspectRecipeTooltip.class, GTAspectRecipeClientTooltip::new));

        registerPhialsFromJson(container);

        // Defer research registration until items are available
        modEventBus.addListener(FMLCommonSetupEvent.class, e ->
                e.enqueueWork(GTThaumcraftResearch::register));
    }

    /**
     * Registers one phial variant per aspect defined in JSON, so adding a new
     * aspect only requires a new JSON file — no Java changes.
     */
    private static void registerPhialsFromJson(ModContainer container) {
        List<String> tags = discoverAspectTags(container);
        for (String tag : tags) {
            GTThaumcraftAdditionalItems.registerPhial(tag);
        }
        if (!tags.isEmpty()) {
            GoldenTweaks.LOGGER.info("Registered {} phial variants from aspect JSON: {}.", tags.size(), tags);
        }
    }

    private static List<String> discoverAspectTags(ModContainer container) {
        List<String> tags = new ArrayList<>();
        try {
            SecureJar jar = container.getModInfo().getOwningFile().getFile().getSecureJar();
            Path dir = jar.getPath("data", "goldentweaks", "thaumcraft", "aspects");
            if (dir == null || !Files.isDirectory(dir)) {
                return tags;
            }
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .forEach(path -> {
                            String tag = readAspectTag(path);
                            if (tag != null) {
                                tags.add(tag);
                            }
                        });
            }
        } catch (Exception e) {
            GoldenTweaks.LOGGER.warn("Failed to discover aspect JSON for phial registration: {}", e.getMessage());
        }
        return tags;
    }

    private static String readAspectTag(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                return null;
            }
            JsonObject json = element.getAsJsonObject();
            JsonElement tag = json.get("tag");
            if (tag != null && tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString()) {
                return tag.getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}

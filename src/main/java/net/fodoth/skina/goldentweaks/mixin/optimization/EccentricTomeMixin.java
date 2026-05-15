package net.fodoth.skina.goldentweaks.mixin.optimization;

import net.neoforged.fml.event.config.ModConfigEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import website.eccentric.tome.EccentricConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static website.eccentric.tome.EccentricConfig.*;

@Mixin(EccentricConfig.class)
public class EccentricTomeMixin {


    @Final
    @Shadow
    private static List<String> allItems;

    @Final
    @Shadow
    private static List<String> allAliases;


    @Inject(method = "onLoad", at = @At("HEAD"), cancellable = true)
    private static void goldenTweaks$onLoad(ModConfigEvent event, CallbackInfo ci) {
        ci.cancel();

        allItems.clear();
        allAliases.clear();
        ALIAS_MAP.clear();

        allItems.addAll(ITEMS.get());
        allAliases.addAll(ALIASES.get());

        allItems.addAll(goldenTweaks$loadAssetFile("configs/items.txt"));
        allAliases.addAll(goldenTweaks$loadAssetFile("configs/aliases.txt"));

        for (String alias : allAliases) {
            String[] parts = alias.split("=");
            if (parts.length == 2) {
                ALIAS_MAP.put(parts[0].trim(), parts[1].trim());
            }
        }
    }


    @Unique
    private static List<String> goldenTweaks$loadAssetFile(String path) {
        List<String> result = new ArrayList<>();

        try {
            InputStream stream = EccentricConfig.class
                    .getClassLoader()
                    .getResourceAsStream("assets/eccentrictome/" + path);

            if (stream == null) {
                return result;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (!line.isEmpty() && !line.startsWith("#")) {
                        result.add(line);
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return result;
    }
}

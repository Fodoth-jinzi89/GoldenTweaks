package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import thaumcraft.api.aspects.Aspect;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data generation helpers for the two dynamically registered aspect items:
 * the phial of essentia ({@code phial_of_essentia_<tag>}) and the wisp essence
 * ({@code wisp_essence_<tag>}).
 *
 * <p>These items are registered per custom aspect tag, so their models and
 * localizations cannot be written once by hand. Downstream add-ons that define
 * extra aspects can call {@link #registerAspectItems} from their own datagen,
 * or populate Chinese names via {@link #addZhName} and rely on the
 * {@link GatherDataEvent} wiring installed by {@link #register}.
 *
 * <p>Tags are expected to be lowercase (as in the aspect JSON files).
 */
public final class GTThaumcraftDataGen {

    private static final ResourceLocation GENERATED =
            ResourceLocation.withDefaultNamespace("item/generated");

    /** Optional Chinese aspect display names used by the auto-discovery wiring. */
    private static final Map<String, String> ZH_ASPECT_NAMES = new LinkedHashMap<>();

    private GTThaumcraftDataGen() {
    }

    /**
     * Registers the item models and English/Chinese names for the two aspect
     * items of a single aspect tag.
     */
    public static void registerAspectItems(
            String tag,
            String englishName,
            String chineseName,
            ItemModelProvider models,
            LanguageProvider enUs,
            LanguageProvider zhCn
    ) {
        registerAspectModels(tag, models);
        registerAspectTranslations(tag, englishName, chineseName, enUs, zhCn);
    }

    /**
     * Registers the two flat item models for a single aspect tag.
     * <p>The {@code dense} aspect is skipped: its phial/wisp models are
     * hand-written cosmic models (renderblender {@code halo_cosmic} loader) and
     * must not be overwritten by datagen.
     */
    public static void registerAspectModels(String tag, ItemModelProvider models) {
        if ("dense".equals(tag)) {
            return;
        }
        models.withExistingParent("phial_of_essentia_" + tag, GENERATED)
                .texture("layer0", "thaumcraft:item/phial")
                .texture("layer1", "thaumcraft:item/essence");
        models.withExistingParent("wisp_essence_" + tag, GENERATED)
                .texture("layer0", "thaumcraft:item/wispessence");
    }

    /**
     * Registers the English and Chinese names for the two aspect items of a
     * single aspect tag.
     */
    public static void registerAspectTranslations(
            String tag,
            String englishName,
            String chineseName,
            LanguageProvider enUs,
            LanguageProvider zhCn
    ) {
        enUs.add("item.goldentweaks.phial_of_essentia_" + tag, "Phial of Essentia: " + englishName);
        enUs.add("item.goldentweaks.wisp_essence_" + tag, "Wisp Essence: " + englishName);
        zhCn.add("item.goldentweaks.phial_of_essentia_" + tag, "要素安瓿: " + chineseName);
        zhCn.add("item.goldentweaks.wisp_essence_" + tag, "天域之华: " + chineseName);
    }

    /**
     * Supplies the Chinese display name used by the {@link GatherDataEvent}
     * auto-discovery wiring. Falls back to the English name when not provided.
     */
    public static void addZhName(String tag, String chineseName) {
        ZH_ASPECT_NAMES.put(tag, chineseName);
    }

    /**
     * Installs the {@link GatherDataEvent} listener onto the mod event bus. The
     * listener only runs during {@code runData}, so calling this on every launch
     * is harmless.
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(GatherDataEvent.class, GTThaumcraftDataGen::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existing = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ItemModelProvider(output, GoldenTweaks.MODID, existing) {
            @Override
            protected void registerModels() {
                for (String tag : GTThaumcraftAdditionalItems.phials().keySet()) {
                    GTThaumcraftDataGen.registerAspectModels(tag, this);
                }
            }
        });

        generator.addProvider(event.includeClient(), new LanguageProvider(output, GoldenTweaks.MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for (String tag : GTThaumcraftAdditionalItems.phials().keySet()) {
                    String en = englishName(tag);
                    add("item.goldentweaks.phial_of_essentia_" + tag, "Phial of Essentia: " + en);
                    add("item.goldentweaks.wisp_essence_" + tag, "Wisp Essence: " + en);
                }
            }
        });

        generator.addProvider(event.includeClient(), new LanguageProvider(output, GoldenTweaks.MODID, "zh_cn") {
            @Override
            protected void addTranslations() {
                for (String tag : GTThaumcraftAdditionalItems.phials().keySet()) {
                    String zh = ZH_ASPECT_NAMES.getOrDefault(tag, englishName(tag));
                    add("item.goldentweaks.phial_of_essentia_" + tag, "要素安瓿: " + zh);
                    add("item.goldentweaks.wisp_essence_" + tag, "天域之华: " + zh);
                }
            }
        });
    }

    private static String englishName(String tag) {
        Aspect aspect = Aspect.get(tag);
        if (aspect != null && !aspect.getName().isBlank()) {
            return aspect.getName();
        }
        return tag.isEmpty() ? tag : Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
    }
}

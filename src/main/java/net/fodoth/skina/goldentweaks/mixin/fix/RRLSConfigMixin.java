package net.fodoth.skina.goldentweaks.mixin.fix;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.redlance.dima_dencep.mods.rrls.config.DoubleLoad;
import org.redlance.dima_dencep.mods.rrls.config.HideType;
import org.redlance.dima_dencep.mods.rrls.config.Type;
import org.redlance.dima_dencep.mods.rrls.neoforge.ConfigExpectPlatformImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ConfigExpectPlatformImpl.class)
public class RRLSConfigMixin {

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static HideType hideType() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .hideType.get();
        } catch (IllegalStateException e) {

            GoldenTweaks.LOGGER.warn(
                    "RRLS hideType accessed before config load, using default"
            );

            return HideType.ALL;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean rgbProgress() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .rgbProgress.get();
        } catch (IllegalStateException e) {

            return false;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean blockOverlay() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .blockOverlay.get();
        } catch (IllegalStateException e) {

            return false;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean miniRender() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .miniRender.get();
        } catch (IllegalStateException e) {

            return true;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean enableScissor() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .enableScissor.get();
        } catch (IllegalStateException e) {

            return false;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static Type type() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .type.get();
        } catch (IllegalStateException e) {

            return Type.PROGRESS;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static String reloadText() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .reloadText.get();
        } catch (IllegalStateException e) {

            return "Reloading...";
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean resetResources() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .resetResources.get();
        } catch (IllegalStateException e) {

            return true;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean reInitScreen() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .reInitScreen.get();
        } catch (IllegalStateException e) {

            return true;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean removeOverlayAtEnd() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .removeOverlayAtEnd.get();
        } catch (IllegalStateException e) {

            return true;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean earlyPackStatusSend() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .earlyPackStatusSend.get();
        } catch (IllegalStateException e) {

            return false;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static DoubleLoad doubleLoad() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .doubleLoad.get();
        } catch (IllegalStateException e) {

            return DoubleLoad.FORCE_LOAD;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static float animationSpeed() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .animationSpeed.get().floatValue();
        } catch (IllegalStateException e) {

            return 1000.0F;
        }
    }

    /**
     * @author GoldenTweaks
     * @reason Prevent config access before NeoForge config load
     */
    @Overwrite(remap = false)
    public static boolean skipForgeOverlay() {

        try {
            return ConfigExpectPlatformImpl.CONFIG_SPEC_PAIR.getKey()
                    .skipForgeOverlay.get();
        } catch (IllegalStateException e) {

            return false;
        }
    }
}
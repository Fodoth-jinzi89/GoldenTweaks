package net.fodoth.skina.goldentweaks.compat.ae2;

import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.goldentweaks.content.multiblock.recipe.client.GTEssentiaEmiStack;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTScexAspectBridge;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Teaches AE2's EMI bridge how to carry a Thaumcraft aspect.
 *
 * <p>AE2 converts an EMI stack to an {@link AEKey} through a registry of converters, one per key
 * type; a stack whose type has no converter cannot be dropped on a pattern terminal at all. Essentia
 * already has a key — {@code AEssentiaKey}, shipped by Thaumic Energistics so an aspect can live in
 * the ME network — but Thaumic Energistics registers a converter only for JEI. The aspect drawn here
 * comes from scex, whose ingredient AE2 knows nothing about, which is why dragging one out of this
 * mod's pages did nothing while the same aspect could be dragged from a Thaumic Energistics page.</p>
 *
 * <p>This supplies that half: an aspect passes through as the key Thaumic Energistics already defines,
 * addressed by the same {@code thaumcraft:<tag>} id, so a pattern written here names exactly the
 * aspect the network stores.</p>
 */
public final class GTAe2EssentiaConverter {

    private static final String THAUMIC_ENERGISTICS = "thaumicenergistics";

    private GTAe2EssentiaConverter() {
    }

    /**
     * Registers the converter, once.
     *
     * <p>Silent without Thaumic Energistics: there is no essence key type in that world, so nothing
     * could be converted to. It also keeps the mod's classes from being resolved at all.</p>
     */
    public static void register() {
        if (!ModList.get().isLoaded(THAUMIC_ENERGISTICS)) {
            return;
        }
        EmiStackConverters.register(new Converter());
    }

    /** Everything that names a Thaumic Energistics type, resolved only once the mod is known to be there. */
    private static final class Converter
            implements appeng.api.integrations.emi.EmiStackConverter {

        @Override
        public Class<?> getKeyType() {
            return thaumicenergistics.common.integration.appeng.AEssentiaKey.class;
        }

        @Override
        @Nullable
        public EmiStack toEmiStack(GenericStack stack) {
            if (!(stack.what() instanceof thaumicenergistics.common.integration.appeng.AEssentiaKey key)) {
                return null;
            }
            return GTEssentiaEmiStack.registered(key.getId().getPath(), stack.amount());
        }

        @Override
        @Nullable
        public GenericStack toGenericStack(EmiStack stack) {
            String tag = tagOf(stack);
            if (tag == null) {
                return null;
            }
            // The aspect's own id, so both sides agree on identity: an aspect stored through Thaumic
            // Energistics reads back as the same aspect here.
            return new GenericStack(
                    thaumicenergistics.common.integration.appeng.AEssentiaKey.of(
                            ResourceLocation.fromNamespaceAndPath("thaumcraft", tag)),
                    Math.max(1L, stack.getAmount()));
        }

        /**
         * The aspect tag a stack stands for.
         *
         * <p>Two stacks can carry an aspect: this mod's own, which knows its tag, and scex's, which is
         * what a recipe page actually draws when that mod is installed. The second is read back
         * through the compat bridge, so dragging from either page names the same aspect.</p>
         */
        @Nullable
        private static String tagOf(EmiStack stack) {
            if (stack instanceof GTEssentiaEmiStack essentia) {
                return essentia.tag();
            }
            return GTScexAspectBridge.tagOf(stack);
        }
    }
}

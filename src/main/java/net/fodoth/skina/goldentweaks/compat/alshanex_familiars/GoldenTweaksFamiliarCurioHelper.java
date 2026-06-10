package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class GoldenTweaksFamiliarCurioHelper {

    private GoldenTweaksFamiliarCurioHelper() {
    }

    public static boolean isWearingInvertedFamiliarSpellbook(
            LivingEntity entity
    ) {

        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> !handler.findCurios(
                        GoldenTweaksFamiliarCurioHelper::isInvertedSpellbook
                ).isEmpty())
                .orElse(false);
    }

    public static boolean isWearingRegularFamiliarSpellbook(
            LivingEntity entity
    ) {

        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> !handler.findCurios(
                        GoldenTweaksFamiliarCurioHelper::isRegularSpellbook
                ).isEmpty())
                .orElse(false);
    }

    public static boolean isInvertedSpellbook(
            ItemStack stack
    ) {

        return stack != null
                && stack.is(
                AFAdditionalItems.INVERTED_FAMILIAR_SPELLBOOK.get()
        );
    }

    private static boolean isRegularSpellbook(
            ItemStack stack
    ) {

        return stack != null
                && stack.getItem()
                instanceof net.alshanex.familiarslib.item
                .AbstractFamiliarSpellbookItem
                && !isInvertedSpellbook(stack);
    }
}

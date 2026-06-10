package net.fodoth.skina.goldentweaks.compat.create;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.fodoth.skina.goldentweaks.GoldenTweaks;

public final class GTCreateCompat {

    private GTCreateCompat() {
    }

    private static final class Holder {
        private static final CreateRegistrate INSTANCE =
                CreateRegistrate.create(GoldenTweaks.MODID)
                        .setTooltipModifierFactory(item ->
                                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static CreateRegistrate registrate() {
        return Holder.INSTANCE;
    }
}
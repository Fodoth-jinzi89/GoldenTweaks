package net.fodoth.skina.goldentweaks.mixin.feature.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.MaidEdibleBlockManager;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.snack_cabinet.avaritia.EndlessCakeMaidCompat;
import net.fodoth.skina.goldentweaks.compat.snack_cabinet.flavor_immersed_daily.FidMaidCompat;
import net.fodoth.skina.goldentweaks.compat.snack_cabinet.forbiddenmagic.ArcaneCakeMaidCompat;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaidEdibleBlockManager.class)
public class MaidEdibleBlockManagerMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
                    shift = At.Shift.BEFORE
            )
    )
    private static void addEndlessCakeEdible(CallbackInfo ci) {
        try {
            var field = MaidEdibleBlockManager.class.getDeclaredField("EDIBLE_BLOCKS");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<IMaidEdibleBlock> list = (List<IMaidEdibleBlock>) field.get(null);
            if (ModList.get().isLoaded("avaritia")) {
                list.add(new EndlessCakeMaidCompat());
            }
            if (ModList.get().isLoaded("flavor_immersed_daily")) {
                list.add(new FidMaidCompat());
            }
            if (ModList.get().isLoaded("forbiddenmagic")) {
                list.add(new ArcaneCakeMaidCompat());
            }
        } catch (Exception e) {
            GoldenTweaks.LOGGER.error("Failed to add maid edible blocks.", e);
        }
    }
}

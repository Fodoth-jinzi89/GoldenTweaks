package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.flavor_immersed_daily.client.FlavorImmersedDailyClient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(FlavorImmersedDailyClient.class)
public class FlavorImmersedDailyClientMixin {

    @Unique
    private static final Set<String> TRELLIS_SEEDS = Set.of(
            "grapeseed", "cucumberseeds", "wax_gourd_seed_block",
            "kidneybeanseed", "aubergineseedblock", "tomatoseed",
            "cowpeabeanseed", "greengrapeseed", "loofahseed"
    );

    @Inject(
            method = "onItemTooltip",
            at = @At(
                    value = "TAIL",
                    remap = false
            ),
            remap = false
    )
    private static void onItemTooltipTail(ItemTooltipEvent event, CallbackInfo ci) {
        ItemStack itemStack = event.getItemStack();
        String itemId = itemStack.getItemHolder().getRegisteredName();

        itemId = itemId.substring(itemId.lastIndexOf(':') + 1);

        // 如果是爬架作物种子，替换或者添加相应的tooltip
        if (TRELLIS_SEEDS.contains(itemId)) {
            // 移除之前添加的默认tooltip
            event.getToolTip().removeIf(component -> {
                String str = component.getString();
                return str.contains("farmland") || str.contains("耕地");
            });

            // 添加爬架作物tooltip
            event.getToolTip().add(
                    Component.translatable("tooltip.flavor_immersed_daily.crop_type.trellis")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }
    }
}

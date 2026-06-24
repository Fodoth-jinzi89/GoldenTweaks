package net.fodoth.skina.goldentweaks.compat.questshop;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class QSAdditionalTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, GoldenTweaks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> QUESTSHOP_TAB =
            TABS.register("questshop_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.goldentweaks.questshop"))
                    .icon(() -> new ItemStack(QSAdditionalItems.RADIANT_GOLD.get()))
                    .displayItems((params, output) -> {
                        output.accept(QSAdditionalItems.RADIANT_GOLD.get());
                        output.accept(QSAdditionalItems.BRILLIANT_GOLD.get());
                        output.accept(QSAdditionalItems.GLORIOUS_GOLD.get());
                        output.accept(QSAdditionalItems.LUMINOUS_GOLD.get());
                        output.accept(QSAdditionalItems.IMMACULATE_GOLD.get());
                    })
                    .build()
            );

    private QSAdditionalTabs() {
    }
}
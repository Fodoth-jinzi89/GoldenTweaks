package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily.accessor;

import com.flavor_immersed_daily.SpecialItems;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpecialItems.MultiStageInteractiveBlock.class)
public interface MultiStageInteractiveBlockAccessor {

    @Invoker("getCorrespondingItem")
    Item invokeGetCorrespondingItem(String blockName);

    @Accessor("name")
    String getName();
}
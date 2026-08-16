package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily.accessor;

import com.flavor_immersed_daily.block.block.food.MultiStageInteractiveBlock;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiStageInteractiveBlock.class)
public interface MultiStageInteractiveBlockAccessor {

    @Invoker("foodItem")
    Item invokeFoodItem();
}

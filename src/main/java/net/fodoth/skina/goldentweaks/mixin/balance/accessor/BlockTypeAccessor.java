package net.fodoth.skina.goldentweaks.mixin.balance.accessor;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.blocktype.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = BlockType.class, remap = false)
public interface BlockTypeAccessor {

    @Accessor("attributeMap")
    Map<Class<? extends Attribute>, Attribute> goldentweaks$getAttributeMap();
}

package net.fodoth.skina.goldentweaks.mixin.fix.appflux.accessor;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AEBaseMenu.class)
public interface AEBaseMenuSemanticsAccessor {

    @Accessor("semanticBySlot")
    Map<Slot, SlotSemantic> gt$getSemanticBySlot();

    @Accessor("slotsBySemantic")
    ArrayListMultimap<SlotSemantic, Slot> gt$getSlotsBySemantic();
}

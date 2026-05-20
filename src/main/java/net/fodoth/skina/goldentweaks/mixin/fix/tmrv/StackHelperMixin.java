package net.fodoth.skina.goldentweaks.mixin.fix.tmrv;

import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.StackHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StackHelper.class)
public abstract class StackHelperMixin {


    @Final
    @Shadow(remap = false)
    private mezz.jei.api.ingredients.subtypes.ISubtypeManager subtypeManager;

    /**
     * @author Fodoth_jinzi89
     * @reason Prevent NPE when subtypeInfo is null
     */
    @Overwrite(remap = false)

    public String getUniqueIdentifierForStack(ItemStack stack, UidContext context) {

        String result = StackHelper.getRegistryNameForStack(stack);

        @SuppressWarnings("removal")
        String subtypeInfo = this.subtypeManager.getSubtypeInfo(stack, context);

        // 某些模组比如 irons_jewelry 会返回 null
        // noinspection ConstantValue
        if (subtypeInfo != null && !subtypeInfo.isEmpty()) {
            result = result + ":" + subtypeInfo;
        }

        return result;
    }
}
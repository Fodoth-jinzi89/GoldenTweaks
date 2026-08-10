package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.fodoth.skina.goldentweaks.mixin.fix.create.accessor.SmartBlockEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TableClothBlockEntity.class)
public abstract class TableClothBlockEntityMixin implements SmartBlockEntityInvoker {


    /**
     * @author Fodoth_jinzi89
     * @reason Sable compat - Skip super.destroy()
     */
    @Redirect(
            method = "destroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;destroy()V"
            )
    )
    private void redirectSuperDestroy(SmartBlockEntity instance) {
        // 什么都不做，跳过 super.destroy()
    }

}

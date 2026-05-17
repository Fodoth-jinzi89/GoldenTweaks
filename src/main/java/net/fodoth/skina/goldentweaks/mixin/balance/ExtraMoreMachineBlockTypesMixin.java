package net.fodoth.skina.goldentweaks.mixin.balance;

import com.google.common.collect.Table;
import com.jerry.mekextras.common.block.attribute.ExtraAttributeUpgradeSupport;
import com.jerry.mekextras.common.integration.mekmm.content.blocktype.ExtraMoreMachineFactory;
import com.jerry.mekextras.common.integration.mekmm.registries.ExtraMoreMachineBlockTypes;
import com.jerry.mekextras.common.tier.ExtraFactoryTier;
import com.jerry.mekmm.common.content.blocktype.MoreMachineFactoryType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.mixin.balance.accessor.BlockTypeAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ExtraMoreMachineBlockTypes.class, remap = false)
public class ExtraMoreMachineBlockTypesMixin {

    @Shadow(remap = false)
    @Final
    private static Table<
            ExtraFactoryTier,
            MoreMachineFactoryType,
            ExtraMoreMachineFactory<?>
            > MM_FACTORIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void goldentweaks$patchRecyclerFactories(CallbackInfo ci) {

        if (!GoldenTweaksCommonConfig.isMekmmBalanced()) {
            return;
        }

        try {

            for (Table.Cell<
                    ExtraFactoryTier,
                    MoreMachineFactoryType,
                    ExtraMoreMachineFactory<?>
                    > cell : MM_FACTORIES.cellSet()) {

                if (cell.getColumnKey() != MoreMachineFactoryType.RECYCLING) {
                    continue;
                }

                ExtraMoreMachineFactory<?> factory = cell.getValue();

                goldentweaks$replaceUpgradeAttributes(factory);
            }


        } catch (Exception e) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to patch MekMM recycler factories",
                    e
            );
        }
    }

    @Unique
    private static void goldentweaks$replaceUpgradeAttributes(Object blockType) {

        Map<Class<? extends Attribute>, Attribute> attributes =
                ((BlockTypeAccessor) blockType)
                        .goldentweaks$getAttributeMap();

        attributes.put(
                AttributeUpgradeSupport.class,
                ExtraAttributeUpgradeSupport.EXTRA_MACHINE_UPGRADES
        );

    }
}
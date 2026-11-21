package net.fodoth.skina.goldentweaks.compat.jade;

import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMachineBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Hooks the kettle into Jade's tooltip.
 *
 * <p>Jade discovers plugins by annotation, so this class only has to exist and be annotated: the
 * mod loader never names it. Both halves are needed — the server half ships the live state, the
 * client half draws it — and both are registered against the host block.</p>
 */
@WailaPlugin("goldentweaks")
public final class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new GTReactorJadeProvider(),
                GTChemicalReactorMachineBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new GTReactorJadeComponent(),
                GTChemicalReactorMachineBlock.class);
    }
}

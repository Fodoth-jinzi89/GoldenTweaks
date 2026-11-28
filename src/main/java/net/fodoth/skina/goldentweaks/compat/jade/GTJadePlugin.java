package net.fodoth.skina.goldentweaks.compat.jade;

import net.fodoth.skina.goldentweaks.content.multiblock.common.GTMachineBlock;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlock;
import net.neoforged.fml.ModList;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Hooks the kettle and the general assembly into Jade's tooltip.
 *
 * <p>Jade discovers plugins by annotation, so this class only has to exist and be annotated: the
 * mod loader never names it. Both halves are needed — the server half ships the live state, the
 * client half draws it — and both are registered against the host block.</p>
 *
 * <p>The assembly registers a client half only. Its contents already reach the client with the
 * block-entity update, so a server half would be a second copy of the same numbers on every
 * hover.</p>
 */
@WailaPlugin("goldentweaks")
public final class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new GTReactorJadeProvider(),
                GTMachineBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new GTReactorJadeComponent(),
                GTMachineBlock.class);
        // Guarded: the port classes reference AMI's port types, and naming one of them without the
        // mod present fails class verification outright.
        if (ModList.get().isLoaded("avaritia_more_items")) {
            // Every port block, not just the assemblies: the component answers with nothing for the
            // other kinds, and one block class is shared by all of them.
            registration.registerBlockComponent(new GTGeneralJadeComponent(), GTPortBlock.class);
        }
    }
}

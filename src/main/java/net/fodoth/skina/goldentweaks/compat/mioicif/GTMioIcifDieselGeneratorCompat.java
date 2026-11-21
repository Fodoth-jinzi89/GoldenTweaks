package net.fodoth.skina.goldentweaks.compat.mioicif;

import com.miophas.singularity_iteration.common.blockentity.generator.mio_icif_diesel_generator;
import com.miophas.singularity_iteration.common.registry.mio_icif_block_entities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * SI 的柴油发电机内部本来就有 240,000 mB 的柴油罐，但模组只给自己的处理器/热力机器注册了
 * {@code Capabilities.FluidHandler.BLOCK}，柴油发电机不在名单里，于是管道无法输入，只能往槽位塞桶。
 *
 * <p>这里补注册一个只允许注入、不允许抽取的流体仓：燃料种类校验仍由原版燃料罐负责
 * （只接受 {@code mio_icif:dieseloil}）。</p>
 */
public final class GTMioIcifDieselGeneratorCompat {

    private GTMioIcifDieselGeneratorCompat() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                mio_icif_block_entities.DIESEL_GENERATOR_ENTITY_TYPE.get(),
                (generator, side) -> new FuelTankInput(generator)
        );
    }

    private static final class FuelTankInput implements IFluidHandler {

        private final mio_icif_diesel_generator generator;

        private FuelTankInput(mio_icif_diesel_generator generator) {
            this.generator = generator;
        }

        @Override
        public int getTanks() {
            return this.generator.getFuelTank().getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return this.generator.getFuelTank().getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return this.generator.getFuelTank().getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return this.generator.getFuelTank().isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return this.generator.getFuelTank().fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}

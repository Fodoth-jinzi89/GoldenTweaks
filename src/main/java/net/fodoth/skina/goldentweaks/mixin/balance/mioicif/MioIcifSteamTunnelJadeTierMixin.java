package net.fodoth.skina.goldentweaks.mixin.balance.mioicif;

import com.miophas.singularity_iteration.common.integration.jade.EUEnergyStorageProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 Jade 对蒸汽隧道电力端口走「动态层级」分支。
 *
 * <p>Jade 的 {@code EUEnergyStorageProvider.currentTier} 只在
 * {@code EnergyNodeRegistry.dynamicTier(...)} 为真时才回退到 {@code getSourceTier()}，
 * 否则直接用方块构造时写死的 {@code CableTier.HV}。蒸汽隧道电力端口没有注册成
 * dynamic 节点，因此即便 {@code getSourceTier()} 被改写，Jade 依旧显示 HV。</p>
 *
 * <p>这里把「判断是否动态层级」这一步改成：该端口一律视为动态，
 * 于是 Jade 会去读我们改写后的 {@code getSourceTier()}。</p>
 */
@Mixin(value = EUEnergyStorageProvider.class, remap = false)
public abstract class MioIcifSteamTunnelJadeTierMixin {

    private static final ResourceLocation STEAM_TUNNEL_POWER_PORT =
            ResourceLocation.fromNamespaceAndPath("mio_icif", "generator/block_steam_tunnel_power_port");

    /**
     * 把 {@code EnergyNodeRegistry.dynamicTier(id)} 的返回值对蒸汽隧道电力端口强制为 true。
     *
     * <p>注入在 {@code currentTier} 里那句判定之前，直接把结果替换掉。</p>
     */
    @Inject(
            method = "currentTier",
            at = @At("HEAD"),
            cancellable = true)
    private static void gt$steamTunnelUsesDynamicTier(
            BlockEntity blockEntity,
            com.miophas.singularity_iteration.core.api.energy.ICableTier fallback,
            CallbackInfoReturnable<com.miophas.singularity_iteration.core.api.energy.ICableTier> cir) {
        if (blockEntity == null) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        if (!STEAM_TUNNEL_POWER_PORT.equals(id)) {
            return;
        }
        if (!(blockEntity instanceof com.miophas.singularity_iteration.core.prefab.blockentity.AbstractEnergyBlockEntity energyBlock)) {
            return;
        }

        int index = energyBlock.getSourceTier() - 1;
        java.util.List<com.miophas.singularity_iteration.core.api.energy.storage.CableTier> tiers =
                com.miophas.singularity_iteration.core.api.energy.storage.CableTier.allTiers();
        cir.setReturnValue(index >= 0 && index < tiers.size() ? tiers.get(index) : fallback);
    }
}

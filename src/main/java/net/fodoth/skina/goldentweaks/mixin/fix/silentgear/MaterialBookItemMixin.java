package net.fodoth.skina.goldentweaks.mixin.fix.silentgear;

import net.fodoth.skina.goldentweaks.network.packet.S2COpenMaterialBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.silentchaos512.gear.item.MaterialBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MaterialBookItem.class)
public class MaterialBookItemMixin {
    /**
     * @author Fodoth
     * @reason Fix Material Book screen opening for all players in multiplayer
     */
    @Overwrite
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new S2COpenMaterialBookPacket());
        }

        return InteractionResultHolder.sidedSuccess(
                player.getItemInHand(usedHand),
                level.isClientSide
        );
    }

}
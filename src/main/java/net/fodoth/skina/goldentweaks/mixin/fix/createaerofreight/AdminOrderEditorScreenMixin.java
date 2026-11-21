package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.AdminOrderEditorPublishPayload;
import cn.blockforge.generated.createareofreight.client.AdminOrderEditorScreen;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTEditorTransfer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AdminOrderEditorScreen.class, remap = false)
public abstract class AdminOrderEditorScreenMixin {
    @Redirect(method = "publish", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/network/PacketDistributor;sendToServer(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"))
    private void gt$publishParts(CustomPacketPayload payload, CustomPacketPayload[] extra) {
        GTEditorTransfer.publish((AdminOrderEditorPublishPayload) payload);
    }
}

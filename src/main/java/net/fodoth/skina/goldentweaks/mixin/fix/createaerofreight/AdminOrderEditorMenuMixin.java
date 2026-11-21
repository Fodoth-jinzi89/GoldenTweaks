package net.fodoth.skina.goldentweaks.mixin.fix.createaerofreight;

import cn.blockforge.generated.createareofreight.admin.AdminOrderEditorMenu;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTEditorLists;
import net.fodoth.skina.goldentweaks.compat.createaerofreight.GTEditorTransfer;
import cn.blockforge.generated.createareofreight.admin.AdminOrderEditorService;
import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AdminOrderEditorMenu.class, remap = false)
public abstract class AdminOrderEditorMenuMixin {

    @Shadow private static AdminOrderEditorService.Snapshot readSnapshot(RegistryFriendlyByteBuf buffer) { throw new AssertionError(); }

    @Redirect(method = "open", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;"))
    private static OptionalInt gt$sendSnapshotParts(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> writer) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        try {
            writer.accept(buffer);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            var id = GTEditorTransfer.send(bytes, part -> PacketDistributor.sendToPlayer(player, part));
            return player.openMenu(provider, marker -> { marker.writeInt(-1); marker.writeUUID(id); });
        } finally {
            buffer.release();
        }
    }

    @Inject(method = "readSnapshot", at = @At("HEAD"), cancellable = true)
    private static void gt$readSnapshotParts(RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<AdminOrderEditorService.Snapshot> cir) {
        if (buffer.readableBytes() >= 4 && buffer.getInt(buffer.readerIndex()) == -1) {
            buffer.readInt();
            RegistryFriendlyByteBuf complete = new RegistryFriendlyByteBuf(
                    Unpooled.wrappedBuffer(GTEditorTransfer.take(buffer.readUUID())), buffer.registryAccess());
            try {
                cir.setReturnValue(readSnapshot(complete));
            } finally {
                complete.release();
            }
        }
    }

    @Inject(method = "readList", at = @At("HEAD"), cancellable = true)
    private static void gt$readCompleteList(RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<List<String>> cir) {
        cir.setReturnValue(GTEditorLists.read(buffer));
    }
}

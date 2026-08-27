package net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload;

import com.gali.ae2_auto_pattern_upload.network.PatternUploadUtil;
import com.gali.ae2_auto_pattern_upload.network.ProvidersListS2CPacket;
import com.gali.ae2_auto_pattern_upload.network.RequestProvidersListC2SPacket;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.PeatPatternUploadCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.fodoth.skina.goldentweaks.network.packet.S2CProviderIconsPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yuuki1293.ae2peat.menu.PatternEncodingAccessTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;

import java.util.List;
import java.util.stream.LongStream;

@Mixin(RequestProvidersListC2SPacket.class)
public abstract class RequestProvidersListC2SPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gt$handlePeat(RequestProvidersListC2SPacket packet, IPayloadContext context, CallbackInfo ci) {
        if (context.player().containerMenu instanceof PatternEncodingAccessTermMenu menu) {
            context.enqueueWork(() -> {
                List<PatternUploadUtil.ProviderEntry> providers = PeatPatternUploadCompat.listProviders(menu);
                List<Long> ids = LongStream.range(0, providers.size()).map(i -> -1L - i).boxed().toList();
                List<Component> names = providers.stream().map(PatternUploadUtil.ProviderEntry::name).toList();
                List<Integer> emptySlots = providers.stream().map(PatternUploadUtil.ProviderEntry::emptySlots).toList();
                List<ItemStack> icons = providers.stream().map(entry -> entry.provider().getTerminalGroup() == null
                        || entry.provider().getTerminalGroup().icon() == null
                        ? ItemStack.EMPTY
                        : entry.provider().getTerminalGroup().icon().toStack()).toList();
                ServerPlayer player = (ServerPlayer) context.player();
                player.connection.send(new S2CProviderIconsPacket(names.stream().map(Component::getString).toList(), icons));
                player.connection.send(new ProvidersListS2CPacket(ids, names, emptySlots));
            });
            ci.cancel();
        } else if (context.player().containerMenu instanceof PatternEncodingTermMenu menu) {
            context.enqueueWork(() -> {
                List<PatternUploadUtil.ProviderEntry> providers = PatternUploadUtil.listAvailableProviders(menu);
                List<Long> ids = LongStream.range(0, providers.size()).map(i -> -1L - i).boxed().toList();
                List<Component> names = providers.stream().map(PatternUploadUtil.ProviderEntry::name).toList();
                List<Integer> emptySlots = providers.stream().map(PatternUploadUtil.ProviderEntry::emptySlots).toList();
                List<ItemStack> icons = providers.stream().map(entry -> entry.provider().getTerminalGroup() == null
                        || entry.provider().getTerminalGroup().icon() == null
                        ? ItemStack.EMPTY
                        : entry.provider().getTerminalGroup().icon().toStack()).toList();
                ServerPlayer player = (ServerPlayer) context.player();
                player.connection.send(new S2CProviderIconsPacket(names.stream().map(Component::getString).toList(), icons));
                player.connection.send(new ProvidersListS2CPacket(ids, names, emptySlots));
            });
            ci.cancel();
        }
    }
}

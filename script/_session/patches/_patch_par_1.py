import io

# ============ 1. PACKET ============
pkt = r'src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SMachineParallelPacket.java'
io.open(pkt, 'w', encoding='utf-8', newline='\n').write('''package net.fodoth.skina.goldentweaks.network.packet;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.avaritia_more_items.GTChemicalReactorMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Sets the machine's parallel ceiling.
 *
 * <p>{@code 0} means "always run at the maximum", which is the field's default. Values below that
 * have no meaning, so the block entity clamps instead of rejecting.</p>
 */
public record C2SMachineParallelPacket(BlockPos pos, int limit) implements CustomPacketPayload {

    public static final Type<C2SMachineParallelPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    GoldenTweaks.MODID, "machine_parallel"));

    public static final StreamCodec<FriendlyByteBuf, C2SMachineParallelPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeBlockPos(packet.pos());
                        buffer.writeVarInt(packet.limit());
                    },
                    buffer -> new C2SMachineParallelPacket(buffer.readBlockPos(),
                            buffer.readVarInt()));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SMachineParallelPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.level().isLoaded(pkt.pos())) {
                return;
            }
            if (player.distanceToSqr(
                    pkt.pos().getX() + 0.5D, pkt.pos().getY() + 0.5D, pkt.pos().getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (player.level().getBlockEntity(pkt.pos())
                    instanceof GTChemicalReactorMachineBlockEntity machine) {
                machine.setParallelLimit(pkt.limit());
            }
        });
    }
}
''')
print('packet written')

# ============ 2. REGISTER ============
p = r'src/main/java/net/fodoth/skina/goldentweaks/network/ModNetworking.java'
s = io.open(p, encoding='utf-8').read()
orig = s
if 'C2SMachineParallelPacket;' not in s:
    s = s.replace('import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;',
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineOverclockPacket;\n'
                  'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineParallelPacket;', 1)
s = s.replace('''        registrar.playToServer(
                C2SMachineOverclockPacket.TYPE,
                C2SMachineOverclockPacket.STREAM_CODEC,
                C2SMachineOverclockPacket::handle
        );''',
'''        registrar.playToServer(
                C2SMachineOverclockPacket.TYPE,
                C2SMachineOverclockPacket.STREAM_CODEC,
                C2SMachineOverclockPacket::handle
        );

        registrar.playToServer(
                C2SMachineParallelPacket.TYPE,
                C2SMachineParallelPacket.STREAM_CODEC,
                C2SMachineParallelPacket::handle
        );''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('networking:', 'changed' if s != orig else 'NO CHANGE')

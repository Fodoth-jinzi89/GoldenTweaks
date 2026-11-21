import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/network/ModNetworking.java'
s = io.open(p, encoding='utf-8').read()
orig = s
s = s.replace('import net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;',
              'import net.fodoth.skina.goldentweaks.network.packet.C2SMachineNamePacket;\nimport net.fodoth.skina.goldentweaks.network.packet.C2SPickupItemPacket;', 1)
s = s.replace('''        registrar.playToServer(
                C2SPortTogglePacket.TYPE,
                C2SPortTogglePacket.STREAM_CODEC,
                C2SPortTogglePacket::handle
        );''','''        registrar.playToServer(
                C2SPortTogglePacket.TYPE,
                C2SPortTogglePacket.STREAM_CODEC,
                C2SPortTogglePacket::handle
        );

        registrar.playToServer(
                C2SMachineNamePacket.TYPE,
                C2SMachineNamePacket.STREAM_CODEC,
                C2SMachineNamePacket::handle
        );''', 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')

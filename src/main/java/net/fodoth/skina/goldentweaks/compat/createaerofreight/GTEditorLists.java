package net.fodoth.skina.goldentweaks.compat.createaerofreight;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class GTEditorLists {

    private GTEditorLists() {
    }

    public static @NotNull List<String> read(@NotNull FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        // Even an empty UTF string needs one length byte. Reject corrupt counts before allocating.
        if (count < 0 || count > buffer.readableBytes()) {
            throw new DecoderException("Invalid AeroFreight editor list count: " + count);
        }
        List<String> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(buffer.readUtf(32767));
        }
        return List.copyOf(entries);
    }
}

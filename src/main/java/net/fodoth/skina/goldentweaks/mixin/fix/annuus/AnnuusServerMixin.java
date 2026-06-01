package net.fodoth.skina.goldentweaks.mixin.fix.annuus;

import com.github.cao.awa.annuus.server.AnnuusServer;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = AnnuusServer.class, remap = false)
public class AnnuusServerMixin {

    /**
     * @author GoldenTweaks
     * @reason Replace obsolete Fabric accessor usage with official NeoForge API
     */
    @Overwrite
    private static Connection convertToStandardConnection(Object target) {
        return switch (target) {
            case ServerCommonPacketListenerImpl handler ->
                    handler.getConnection();

            case ServerPlayer player ->
                    player.connection.getConnection();

            case Connection connection ->
                    connection;

            default ->
                    throw new UnsupportedOperationException(
                            "Cannot convert object '" + target + "' to the standard key"
                    );
        };
    }
}
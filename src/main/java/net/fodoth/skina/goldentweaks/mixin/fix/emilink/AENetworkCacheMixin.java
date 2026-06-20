package net.fodoth.skina.goldentweaks.mixin.fix.emilink;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import org.chatterjay.emiextend.client.AENetworkCache;
import org.chatterjay.emiextend.client.DiskCacheIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(AENetworkCache.class)
public abstract class AENetworkCacheMixin {

    @Redirect(
            method = "loadFromDisk",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/chatterjay/emiextend/client/DiskCacheIO;load(Ljava/nio/file/Path;)[B"
            ),
            remap = false
    )
    private static byte[] goldentweaks$limitCache(Path path) {
        byte[] data = DiskCacheIO.load(path);

        if (data != null && data.length > 16 * 1024 * 1024) {
            GoldenTweaks.LOGGER.warn(
                    "EmiLink cache too large ({} MB), ignoring",
                    data.length / 1024 / 1024
            );
            return null;
        }

        return data;
    }
}

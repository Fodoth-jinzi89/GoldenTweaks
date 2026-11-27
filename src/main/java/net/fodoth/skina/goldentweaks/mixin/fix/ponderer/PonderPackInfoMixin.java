package net.fodoth.skina.goldentweaks.mixin.fix.ponderer;

import com.nododiiiii.ponderer.workspace.api.PonderPackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

/**
 * Skips resource packs that cannot be Ponderer packs, without unpacking them.
 *
 * <p>{@code PonderPackInfo.fromZip} walks a zip with a {@code ZipInputStream}, which has to inflate
 * every entry in order until it reaches {@code pack.json}. A pack that is not a Ponderer pack has no
 * such entry, so the walk ends only after the whole archive has been read. Scanning a normal
 * {@code resourcepacks} folder means doing that to every pack the player owns: 76 packs took about
 * 1.3 seconds at startup here, of which one was actually a Ponderer pack.</p>
 *
 * <p>The test used here is the cheap one: {@code ZipFile} reads the central directory only, so asking
 * whether an entry exists costs one seek rather than a full decompression pass. A pack without
 * {@code pack.json} is answered {@code null} exactly as the original method would, only far sooner.</p>
 */
@Mixin(value = PonderPackInfo.class, remap = false)
public abstract class PonderPackInfoMixin {

    /**
     * The name the original method looks for, as a top-level entry.
     *
     * <p>A nested {@code pack.json} is not it: the original compares the entry's full name for
     * equality against {@code "pack.json"}, so an archive that only has one deeper inside is not a
     * Ponderer pack either.</p>
     */
    private static final String PACK_ENTRY = "pack.json";

    @Inject(method = "fromZip", at = @At("HEAD"), cancellable = true)
    private static void gt$skipNonPondererPacks(Path path,
                                                CallbackInfoReturnable<PonderPackInfo> callback) {
        // The original returns null for a path that does not exist; leaving that case to it keeps one
        // source of truth for the missing-file answer.
        if (path == null) {
            callback.setReturnValue(null);
            return;
        }
        try (ZipFile zip = new ZipFile(path.toFile())) {
            if (zip.getEntry(PACK_ENTRY) == null) {
                callback.setReturnValue(null);
            }
        } catch (IOException unreadable) {
            // A corrupt or unreadable archive has nothing to offer either way; the original would
            // fail on it too, and letting it try keeps its own error handling intact.
        }
    }
}

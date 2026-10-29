package net.fodoth.skina.goldentweaks.mixin.fix.minecraft;

import net.minecraft.util.DirectoryLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * 退出世界后回到标题界面时，刚关掉的世界目录 session.lock 仍被本进程的整合服务器持有
 * （区块卸载要花上一段时间才真正释放；装了 FastQuit 时更是会一直留着）。
 * 世界列表刷新会在后台线程调用 {@link DirectoryLock#isLocked(java.nio.file.Path)}，
 * 其中 {@link FileChannel#tryLock()} 对「同一 JVM 已持有该锁」抛的是
 * {@link OverlappingFileLockException}（既不是返回 {@code null}，也不是 {@code IOException}），
 * 原版只捕获了 IOException / AccessDenied / NoSuchFile，于是这个运行时异常冒泡到
 * {@code LevelStorageSource.lambda$loadLevelSummaries$3} 的 {@code catch (Exception)} 里，
 * 刷出一大串 {@code Failed to read ... lock} 并把该世界的摘要整个丢掉。
 *
 * <p>这里把「本 JVM 已持有」按「已被锁定」处理（走原版锁被其它进程占用时的正常分支）：
 * 异常不再出现，世界照常留在列表里并标记为使用中，等整合服务器真正释放锁后自动恢复正常。</p>
 */
@Mixin(DirectoryLock.class)
public class DirectoryLockMixin {

    @Redirect(
            method = "isLocked",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/channels/FileChannel;tryLock()Ljava/nio/channels/FileLock;"
            )
    )
    private static FileLock gt$treatSameJvmLockAsLocked(FileChannel channel) throws IOException {
        try {
            return channel.tryLock();
        } catch (OverlappingFileLockException e) {
            return null;
        }
    }
}

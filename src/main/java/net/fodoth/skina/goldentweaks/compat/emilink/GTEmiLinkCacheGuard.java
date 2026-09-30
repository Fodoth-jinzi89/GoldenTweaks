package net.fodoth.skina.goldentweaks.compat.emilink;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * emilink（EmiLink / emiextend）磁盘缓存守卫。
 *
 * <p>emilink 把「每个服务器的 AE 网络物品可用性」缓存到 {@code <游戏目录>/emilink/cache.bin}
 * （{@code EmCc} 魔数 + LZ4，内容是 {@code ItemStack} 的序列化）。它<b>只在缓存格式版本变化时失效</b>，
 * 完全不感知模组 / 物品注册表的变化：一旦增删模组，缓存里的物品 id 就对不上了，轻则搜索与网络标记失灵，
 * 重则加载时解压出一个巨大的字节数组直接把堆打爆（{@code DiskCacheIO.load} 用
 * {@code LZ4FrameInputStream.readAllBytes()}，没有任何大小上限）。这正是「每次新增模组都要手动删 cache」的根因。</p>
 *
 * <p>本类在客户端启动时替代手动删除，做两件事：</p>
 * <ol>
 *   <li><b>模组列表指纹</b>（全部 modId@version 排序后的 SHA-1）与上次不同 ⇒ 缓存已过期，删除；</li>
 *   <li>缓存文件<b>体积超过 {@link #MAX_CACHE_BYTES}</b> ⇒ 直接删除，避免下次加载时 OOM。</li>
 * </ol>
 *
 * <p>指纹存在 {@code config/goldentweaks-emilink-modlist.txt}。emilink 未安装时什么都不做。</p>
 */
public final class GTEmiLinkCacheGuard {

    private static final String MOD_ID = "emilink";

    private static final String CACHE_DIR = "emilink";

    private static final String CACHE_FILE = "cache.bin";

    private static final String STAMP_FILE = "goldentweaks-emilink-modlist.txt";

    /** 缓存体积上限：正常只有几十 KB ~ 几 MB，超过即视为异常（防止加载时整块解压 OOM）。 */
    private static final long MAX_CACHE_BYTES = 16L * 1024L * 1024L;

    private GTEmiLinkCacheGuard() {
    }

    public static void onClientSetup() {

        if (!ModList.get().isLoaded(MOD_ID)) {
            return;
        }

        try {
            Path cache = FMLPaths.GAMEDIR.get().resolve(CACHE_DIR).resolve(CACHE_FILE);
            Path stamp = FMLPaths.CONFIGDIR.get().resolve(STAMP_FILE);

            String fingerprint = fingerprint();

            long size = Files.isRegularFile(cache) ? Files.size(cache) : -1L;

            boolean oversize = size > MAX_CACHE_BYTES;
            boolean stale = !fingerprint.equals(readStamp(stamp));

            if (stale || oversize) {
                boolean deleted = Files.deleteIfExists(cache);

                GoldenTweaks.LOGGER.info(
                        "[GT-emilink] {} emilink 缓存：{}（{} 字节）",
                        deleted ? "已清除" : "无需清除",
                        oversize ? "体积异常" : "模组列表变化",
                        size);
            }

            Files.writeString(stamp, fingerprint, StandardCharsets.UTF_8);

        } catch (Throwable t) {
            GoldenTweaks.LOGGER.warn("[GT-emilink] 处理 emilink 缓存失败", t);
        }
    }

    private static String readStamp(Path stamp) {

        try {
            return Files.isRegularFile(stamp)
                    ? Files.readString(stamp, StandardCharsets.UTF_8).trim()
                    : "";
        } catch (Throwable t) {
            return "";
        }
    }

    private static String fingerprint() throws Exception {

        List<String> ids = new ArrayList<>();

        ModList.get().getMods().forEach(info -> ids.add(info.getModId() + "@" + info.getVersion()));

        ids.sort(String::compareTo);

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(String.join("\n", ids).getBytes(StandardCharsets.UTF_8));

        StringBuilder builder = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            builder.append(Character.forDigit((b >> 4) & 0xF, 16));
            builder.append(Character.forDigit(b & 0xF, 16));
        }

        return builder.toString();
    }
}

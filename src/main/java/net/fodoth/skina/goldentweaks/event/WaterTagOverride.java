package net.fodoth.skina.goldentweaks.event;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

/**
 * createnuclear 与 flavor_immersed_daily_argo 都会往 minecraft:water 流体标签里合并自己的流体，
 * 导致 EMI 在显示使用该标签的配方时多出液氮和水车中的水。
 * 1.21.1 的标签不支持移除条目，这里注册一个最高优先级的数据包，用 replace 重写为原版内容。
 *
 * <p><b>为什么不用 {@code addPackFinders}:</b> 那个重载会调用
 * {@code Pack.readMetaAndCreate} 去读包里的 {@code pack.mcmeta}，读取失败时它返回
 * {@code null}，却仍把这个 null 塞进 repository，于是 {@code PackRepository} 在
 * {@code streamSelfAndChildren()} 上抛 NPE，整个存档都无法进入。而它在 NeoForge 的
 * {@code UnionFileSystem} 上读嵌套目录里的文件并不稳定（日志里的
 * {@code invalid LOC header}），也就是说这条路径随时可能把玩家的世界弄成打不开。</p>
 *
 * <p>这里改为注册自己的 {@link RepositorySource}：包资源直接由 {@link PathPackResources}
 * 指向 mod 文件里的 {@code gt_tag_overrides} 目录，元数据在这里显式构造，不经过那次读取。</p>
 */
@EventBusSubscriber(modid = GoldenTweaks.MODID)
public final class WaterTagOverride {

    private static final String PACK_DIRECTORY = "gt_tag_overrides";
    private static final String PACK_ID = GoldenTweaks.MODID + ":" + PACK_DIRECTORY;

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        // Only the data pack carries the tag override; resource packs have nothing to add here.
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }

        Path root = packRoot();
        if (root == null) {
            GoldenTweaks.LOGGER.warn(
                    "[GT] Bundled data pack {} is missing; water tag override not applied", PACK_DIRECTORY);
            return;
        }

        event.addRepositorySource((consumer) -> {
            Pack pack = createPack(root);
            if (pack != null) {
                consumer.accept(pack);
            }
        });
    }

    /** Location of the bundled pack inside this mod's jar, or {@code null} when absent. */
    private static Path packRoot() {
        var container = ModList.get().getModContainerById(GoldenTweaks.MODID);
        if (container.isEmpty()) {
            return null;
        }
        Path path = container.get().getModInfo().getOwningFile().getFile()
                .findResource(PACK_DIRECTORY);
        // A directory that does not exist would make PathPackResources fail later; treat it as
        // missing so nothing is registered at all.
        return path != null && PathPackResources.validatePath(path) ? path : null;
    }

    /**
     * Builds the pack with explicit metadata.
     *
     * <p>The description and compatibility are supplied here instead of being parsed from
     * {@code pack.mcmeta}, which is the read that fails on NeoForge's union file system. The public
     * {@code Pack} constructor takes the metadata directly, so no file access happens at all while
     * the pack is being registered.</p>
     */
    private static Pack createPack(Path root) {
        try {
            PackLocationInfo location = new PackLocationInfo(
                    PACK_ID,
                    Component.literal("GoldenTweaks Water Tag Override"),
                    PackSource.FEATURE,
                    Optional.empty());

            Pack.Metadata metadata = new Pack.Metadata(
                    Component.literal("GoldenTweaks water tag override"),
                    PackCompatibility.COMPATIBLE,
                    FeatureFlags.DEFAULT_FLAGS,
                    java.util.List.of());

            Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) {
                    return new PathPackResources(info, root);
                }

                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata ignored) {
                    return new PathPackResources(info, root);
                }
            };

            PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);
            return new Pack(location, supplier, metadata, selection);
        } catch (RuntimeException error) {
            GoldenTweaks.LOGGER.error("[GT] Could not register the water tag override pack", error);
            return null;
        }
    }

    private WaterTagOverride() {
    }
}

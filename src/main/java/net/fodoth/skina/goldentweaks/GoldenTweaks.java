package net.fodoth.skina.goldentweaks;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import com.mojang.logging.LogUtils;
import net.fodoth.skina.goldentweaks.compat.cataclysm.GTCataclysmCompat;
import net.fodoth.skina.goldentweaks.compat.academy.AcademyStormWingFallImmunity;
import net.fodoth.skina.goldentweaks.compat.academy.GMMediaPlayerState;
import net.fodoth.skina.goldentweaks.compat.academy.GTRailgunDestruction;
import net.fodoth.skina.goldentweaks.compat.academy.GTThermalCoinRailgun;
import net.fodoth.skina.goldentweaks.compat.academy.network.GTMediaUnloadPacket;
import net.fodoth.skina.goldentweaks.compat.appflux.AppliedFluxCompat;
import net.fodoth.skina.goldentweaks.compat.maidsoulcore.MaidLogSuppressor;
import net.fodoth.skina.goldentweaks.compat.create.GTCreateItems;
import net.fodoth.skina.goldentweaks.compat.kaleidoscope.KaleidoCompat;
import net.fodoth.skina.goldentweaks.compat.compactmachines.CompactMachinesCompat;
import net.fodoth.skina.goldentweaks.compat.compactmachines.CompactMachinesMobSpawnHandler;
import net.fodoth.skina.goldentweaks.compat.kaleidoscope.VillageGarbageStationAddition;
import net.fodoth.skina.goldentweaks.compat.questshop.QSCompat;
import net.fodoth.skina.goldentweaks.compat.fix.renderblender.RenderBlenderCosmicQueueFlushHandler;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftCompat;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.GTThaumcraftRecipeLoader;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.BrainJarResearchEvent;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.ThaumometerStorageScanQueue;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTAnimatedIconAnimator;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTThaumcraftClientData;
import net.fodoth.skina.goldentweaks.compat.thaumcraft.client.GTWardedGlassRenderer;
import net.fodoth.skina.goldentweaks.compat.thaumicbases.ThaumicCropHarvestEvent;
import net.fodoth.skina.goldentweaks.compat.thaumicbases.GTThaumicBasesFurnaceCompat;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalCreativeTabs;
import net.fodoth.skina.goldentweaks.compat.astralsorcery.GTFocalPointColumnRenderer;
import net.fodoth.skina.goldentweaks.event.ClientWindowSizeGuard;
import net.fodoth.skina.goldentweaks.event.ExspectrimentsClientCompatEvent;
import net.fodoth.skina.goldentweaks.event.FamiliarProtectionEvent;
import net.fodoth.skina.goldentweaks.event.InvertedFamiliarSpellbookEvent;
import net.fodoth.skina.goldentweaks.event.QuestBookRecipeKeyHandler;
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.flavorimmerseddaily.FidAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.thermal.ThermalCrystallarieumAugment;
import net.fodoth.skina.goldentweaks.compat.create.GTCreateCompat;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.GTCreateOreExcavationItems;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.GTCreateOreExcavationTooltip;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SSCreateCompat;
import net.fodoth.skina.goldentweaks.event.SalvageCharmEvent;
import net.fodoth.skina.goldentweaks.compat.lootr.LootrQuickLootEvent;
import net.fodoth.skina.goldentweaks.compat.traveloptics.TravelOpticsSummonProtection;
import net.fodoth.skina.goldentweaks.compat.mcwifipnp.McWifiPnPZstdPortLayout;
import net.fodoth.skina.goldentweaks.compat.oei.OreUnificationGuard;
import net.fodoth.skina.goldentweaks.compat.ftbchunks.GTClaimProtection;
import net.fodoth.skina.goldentweaks.compat.ftblibrary.client.GTFtbSidebarBlacklist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(GoldenTweaks.MODID)
public class GoldenTweaks {

    public static final String MODID = "goldentweaks";

    public static final Logger LOGGER = LogUtils.getLogger();

    public GoldenTweaks(IEventBus modEventBus, ModContainer container) {

        container.registerConfig(
                ModConfig.Type.CLIENT,
                GoldenTweaksClientConfig.SPEC
        );

        container.registerConfig(
                ModConfig.Type.COMMON,
                GoldenTweaksCommonConfig.SPEC
        );

        try {
            Class.forName("com.github.luben.zstd.ZstdOutputStream");
            LOGGER.info("[GT-PROBE] ZstdOutputStream loaded OK");
        } catch (Throwable t) {
            LOGGER.error("[GT-PROBE] ZstdOutputStream failed to load", t);
        }

        if (ModList.get().isLoaded("create")) {
            LOGGER.info("Detected Create, registering Registrate");

            GTCreateCompat.registrate()
                    .registerEventListeners(modEventBus);
            GTCreateItems.register();
        }

        if (ModList.get().isLoaded("create") && ModList.get().isLoaded("sophisticatedstorage")) {
            LOGGER.info("Detected create + sophisticatedstorage, registering contraption storage for SS containers");
            // 精致存储容器在移动结构上的挂载：补可打开的菜单（含非 9 的倍数槽位）与客户端渲染同步
            SSCreateCompat.register(modEventBus);
        }

        if (ModList.get().isLoaded("apotheosis_things")) {
            LOGGER.info("Detected apotheosis_things, registering compatibility content");

            NeoForge.EVENT_BUS.register(SalvageCharmEvent.class);
        }


        if (ModList.get().isLoaded("alshanex_familiars")) {
            LOGGER.info("Detected alshanex_familiars, registering compatibility content");
            AFAdditionalItems.register(modEventBus);
            AFAdditionalCreativeTabs.register(modEventBus);
            NeoForge.EVENT_BUS.register(InvertedFamiliarSpellbookEvent.class);
            NeoForge.EVENT_BUS.register(FamiliarProtectionEvent.class);
        }

        if (ModList.get().isLoaded("exspectriments")) {
            LOGGER.info("Detected exspectriments, registering compatibility content");
            modEventBus.register(ExspectrimentsClientCompatEvent.class);
        }

        if (ModList.get().isLoaded("traveloptics")
                && ModList.get().isLoaded("irons_spellbooks")
                && ModList.get().isLoaded("familiarslib")) {
            LOGGER.info("Detected traveloptics, protecting summons from friendly fire");
            NeoForge.EVENT_BUS.register(TravelOpticsSummonProtection.class);
        }

        if (ModList.get().isLoaded("kaleidoscope_cookery")) {
            LOGGER.info("Detected kaleidoscope_cookery, registering compatibility content");

            KaleidoCompat.register(modEventBus);
            NeoForge.EVENT_BUS.register(VillageGarbageStationAddition.class);
        }

        if (ModList.get().isLoaded("compactmachines")) {
            LOGGER.info("Detected compactmachines, registering compatibility content");
            CompactMachinesCompat.register(modEventBus);
            NeoForge.EVENT_BUS.register(CompactMachinesMobSpawnHandler.class);
        }

        if (ModList.get().isLoaded("cataclysm")) {
            LOGGER.info("Detected cataclysm, registering compatibility content");

            GTCataclysmCompat.register(modEventBus);
        }

        if (ModList.get().isLoaded("questshop")) {
            LOGGER.info("Detected questshop, registering compatibility content");

            QSCompat.register(modEventBus);
        }

        if (ModList.get().isLoaded("thaumcraft")) {
            LOGGER.info("Detected thaumcraft, registering compatibility content");

            GTThaumcraftCompat.register(modEventBus, container);
            NeoForge.EVENT_BUS.register(GTThaumcraftRecipeLoader.class);
            NeoForge.EVENT_BUS.register(BrainJarResearchEvent.class);
            if (ModList.get().isLoaded("ae2")) {
                NeoForge.EVENT_BUS.register(ThaumometerStorageScanQueue.class);
            }
        }

        if (ModList.get().isLoaded("lootr")) {
            LOGGER.info("Detected lootr, registering quick loot interaction");
            // 快速拾取：按住右键直接拿取 Lootr 容器内的战利品（见 compat/lootr/LootrQuickLootEvent）
            NeoForge.EVENT_BUS.register(LootrQuickLootEvent.class);
        }

        if (ModList.get().isLoaded("oneenoughitem")) {
            // 启动自检 OEI 统一规则是否生效，必要时触发一次重载自愈（见 compat/oei/OreUnificationGuard）
            NeoForge.EVENT_BUS.register(OreUnificationGuard.class);
        }

        if (ModList.get().isLoaded("createoreexcavation")) {
            LOGGER.info("Detected createoreexcavation, registering the golden vein finder and vein sharing tooltips");
            // 黄金探矿杖 + 钻井物品上的矿脉分享说明（见 compat/createoreexcavation）
            GTCreateOreExcavationItems.register(modEventBus);
            NeoForge.EVENT_BUS.register(GTCreateOreExcavationTooltip.class);
        }

        if (ModList.get().isLoaded("ftbchunks")) {
            LOGGER.info("Detected ftbchunks, enforcing claim protection for skill block breaking");
            // 技能自行构造并发布 BreakEvent（绕过原版破坏流程）时，仍按 FTB Chunks 规则复核（见 compat/ftbchunks/GTClaimProtection）
            NeoForge.EVENT_BUS.register(GTClaimProtection.class);
        }

        if (ModList.get().isLoaded("academy")) {
            LOGGER.info("Detected academy, granting fall immunity after storm_wing is unlocked");
            // 风暴之翼解锁后免疫坠落伤害（见 compat/academy/AcademyStormWingFallImmunity）
            NeoForge.EVENT_BUS.register(AcademyStormWingFallImmunity.class);
            // 超电磁炮的方块破坏分摊到多 tick 执行（见 compat/academy/GTRailgunDestruction）
            NeoForge.EVENT_BUS.register(GTRailgunDestruction.class);
        }

        if (ModList.get().isLoaded("academy") && ModList.get().isLoaded("thermal")) {
            LOGGER.info("Detected academy + thermal, allowing thermal coins to be fired by the railgun");
            // 热力硬币也能像学院币一样右键抛出并被超电磁炮发射（见 compat/academy/GTThermalCoinRailgun）
            NeoForge.EVENT_BUS.register(GTThermalCoinRailgun.class);
        }

        if (ModList.get().isLoaded("thaumicbases")) {
            NeoForge.EVENT_BUS.register(ThaumicCropHarvestEvent.class);
            // 进阶炼金炉的自动化：补上神秘基础学方块实体类型缺失的物品能力注册（见 compat/thaumicbases）
            modEventBus.addListener(GTThaumicBasesFurnaceCompat::registerCapabilities);
        }

        if (ModList.get().isLoaded("flavor_immersed_daily")) {
            LOGGER.info("Detected flavor_immersed_daily, restoring the teapot cover item removed upstream");
            // 该模组 2026.9.26 起删除了 teapotcover，这里按原命名空间补回，fidworkblock 茶壶才能加盖
            FidAdditionalItems.register(modEventBus);
        }

        if (ModList.get().isLoaded("thermal")) {
            LOGGER.info("Detected thermal, registering the additional integral components");
            // 整合组件原本只有 1~3 级，这里补上 4~6 级（见 compat/thermal/ThermalAdditionalItems）
            ThermalAdditionalItems.register(modEventBus);
        }

        if (ModList.get().isLoaded("thermal") && ModList.get().isLoaded("spectrum")) {
            LOGGER.info("Detected thermal + spectrum, registering the crystallarieum specialization augment");
            // 结晶器的「专业化升级：光谱墨法结晶」，装上后结晶器可运行光谱的结晶配方（见 compat/thermal/ThermalCrystallarieumAugment）
            ThermalCrystallarieumAugment.register(modEventBus);
        }

        if (ModList.get().isLoaded("appflux") && ModList.get().isLoaded("ae2helpers")) {
            LOGGER.info("Detected appflux + ae2helpers, merging the result-import card into the upgrade sidebar");
            modEventBus.addListener(AppliedFluxCompat::onCommonSetup);
        }

        if (ModList.get().isLoaded("academy")) {
            // 媒体播放器：删除曲目（从服务端 loadedMedia 移除）的网络包
            modEventBus.addListener(this::registerPayloads);
        }

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // 自定义早期窗口（Drippy 等）会锁死窗口尺寸，启动后按启动器的 --width/--height 纠正一次
        NeoForge.EVENT_BUS.register(ClientWindowSizeGuard.class);

        if (ModList.get().isLoaded("emi") && ModList.get().isLoaded("ftbquests")) {
            // 任务书 R/U 查配方：方法签名引用 EMI 类型，只能在 EMI + FTB Quests 都存在时加载该类，
            // 否则注解扫描/类加载会在未装 EMI 时抛 NoClassDefFoundError
            NeoForge.EVENT_BUS.register(QuestBookRecipeKeyHandler.class);
        }

        if (ModList.get().isLoaded("maidsoulcore")) {
            // 按客户端配置压制女仆核心 / ApricityUI 的日志（见 MaidLogSuppressor）
            MaidLogSuppressor.installIfPresent();
        }

        if (ModList.get().isLoaded("ftblibrary")) {
            // 期货机界面左上角已有自己的页签，FTB 侧边栏按钮叠在上面，登记进屏幕黑名单
            GTFtbSidebarBlacklist.install();
        }

        if (ModList.get().isLoaded("createdieselgenerators")) {
            LOGGER.info("Detected createdieselgenerators, delayed init");
            event.enqueueWork(CDGSpriteShifts::init);
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            NeoForge.EVENT_BUS.register(GTAnimatedIconAnimator.class);
            NeoForge.EVENT_BUS.register(GTThaumcraftClientData.class);
            // port.278 起守卫者玻璃改成烘焙模型（连接纹理走 ModelData，物品那一趟直接返回空），
            // 这里按旧版自绘连接纹理面，见 GTWardedGlassRenderer + WardedGlassBakedModelMixin
            NeoForge.EVENT_BUS.register(GTWardedGlassRenderer.class);
        }
        if (ModList.get().isLoaded("renderblender")) {
            LOGGER.info("Detected renderblender, registering compatibility content");
            NeoForge.EVENT_BUS.register(RenderBlenderCosmicQueueFlushHandler.class);
        }
        if (ModList.get().isLoaded("mcwifipnp") && ModList.get().isLoaded("zstdnet")) {
            LOGGER.info("Detected mcwifipnp + zstdnet, relocating the share-to-lan zstd port field");
            // zstdnet 的 zstd 端口框会贴到 mcwifipnp 的第一标签页控件上且不随标签页切换，
            // 这里把它移到所有设置项下方且只在第一个标签页显示（见 McWifiPnPZstdPortLayout）
            NeoForge.EVENT_BUS.register(McWifiPnPZstdPortLayout.class);
        }

        if (ModList.get().isLoaded("astralsorcery")) {
            LOGGER.info("Detected astralsorcery, drawing focal point light columns with a vanilla render type");
            // AS 2.0.1 的降星点光柱走自定义 core shader + 自定义 RenderType，在部分整合包里画不出像素
            // （对象生成/alpha 都正常，就是没有可见结果），这里用原版渲染类型自绘一根（见 GTFocalPointColumnRenderer）
            NeoForge.EVENT_BUS.register(GTFocalPointColumnRenderer.class);
        }

        if (ModList.get().isLoaded("academy")) {
            // 媒体播放器：播放模式（单曲/列表/随机）与「删除歌曲」状态，以及列表/随机模式的自动切歌
            NeoForge.EVENT_BUS.register(GMMediaPlayerState.class);
        }

    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(GTMediaUnloadPacket.TYPE, GTMediaUnloadPacket.STREAM_CODEC, GTMediaUnloadPacket::handle);
    }
}

package net.fodoth.skina.goldentweaks;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import com.mojang.logging.LogUtils;
import net.fodoth.skina.goldentweaks.compat.cataclysm.GTCataclysmCompat;
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
import net.fodoth.skina.goldentweaks.compat.alshanex_familiars.AFAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.flavorimmerseddaily.FidAdditionalItems;
import net.fodoth.skina.goldentweaks.compat.create.GTCreateCompat;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SSCreateCompat;
import net.fodoth.skina.goldentweaks.event.SalvageCharmEvent;
import net.fodoth.skina.goldentweaks.compat.lootr.LootrQuickLootEvent;
import net.fodoth.skina.goldentweaks.compat.traveloptics.TravelOpticsSummonProtection;
import net.fodoth.skina.goldentweaks.compat.mcwifipnp.McWifiPnPZstdPortLayout;
import net.fodoth.skina.goldentweaks.compat.oei.OreUnificationGuard;
import net.fodoth.skina.goldentweaks.compat.ftbchunks.GTClaimProtection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
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

        if (ModList.get().isLoaded("traveloptics")) {
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

        if (ModList.get().isLoaded("ftbchunks")) {
            LOGGER.info("Detected ftbchunks, enforcing claim protection for skill block breaking");
            // 技能自行构造并发布 BreakEvent（绕过原版破坏流程）时，仍按 FTB Chunks 规则复核（见 compat/ftbchunks/GTClaimProtection）
            NeoForge.EVENT_BUS.register(GTClaimProtection.class);
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

        if (ModList.get().isLoaded("appflux") && ModList.get().isLoaded("ae2helpers")) {
            LOGGER.info("Detected appflux + ae2helpers, merging the result-import card into the upgrade sidebar");
            modEventBus.addListener(AppliedFluxCompat::onCommonSetup);
        }

        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // 自定义早期窗口（Drippy 等）会锁死窗口尺寸，启动后按启动器的 --width/--height 纠正一次
        NeoForge.EVENT_BUS.register(ClientWindowSizeGuard.class);

        if (ModList.get().isLoaded("maidsoulcore")) {
            // 按客户端配置压制女仆核心 / ApricityUI 的日志（见 MaidLogSuppressor）
            MaidLogSuppressor.installIfPresent();
        }

        if (ModList.get().isLoaded("createdieselgenerators")) {
            LOGGER.info("Detected createdieselgenerators, delayed init");
            event.enqueueWork(CDGSpriteShifts::init);
        }
        if (ModList.get().isLoaded("thaumcraft")) {
            NeoForge.EVENT_BUS.register(GTAnimatedIconAnimator.class);
            NeoForge.EVENT_BUS.register(GTThaumcraftClientData.class);
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

    }
}

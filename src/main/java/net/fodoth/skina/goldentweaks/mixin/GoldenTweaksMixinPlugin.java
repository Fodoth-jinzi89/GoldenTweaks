package net.fodoth.skina.goldentweaks.mixin;

import net.fodoth.skina.goldentweaks.compat.carryon.CarryOnAeroCompatASM;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.Ae2ApuASM;
import net.fodoth.skina.goldentweaks.compat.appliedkjs.AppliedKjsASM;
import net.fodoth.skina.goldentweaks.compat.exspectriments.ExspectrimentsASM;
import net.fodoth.skina.goldentweaks.compat.ftbquests.FTBQuestsLangSplitterASM;
import net.fodoth.skina.goldentweaks.compat.linearbearing.LinearbearingASM;
import net.fodoth.skina.goldentweaks.compat.ldlib2.Ldlib2ASM;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;

import java.util.List;
import java.util.Set;

import static net.fodoth.skina.goldentweaks.util.ModPresent.checkIfPresent;

public class GoldenTweaksMixinPlugin implements IMixinConfigPlugin {

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        // 万法皆通 mixin 写错
        if (mixinClassName.contains("com.github.yimeng261.maidspell.mixin.tlm.MaidFeedOwnerTaskMixin")) {
            return false;
        }

        if (mixinClassName.contains("com.konrados.ramization.mixin.MixinTextureAtlas")) {
            return false;
        }

        if (mixinClassName.contains("io.redspace.ironsjewelry.mixin.VillagerMixin")) {
            return false;
        }

        // 这些 mixin 注入 iron's spellbooks 的召唤物目标逻辑，并引用 alshanex 魔宠 / familiarslib 的类型。
        // 只要 iron 存在这些 mixin 就会尝试应用，缺 familiarslib 时会在应用阶段抛 NoClassDefFoundError，
        // 因此三个依赖必须同时存在。
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.traveloptics")) {
            return isModLoaded("traveloptics")
                    && isModLoaded("irons_spellbooks")
                    && isModLoaded("familiarslib");
        }

        // additional_attributes 的 SpellUtils 覆盖仅在 iron's spellbooks 存在时才有意义
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.irons_spellbooks")) {
            return isModLoaded("irons_spellbooks")
                    && checkIfPresent("de.cadentem.additional_attributes.compat.irons_spellbooks.SpellUtils");
        }

        // alshanex 魔宠相关 mixin 需要 familiarslib，属性桥接还需要 iron's spellbooks 的属性注册表
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.alshanex_familiars")) {
            return isModLoaded("alshanex_familiars")
                    && isModLoaded("familiarslib")
                    && isModLoaded("irons_spellbooks");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.ae_better_villagers")) {
            return isModLoaded("ae2cs") && isModLoaded("neoecoae") && isModLoaded("extendedae") && isModLoaded("ae2helpers") && isModLoaded("packagedauto");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.compactmachines")) {
            return isModLoaded("compactmachines");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.compactmachines")) {
            return isModLoaded("compactmachines");
        }

        // Create 搅拌机（机械搅拌器）自动执行无序合成配方时，DummyCraftingContainer 未提供
        // 宽/高/物品，asCraftInput() 会返回空输入，getRemainingItems 因此丢失合成余留物
        // （例如全矿矿物模组的矿锤会被整把消耗，而不是仅损耗 1 点耐久）
        if (mixinClassName.endsWith("fix.create.BasinRecipeMixin")) {
            return isModLoaded("create");
        }

        // Create 6.0.10 连续碰撞：实体包围盒中心与装置碰撞箱中心完全重合（零分离退化）时
        // ContinuousSeparationManifold.axis / normalAxis 保持 null，collideMany 读取时 NPE 崩溃
        if (mixinClassName.endsWith("fix.create.ContinuousSeparationManifoldMixin")) {
            return isModLoaded("create");
        }

        // Create 溜槽护目镜用无参描述 id 翻译被运输物品名，精妙存储等含 %s 占位的名字会残留字面 %s
        if (mixinClassName.endsWith("fix.create.ChuteGoggleItemNameMixin")) {
            return isModLoaded("create");
        }

        // 移动结构上的挂载容器菜单只接受 9 的倍数槽位，限量木桶等 1~8 槽容器无法右键打开
        if (mixinClassName.endsWith("fix.create.MountedStorageMenusMixin")) {
            return isModLoaded("create");
        }

        // 挂载存储 read 时补建精致存储影子 BE（需要 Provider 装载升级/设置 NBT）
        if (mixinClassName.endsWith("fix.create.MountedStorageManagerMixin")) {
            return isModLoaded("create") && isModLoaded("sophisticatedstorage");
        }

        // 移动结构上 SS 容器的手持物品交互（开口面装升级/存物）
        if (mixinClassName.endsWith("fix.create.AbstractContraptionEntityInteractionMixin")) {
            return isModLoaded("create") && isModLoaded("sophisticatedstorage");
        }

        // 装置上应用 SS tier 升级需要读取 SS 的私有「tier 升级 → 目标方块」映射
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.compat.sophisticatedstorage")) {
            return isModLoaded("create") && isModLoaded("sophisticatedstorage");
        }

        // Sable 单方块子关卡的渲染路径不调用方块模型的 getModelData 钩子，
        // 精致存储等把逐格显示物烘进模型的模组会丢失物品图标（只剩 BE 数量显示）
        if (mixinClassName.endsWith("fix.sable.SingleBlockSubLevelModelDataMixin")) {
            return isModLoaded("sable");
        }

        // CoFH Core 处理工具提示首行时对 siblings 直接 removeIf，AE2 等界面传入的是不可变列表，
        // 会抛 UnsupportedOperationException 导致渲染线程崩溃
        if (mixinClassName.endsWith("fix.cofh_core.CoreClientEventsMixin")) {
            return isModLoaded("cofh_core");
        }

        // steampowered 的金属齿轮把 Flywheel 视觉注册成了 SingleAxisRotatingVisual.shaft，
        // 方块形态只渲染杆子、不渲染轮盘；改用齿轮自身的烘焙模型
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.steampowered")) {
            return isModLoaded("steampowered");
        }

        // Mekanism solar/wind machines running inside Compact Machines dimensions
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.mekanism.")) {
            if (mixinClassName.endsWith("MekMMSolarCheckMixin")
                    || mixinClassName.endsWith("MekMMLargeSolarNeutronActivatorMixin")) {
                return isModLoaded("mekmm") && isModLoaded("mekanism") && isModLoaded("compactmachines");
            }
            // Mekanism Advanced Generators ships its own solar panel copies in com.pocky.solarpanels
            if (mixinClassName.endsWith("SolarPanelsSolarCheckMixin")
                    || mixinClassName.endsWith("SolarPanelsSolarGeneratorMixin")) {
                return isModLoaded("mekanismadvancedgenerators") && isModLoaded("compactmachines");
            }
            return isModLoaded("mekanism") && isModLoaded("mekanismgenerators") && isModLoaded("compactmachines");
        }

        // evolvedmekanism 的高级工厂槽位容量对齐 mekanism_extras（突破 64 堆叠上限）
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.balance.mekanism")) {
            return isModLoaded("mekanism");
        }

        if (mixinClassName.endsWith("fix.watut.PlayerStatusManagerClientMixin")) {
            return isModLoaded("watut");
        }

        if (mixinClassName.endsWith("fix.watut.PacketNBTFromServerMixin")) {
            return isModLoaded("watut");
        }

        if (mixinClassName.endsWith("fix.emi.ItemEmiStackTooltipMixin")
                || mixinClassName.endsWith("fix.emi.EmiTagsRawValuesMixin")) {
            return isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.emi.EmiSearchMixin")
                || mixinClassName.endsWith("fix.emi.EmiSearchCompiledQueryMixin")) {
            return isModLoaded("emi");
        }

        // emi_ores 的生物群系堆栈在退出世界后（无 level）烘焙搜索时访问注册表 NPE
        if (mixinClassName.endsWith("fix.emi_ores.BiomeEmiStackNullLevelMixin")) {
            return isModLoaded("emi_ores") && isModLoaded("emi");
        }

        // wcwt 的配方页「填充物品」按钮由 EMI 的 recipe decorator 注入，
        // 而 EMI 在正式客户端默认不应用 decorator，需要 wcwt 存在时恢复
        if (mixinClassName.endsWith("fix.emi.EmiRecipeDecoratorsMixin")) {
            return isModLoaded("emi") && isModLoaded("wcwt");
        }

        // wcwt 的「填充物品」锤子按钮落点：EMI 三按钮竖向排列时不应另开一列
        if (mixinClassName.endsWith("fix.wcwt.WcwtEmiPullButtonPositionMixin")) {
            return isModLoaded("emi") && isModLoaded("wcwt");
        }

        // SilentGear 的预制工具（GearItem）从 EMI 索引中隐藏
        if (mixinClassName.endsWith("fix.silentgear.SilentGearEmiHiderMixin")) {
            return isModLoaded("silentgear") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.oneenoughitem.UtilsMixin")
                || mixinClassName.endsWith("fix.oneenoughitem.RawTagCaptureMixin")
                || mixinClassName.endsWith("shut.oei.OneenoughitemMixin")) {
            return isModLoaded("oneenoughitem");
        }

        if (mixinClassName.endsWith("fix.kaleidoscope.SoupBaseManagerMixin")) {
            return isModLoaded("kaleidoscope_cookery");
        }

        if (mixinClassName.endsWith("fix.tradecycling.MerchantScreenTradeCyclingMixin")) {
            return isModLoaded("trade_cycling");
        }

        if (mixinClassName.endsWith("fix.krypton.MinecraftCompressDecoderMixin")) {
            return isModLoaded("krypton");
        }

        if (mixinClassName.endsWith("fix.carryon.client.CarriedObjectRenderMixin")
                || mixinClassName.endsWith("fix.carryon.client.CarryRenderHelperMixin")) {
            return isModLoaded("carryon");
        }

        if (mixinClassName.endsWith("fix.ramization.SmartGCSchedulerMixin")) {
            return isModLoaded("ramization");
        }

        // create_connected refreshes item visibility through the JEI runtime API, which TMRV locks too early
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.createconnected")) {
            return isModLoaded("toomanyrecipeviewers")
                    && checkIfPresent("com.hlysine.create_connected.compat.CreateConnectedJEI");
        }

        if (mixinClassName.endsWith("fix.supplementaries.ConfigButtonMixin")) {
            return isModLoaded("supplementaries");
        }

        // Language Reload 的 separateTranslations 未初始化时，Entity.toString 在区块生成线程等
        // 非客户端线程上会抛 NPE
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.languagereload")) {
            return isModLoaded("languagereload");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.iceberg")) {
            return isModLoaded("iceberg") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.neoguanniao.client.BirdMovementControllerMixin")) {
            return isModLoaded("neoguanniao");
        }

        if (mixinClassName.endsWith("fix.ftbquests.RewardTypesMixin")) {
            return isModLoaded("ftbquests");
        }

        // 任务书章节图片：解锁后立即显示、渲染真实物品（默认 tooltip + R/U 查配方）、子任务跟随根任务完成
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.ftbquests")) {
            return isModLoaded("ftbquests");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.buildingwands")) {
            return isModLoaded("wands");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2peat")) {
            return isModLoaded("ae2peat");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.hazennstuff")) {
            return isModLoaded("hazennstuff");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.advanced_infusion")) {
            return isModLoaded("advanced_infusion");
        }

        // Astral Sorcery 2.0.1：创意标签在专用服务器上会崩，降星点 tick 会 CME，新生成区块的降星点需要补激活
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.astralsorcery")) {
            return isModLoaded("astralsorcery");
        }

        // Biomes O' Plenty 的「异常方块」（错误方块）：限制为只随机原版方块并降速
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.biomesoplenty")) {
            return isModLoaded("biomesoplenty");
        }

        if (mixinClassName.endsWith("fix.ae2helpers.WcwtEmiRecipeHandlerMixin")) {
            return isModLoaded("ae2helpers") && isModLoaded("wcwt") && isModLoaded("emi");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.compat.aiimprovements")) {
            return isModLoaded("aiimprovements");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers.")) {
            return isModLoaded("ae2helpers") && isModLoaded("wcwt");
        }

        // AppliedFlux 的感应卡侧栏与 ae2helpers 的导入卡：把导入槽并入感应卡侧栏
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.appflux.")) {
            return isModLoaded("appflux") && isModLoaded("ae2helpers");
        }

        // extendedae_plus 给样板供应器的 AppliedFlux 升级栏少分配 1 格，这里补上
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.extendedaeplusslots.")) {
            return isModLoaded("extendedae_plus") && isModLoaded("appflux") && isModLoaded("ae2helpers");
        }

        // GoldenTweaks 接管的「合成结果导入卡」逻辑（方块形 + 面板形）
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2helpersblock.")) {
            return isModLoaded("ae2helpers") && isModLoaded("appflux");
        }

        if (mixinClassName.endsWith("fix.ae2.NameSearchPredicateMixin")) {
            return isModLoaded("ae2");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2.")) {
            return isModLoaded("ae2") && isModLoaded("emi");
        }

        // neoecoae 的「收藏缺失」按钮走 JEI 内部类，纯 EMI 下不可用，改接 EMI 收藏夹
        if (mixinClassName.endsWith("fix.neoecoae.ECOCraftConfirmScreenBookmarkMixin")) {
            return isModLoaded("neoecoae") && isModLoaded("emi");
        }

        // AEQC 的「收藏缺失项」按钮同样走 JEI 内部类，纯 EMI 下不可用，改接 EMI 收藏夹
        if (mixinClassName.endsWith("fix.aeqc.BookmarkMissingButtonMixin")) {
            return isModLoaded("aeqc") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.thaumcraft.ThaumatoriumOutputMixin")) {
            return isModLoaded("thaumcraft") && isModLoaded("ae2");
        }

        // Sodium 渲染方块实体时会把破坏裂纹一并套到方块实体几何上，结晶器上方的「魔法阵」正是方块实体渲染的，
        // 于是被挖时也被盖裂纹（原版本只给方块模型铺裂纹）。对结晶器跳过该装饰。
        if (mixinClassName.endsWith("fix.sodium.SodiumCrystallarieumBreakingOverlayMixin")) {
            return isModLoaded("sodium")
                    && checkIfPresent("net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.renderblender.")) {
            if (!isModLoaded("renderblender")) {
                return false;
            }
            if (mixinClassName.endsWith("IrisCompatMixin")
                    || mixinClassName.endsWith("CosmicRenderCallMixin")) {
                return isModLoaded("iris");
            }
            return true;
        }

        if (mixinClassName.endsWith("fix.ae2autopatternupload.AEBaseScreenMixin")) {
            return isModLoaded("ae2_auto_pattern_upload") && isModLoaded("neoecoae");
        }

        if (mixinClassName.endsWith("fix.ae2autopatternupload.ProviderSelectScreenMixin")
                || mixinClassName.endsWith("fix.ae2autopatternupload.accessor.ProviderGroupAccessor")
                || mixinClassName.endsWith("fix.ae2autopatternupload.ProvidersListS2CPacketDummyMixin")) {
            return isModLoaded("ae2_auto_pattern_upload");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.linearbearing")) {
            return isModLoaded("linearbearing");
        }

        // ldlib2：客户端专属的 ItemSlot#xeiPhantom 在专用服务器上会加载 Renderable 崩服
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ldlib2")) {
            return isModLoaded("ldlib2");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.lzxnonefate")) {
            return isModLoaded("lzxnonefate");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2autopatternupload")) {
            return isModLoaded("ae2_auto_pattern_upload") && isModLoaded("ae2peat");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.aeronautics_curios_compat")) {
            return isModLoaded("aeronautics_curios_compat");
        }

        if (mixinClassName.endsWith("fix.extendedae_plus.JeiRuntimeCompatMixin")) {
            return isModLoaded("extendedae_plus") && isModLoaded("toomanyrecipeviewers");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus")) {
            return isModLoaded("extendedae_plus") && isModLoaded("extendedae") && isModLoaded("patternbetter");
        }

        if (mixinClassName.endsWith("fix.neoecoae.NEExtraModelsMixin")) {
            return checkIfPresent("cn.dancingsnow.neoecoae.client.all.NEExtraModels");
        }

        // 深海任务界面在 create_submarine 更新后由 WelcomeScreen 改名为 UpdateScreen
        if (mixinClassName.endsWith("fix.createsubmarine.DeepSeasUpdateScreenMixin")) {
            return checkIfPresent("com.maxenonyme.createsubmarine.submarine.client.DeepSeasUpdateScreen");
        }

        // 深海：进入主菜单时的光敏性安全须知界面
        if (mixinClassName.endsWith("fix.createsubmarine.DeepSeasSafetyScreenMixin")) {
            return checkIfPresent("com.maxenonyme.createsubmarine.submarine.client.DeepSeasSafetyScreen");
        }

        // 深海：首次进入主菜单时的水压模型选择界面（不询问，保持配置默认值）
        if (mixinClassName.endsWith("fix.createsubmarine.PressureModelScreenMixin")) {
            return checkIfPresent("com.maxenonyme.createsubmarine.submarine.client.PressureModelScreen");
        }

        // 深海：主界面/暂停界面上 Create 菜单按钮下方那颗「深海设置」按钮
        if (mixinClassName.endsWith("fix.createsubmarine.DeepSeasMenuButtonMixin")) {
            return checkIfPresent("com.maxenonyme.createsubmarine.submarine.client.DeepSeasMenuButton");
        }

        if (mixinClassName.endsWith("fix.ae2cs.AECSAdditionalModelsMixin")) {
            return checkIfPresent("io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels");
        }

        // ae2cs 熵变反应室加速卡过多时卡住：钳制有效速度倍率
        if (mixinClassName.endsWith("fix.ae2cs.EntropyReactionChamberSpeedMixin")) {
            return isModLoaded("ae2cs");
        }

        // ae2cs 机器输入槽包装器「模拟抽取有、实际抽取无」，Create 溜槽等外部物流按模拟结果取物会无限复制
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2cs.")) {
            return isModLoaded("ae2cs");
        }

        if (mixinClassName.endsWith("fix.fancymenu.MainThreadTaskExecutorMixin")) {
            return isModLoaded("fancymenu");
        }

        if (mixinClassName.endsWith("fix.tmrv.JEIPluginManagerMixin")) {
            return isModLoaded("toomanyrecipeviewers");
        }

        // PackagedAuto 的 JEI 子类型解释器对不带数据组件的裸 package 物品
        // 调用 List.of(null, null) 会 NPE，需在 JEI/TMRV 存在时修复
        if (mixinClassName.endsWith("fix.packagedauto.PackageItemSubtypeInterpreterMixin")) {
            return isModLoaded("packagedauto") && (isModLoaded("jei") || isModLoaded("toomanyrecipeviewers"));
        }

        if (mixinClassName.endsWith("fix.papi.ClientWorldLeaveCleanupMixin")) {
            return isModLoaded("papi");
        }

        // TMRV 的滚动网格/滑条与九宫格绘制：修复滚轮不滚动（被 EMI 当翻页）与滑条贴图错位
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.tmrv")) {
            if (mixinClassName.endsWith("TmrvRecipeScreenScrollMixin")) {
                return isModLoaded("emi") && isModLoaded("toomanyrecipeviewers");
            }
            return isModLoaded("toomanyrecipeviewers");
        }

        // EMI 的 TooltipQuery#getText 没判 getTooltipText() 的 null（有些模组会返回 null）
        if (mixinClassName.endsWith("fix.emi.TooltipQueryMixin")) {
            return isModLoaded("emi");
        }

        // EMI 重载阶段那行红字「延迟较大」：它按 10~500ms 的 worry 判定，我们这边合法阶段是秒级
        if (mixinClassName.endsWith("fix.emi.EmiReloadWorryMixin")) {
            return isModLoaded("emi");
        }

        // 星辉的流明配方会被重复注册，TMRV 的 Category$Recipe#setPlugin 抛裸异常把它们丢掉
        if (mixinClassName.endsWith("fix.tmrv.TmrvRecipeSetPluginMixin")) {
            return isModLoaded("toomanyrecipeviewers");
        }

        // 神秘时代 / scex 要素扫描：跳过对全部三万多物品逐个做的无用 getUidForStack 探错
        if (mixinClassName.endsWith("fix.thaumcraft.ThaumcraftJeiPluginFastScanMixin")) {
            return isModLoaded("thaumcraft") && (isModLoaded("jei") || isModLoaded("toomanyrecipeviewers"));
        }

        if (mixinClassName.endsWith("fix.scexaspect.AspectCatalogMixin")) {
            return isModLoaded("scex_aspect_jei") && isModLoaded("thaumcraft");
        }

        if (mixinClassName.endsWith("shut.AEKeyLegacyComponentLoggerMixin")) {
            return isModLoaded("ae2") && isModLoaded("northstar");
        }

        if (mixinClassName.endsWith("fix.ponder.BakedModelBuffererImplMixin")) {
            return checkIfPresent("net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl");
        }

        // PonderForKaleidoscopeCookery 用旧版 3 参 RecipeRecord 构造器
        // （Kaleidoscope Cookery 1.5.1 已加 flexRecipe 参数），按 W 思索菜谱会 NoSuchMethodError 崩溃
        if (mixinClassName.endsWith("fix.ponderforkc.RecipeItemScenesMixin")) {
            return isModLoaded("ponderforkc");
        }

        // 聚变反应堆的储罐容量只在「燃烧速率变化」时重算，刚成型 / 重新加载后 maxWater 保持 0，
        // 水进不去（上游 mekanism/Mekanism#8281）。成型与速率应用后强制重算一次。
        if (mixinClassName.endsWith("fix.mek.FusionReactorWaterCapacityMixin")) {
            return isModLoaded("mekanism") && isModLoaded("mekanismgenerators");
        }

        // 物流管道在途 stack / 流向预测缓存上限：防止超大堆叠容器把管道 tick 拖成无限计算并 OOM
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.mekanism.")) {
            return isModLoaded("mekanism");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.mekanismextras")) {
            return isModLoaded("mekanism_extras") && isModLoaded("mekmm") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.ftbquests.TranslationManagerDummyMixin")) {
            return isModLoaded("ftbquests") && isModLoaded("ftbquestslangsplitter");
        }

        if (mixinClassName.endsWith("fix.ftbquests.ChapterImageConfigGroupDummyMixin")) {
            return checkIfPresent("dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3")
                    && checkIfPresent("ru.hollowhorizon.additions.questing.mixins.ChapterImageConfigGroupMixin");
        }

        // AcceleratedRendering 的 FTB Library 界面批处理会打乱任务书/配置编辑界面的渲染次序、
        // 裁剪与物品打光（物品光照丢失、模型溢出外框、矩形缺失、文字发虚）；
        // 安装任务书时关闭该特性，让 FTB 界面回退原版管线
        if (mixinClassName.endsWith("fix.ftbquests.FtbAccelerationDisableMixin")) {
            return isModLoaded("ftbquests")
                    && checkIfPresent("com.github.argon4w.acceleratedrendering.features.mods.ModsFeature");
        }

        if (mixinClassName.endsWith("fix.carryon.PickupHandlerDummyMixin")) {
            return isModLoaded("carryon") && isModLoaded("carryonaerocompat");
        }

        // c2me OpenCL 加速在屏幕底部显示的 "Compiling ..." 进度文本：吞掉写入，保持 PROGRESS_TEXT 为 null
        if (mixinClassName.endsWith("shut.C2meOpenClProgressOverlayMixin")) {
            return checkIfPresent("com.ishland.c2me.opts.accel.opencl.common.gen.OpenCLDevice");
        }

        // c2me 的“实验性版本”警告界面：启动时自动点掉
        if (mixinClassName.endsWith("shut.C2meExperimentalWarningScreenMixin")) {
            return checkIfPresent("com.ishland.c2me.base.common.client_warning.ModExperimentalWarningScreen");
        }

        // 对于原版类的 mixin 应启用模组检查
        if (mixinClassName.contains("net.fodoth.skina.goldentweaks.mixin.shut.TouhouLostMaidLoggerMixin")) {
            return isModLoaded("touhou_lost_maid");
        }

        if (mixinClassName.endsWith("shut.FlavorImmersedDailyLoggerMixin")) {
            return isModLoaded("flavor_immersed_daily");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.touhoulostmaid")) {
            return isModLoaded("touhou_little_maid") && isModLoaded("touhou_lost_maid");
        }

        // 女仆照片/智能板放置：备份复原的女仆 MAID_INFO 缺少 Owner 时 getUUID 会 NPE
        if (mixinClassName.endsWith("fix.touhoulittlemaid.AbstractStoreMaidItemMixin")) {
            return isModLoaded("touhou_little_maid");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily")) {
            return isModLoaded("flavor_immersed_daily");
        }

        if (mixinClassName.endsWith("fix.ftbultimine.VanillaCropLikeHandlerMixin")) {
            return isModLoaded("ftbultimine") && isModLoaded("supplementaries") && isModLoaded("thaumicbases");
        }

        if (mixinClassName.endsWith("fix.thaumcraft.ThaumcraftOverworldBiomesMixin")) {
            return isModLoaded("thaumcraft");
        }

        if (mixinClassName.endsWith("fix.thaumcraft.AuraNodeHungryMixin")) {
            return isModLoaded("thaumcraft");
        }

        if (mixinClassName.endsWith("fix.thaumcraft.AuraNodeBiomeConversionMixin")
                || mixinClassName.endsWith("fix.thaumcraft.ThaumcraftBiomeMutationMixin")) {
            return isModLoaded("thaumcraft");
        }

        // references Thaumic Bases' redlon stem block
        if (mixinClassName.endsWith("fix.thaumcraft.CropUtilsMixin")) {
            return isModLoaded("thaumcraft") && isModLoaded("thaumicbases");
        }

        if (mixinClassName.endsWith("feature.thaumcraft.ThaumcraftGolemEntityMixin")
                || mixinClassName.endsWith("feature.thaumcraft.GolemConfigRuntimeMixin")
                || mixinClassName.endsWith("feature.thaumcraft.GolemHarvestGoalMixin")
                || mixinClassName.endsWith("feature.thaumcraft.GolemReturnHomeGoalMixin")) {
            return isModLoaded("thaumcraft");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumcraftcelestial")) {
            return isModLoaded("thaumcraftcelestial");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumicbases")) {
            if (mixinClassName.endsWith("ThaumicCropFarmlandMixin")) {
                return isModLoaded("thaumicbases") && isModLoaded("farmersdelight");
            }
            return isModLoaded("thaumicbases");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.thaumichorizons")) {
            return isModLoaded("thaumichorizons");
        }

        // 神秘能源：奥术合成终端接 Polymorph 需两者都在；源质元件工作台的升级槽修复只需 thaumicenergistics
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumicenergistics")) {
            if (mixinClassName.endsWith("ArcaneCraftingTerminalMixin")) {
                return isModLoaded("thaumicenergistics") && isModLoaded("polymorph");
            }
            return isModLoaded("thaumicenergistics");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumictinkerer")) {
            return isModLoaded("thaumic_tinkerer");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons")) {
            return isModLoaded("thaumichorizons");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thermal")) {
            return isModLoaded("thermal");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.patchouli")) {
            return isModLoaded("patchouli");
        }

        // Polymorph 的 JEI 插件引用打包内 JEI 19.x 不存在的 IRecipeTransferListener，加载即抛
        // NoClassDefFoundError。保留插件并让其在 JEI 19.x 上工作（转移后选中配方）。
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.polymorph")) {
            if (mixinClassName.endsWith("RecipeTransferUtilMixin")) {
                return isModLoaded("polymorph") && (isModLoaded("jei") || isModLoaded("toomanyrecipeviewers"));
            }
            return isModLoaded("polymorph");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.balance.irons_jewelry")) {
            return isModLoaded("irons_jewelry");
        }

        // aeallpattern 的聚合样板展开缓存（匿名 LinkedHashMap 子类）上限过高，单独降档
        if (mixinClassName.endsWith("fix.aeallpattern.AeAllPatternCacheCapMixin")) {
            return checkIfPresent("io.github.langqi99.aeallpattern.aggregate.AggregatePatternExpander$1");
        }

        // 烘焙坊发酵罐只认本模组的全麦面粉，物品统一后需同时接受 OEI 替换后的物品
        if (mixinClassName.endsWith("fix.bakeries.FermentationTankBlockMixin")) {
            return isModLoaded("oneenoughitem")
                    && checkIfPresent("com.renyigesai.bakeries.common.blocks.FermentationTankBlock");
        }

        // mekanicalcreate 的候选配方目录内层 HashMap 无上限，替换为有界 LRU
        if (mixinClassName.endsWith("fix.mekanicalcreate.MekanicalCreateCandidateCacheMixin")) {
            return checkIfPresent("io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver");
        }

        // mekanicalcreate 的两趟配方目录预计算（11.1s）提前到重载起步时预热
        if (mixinClassName.endsWith("fix.mekanicalcreate.MekanicalCreateDeferredRecipesMixin")) {
            return checkIfPresent("io.github.langqi99.mekanicalcreate.content.SimulationRecipeResolver");
        }

        if (mixinClassName.endsWith("fix.mekanicalcreate.EmiReloadPrewarmMixin")) {
            return isModLoaded("emi");
        }

        // AcademyCraft 技能伤害按寂静装备「魔法攻击力」加成
        if (mixinClassName.endsWith("feature.academy.AcademyDamageHelperMixin")) {
            return isModLoaded("academy");
        }

        // AcademyCraft 音效未成功注册时 AbilityLoopSoundManager.tick 访问未绑定 Holder 会崩溃
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.academy")) {
            return isModLoaded("academy");
        }

        // zstdnet：端口标签重定位需要 mcwifipnp 的界面；聊天提示关闭只依赖 zstdnet
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.zstdnet.")) {
            if (mixinClassName.endsWith("ZstdShareToLanPortLabelMixin")) {
                return isModLoaded("zstdnet") && isModLoaded("mcwifipnp");
            }
            return isModLoaded("zstdnet");
        }

        // applied_kjs 的 AppliedKJSServerState 共享 HashMap/HashSet 被区块加载线程与 tick 线程并发访问会 CME
        if (mixinClassName.endsWith("fix.appliedkjs.AppliedKjsServerStateDummyMixin")) {
            return isModLoaded("applied_kjs");
        }

        // 女仆核心：禁用首次进入世界的教程自动弹窗
        if (mixinClassName.endsWith("fix.maidsoulcore.NoviceTutorialAutoPopupMixin")) {
            return isModLoaded("maidsoulcore");
        }

        // 女仆核心：K/V 快捷键只在游戏内触发（打开界面时不响应）
        if (mixinClassName.endsWith("fix.maidsoulcore.AuiConsoleKeyHandlerClientInputMixin")) {
            return isModLoaded("maidsoulcore");
        }

        // 女仆核心：心契誓约大模型连接总开关（客户端配置，配置未加载时自动放行）
        if (mixinClassName.endsWith("fix.maidsoulcore.MaidSoulOwnerChatPacketMixin")
                || mixinClassName.endsWith("fix.maidsoulcore.MaidSoulRuntimeMixin")
                || mixinClassName.endsWith("fix.maidsoulcore.OpenAiCompatibleClientMixin")) {
            return isModLoaded("maidsoulcore");
        }

        // 女仆核心 / 心契誓约：快捷键在非本模组界面中不再触发
        if (mixinClassName.endsWith("fix.maid.KeyMappingMaidHotkeyScreenGuardMixin")) {
            return isModLoaded("maidsoulcore") || isModLoaded("maidmarriage");
        }

        // 女仆核心 AUI 控制台输入法：把 ApricityUI 界面加入 IMBlocker 的白名单
        if (mixinClassName.endsWith("fix.imblocker.IMBlockerConfigMixin")) {
            return isModLoaded("imblocker");
        }

        // REMI（Reliable EMI）给 EMI 注入的「工作站」侧栏在酿造台界面会额外画出酿造列表并挤占 GUI
        if (mixinClassName.endsWith("fix.remi.RemiBrewingWorkstationSidebarMixin")) {
            return isModLoaded("remi") && isModLoaded("emi");
        }

        return true;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {

        ExspectrimentsASM.patch(targetClassName, targetClass);
        AppliedKjsASM.patch(targetClassName, targetClass);
        CarryOnAeroCompatASM.patch(targetClassName, targetClass);
        LinearbearingASM.patch(targetClassName, targetClass);
        Ae2ApuASM.patch(targetClassName, targetClass);
        Ldlib2ASM.patch(targetClassName, targetClass);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith("fix.ae2autopatternupload.AEBaseScreenMixin")) {
            targetClass.methods.stream()
                    .filter(method -> method.name.equals("ae2apu$attachButton")
                            || method.name.endsWith("$extendedae_plus$eap$addUploadButton")
                            || method.name.endsWith("$extendedae_plus$eap$ensureUploadButton"))
                    .forEach(method -> {
                        method.instructions.clear();
                        method.tryCatchBlocks.clear();
                        method.localVariables = null;
                        method.visibleLocalVariableAnnotations = null;
                        method.invisibleLocalVariableAnnotations = null;
                        method.instructions.add(new InsnNode(Opcodes.RETURN));
                    });
        }

        if (mixinClassName.endsWith("fix.extendedae_plus.AEBaseScreenMixin")) {
            targetClass.methods.stream()
                    .filter(method -> method.name.endsWith("eap$appendPageAfterPatternsLabel"))
                    .forEach(method -> {
                        method.instructions.clear();
                        method.tryCatchBlocks.clear();
                        method.localVariables = null;
                        method.visibleLocalVariableAnnotations = null;
                        method.invisibleLocalVariableAnnotations = null;
                        method.instructions.add(new InsnNode(Opcodes.RETURN));
                    });
        }

        FTBQuestsLangSplitterASM.patch(targetClassName, targetClass);
    }
}

package net.fodoth.skina.goldentweaks.mixin;

import net.fodoth.skina.goldentweaks.compat.carryon.CarryOnAeroCompatASM;
import net.fodoth.skina.goldentweaks.compat.ae2autopatternupload.Ae2ApuASM;
import net.fodoth.skina.goldentweaks.compat.exspectriments.ExspectrimentsASM;
import net.fodoth.skina.goldentweaks.compat.ftbquests.FTBQuestsLangSplitterASM;
import net.fodoth.skina.goldentweaks.compat.linearbearing.LinearbearingASM;
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

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.ae_better_villagers")) {
            return isModLoaded("ae2cs") && isModLoaded("neoecoae") && isModLoaded("extendedae") && isModLoaded("ae2helpers") && isModLoaded("packagedauto");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.feature.compactmachines")) {
            return isModLoaded("compactmachines");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.compactmachines")) {
            return isModLoaded("compactmachines");
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

        // SilentGear 的预制工具（GearItem）从 EMI 索引中隐藏
        if (mixinClassName.endsWith("fix.silentgear.SilentGearEmiHiderMixin")) {
            return isModLoaded("silentgear") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.oneenoughitem.UtilsMixin")
                || mixinClassName.endsWith("fix.oneenoughitem.RawTagCaptureMixin")
                || mixinClassName.endsWith("shut.oei.OneenoughitemMixin")) {
            return isModLoaded("oneenoughitem");
        }

        if (mixinClassName.endsWith("fix.fidworkblock.ExampleModMixin")) {
            return isModLoaded("fidworkblock");
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

        // Astral Sorcery 2.0.1 的 fillCreativeTab 被标 @OnlyIn(CLIENT)，专用服务器上会崩
        if (mixinClassName.endsWith("fix.astralsorcery.CommonProxyCreativeTabMixin")) {
            return isModLoaded("astralsorcery");
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

        if (mixinClassName.endsWith("fix.ae2.NameSearchPredicateMixin")) {
            return isModLoaded("ae2");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2.")) {
            return isModLoaded("ae2") && isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.thaumcraft.ThaumatoriumOutputMixin")) {
            return isModLoaded("thaumcraft") && isModLoaded("ae2");
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

        if (mixinClassName.endsWith("fix.ae2cs.AECSAdditionalModelsMixin")) {
            return checkIfPresent("io.github.lounode.ae2cs.common.init.client.AECSAdditionalModels");
        }

        if (mixinClassName.endsWith("fix.fancymenu.MainThreadTaskExecutorMixin")) {
            return isModLoaded("fancymenu");
        }

        if (mixinClassName.endsWith("fix.tmrv.JEIPluginManagerMixin")) {
            return isModLoaded("toomanyrecipeviewers");
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

        if (mixinClassName.endsWith("shut.AEKeyLegacyComponentLoggerMixin")) {
            return isModLoaded("ae2") && isModLoaded("northstar");
        }

        if (mixinClassName.endsWith("fix.ponder.BakedModelBuffererImplMixin")) {
            return checkIfPresent("net.createmod.catnip.impl.client.render.model.BakedModelBuffererImpl");
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

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumictinkerer")) {
            return isModLoaded("thaumic_tinkerer");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons")) {
            return isModLoaded("thaumichorizons");
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

        // AcademyCraft 技能伤害按寂静装备「魔法攻击力」加成
        if (mixinClassName.endsWith("feature.academy.AcademyDamageHelperMixin")) {
            return isModLoaded("academy");
        }

        // zstdnet：端口标签重定位需要 mcwifipnp 的界面；聊天提示关闭只依赖 zstdnet
        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.zstdnet.")) {
            if (mixinClassName.endsWith("ZstdShareToLanPortLabelMixin")) {
                return isModLoaded("zstdnet") && isModLoaded("mcwifipnp");
            }
            return isModLoaded("zstdnet");
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
        CarryOnAeroCompatASM.patch(targetClassName, targetClass);
        LinearbearingASM.patch(targetClassName, targetClass);
        Ae2ApuASM.patch(targetClassName, targetClass);
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

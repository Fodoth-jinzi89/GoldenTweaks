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

        if (mixinClassName.endsWith("fix.emi.EmiSearchWidgetMixin")
                || mixinClassName.endsWith("fix.emi.EmiSearchMixin")) {
            return isModLoaded("emi");
        }

        if (mixinClassName.endsWith("fix.oneenoughitem.UtilsMixin")) {
            return isModLoaded("oneenoughitem");
        }

        if (mixinClassName.endsWith("fix.fidworkblock.ExampleModMixin")) {
            return isModLoaded("fidworkblock");
        }

        if (mixinClassName.endsWith("fix.krypton.MinecraftCompressDecoderMixin")) {
            return isModLoaded("krypton");
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

        if (mixinClassName.endsWith("fix.ae2helpers.WcwtEmiRecipeHandlerMixin")) {
            return isModLoaded("ae2helpers") && isModLoaded("wcwt") && isModLoaded("emi");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.compat.aiimprovements")) {
            return isModLoaded("aiimprovements");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.ae2helpers.")) {
            return isModLoaded("ae2helpers") && isModLoaded("wcwt");
        }

        if (mixinClassName.endsWith("fix.ae2.MEStorageScreenSearchMixin")) {
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

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.fix.thaumichorizons")) {
            return isModLoaded("thaumichorizons");
        }

        if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.balance.irons_jewelry")) {
            return isModLoaded("irons_jewelry");
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

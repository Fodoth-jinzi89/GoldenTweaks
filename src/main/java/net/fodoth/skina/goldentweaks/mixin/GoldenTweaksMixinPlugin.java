package net.fodoth.skina.goldentweaks.mixin;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.compat.exspectriments.ExspectrimentsASM;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Set;

import static net.fodoth.skina.goldentweaks.GoldenTweaks.LOGGER;
import static net.fodoth.skina.goldentweaks.util.ModPresent.checkIfPresent;

public class GoldenTweaksMixinPlugin implements IMixinConfigPlugin {

    private static final String PREFIX = "[" + GoldenTweaks.MODID + "] ";

    private enum Mode {
        VK,
        COMPAT,
        NONE
    }

    private static Mode MODE_CACHE = null;

    private Mode detectMode() {
        if (MODE_CACHE != null) {
            return MODE_CACHE;
        }

        boolean vkLike =
                checkIfPresent("org.lwjgl.vulkan.VK") ||
                        checkIfPresent("io.homo.superresolution.common.SuperResolution") ||
                        checkIfPresent("foundry.veil.Veil");

        boolean compatLike =
                checkIfPresent("lol.richy.threatengl.ThreatenGL") ||
                        checkIfPresent("icyllis.modernui.ModernUI");

        if (vkLike) {
            MODE_CACHE = Mode.VK;
        } else if (compatLike) {
            MODE_CACHE = Mode.COMPAT;
        } else {
            MODE_CACHE = Mode.NONE;
        }

        LOGGER.info(PREFIX + "mixin mode = {}", MODE_CACHE);
        return MODE_CACHE;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {

        Mode mode = detectMode();

        // VK 模式：直接屏蔽 GPUBooster 相关 mixin
        // SR Veil 之类的模组有更好的渲染优化，由它们接管
        if (mode == Mode.VK) {
            if (mixinClassName.startsWith("net.fodoth.skina.goldentweaks.mixin.gpubooster")) {
                LOGGER.warn(PREFIX + "skip (vk mode): {}", mixinClassName);
                return false;
            }
            return true;
        }

        // COMPAT 模式：只禁 GL version patch
        // TGL MUI 也会改 GL version，由它们改去
        if (mode == Mode.COMPAT) {
            if (mixinClassName.endsWith("SetGLVersionMixin")) {
                LOGGER.warn(PREFIX + "skip SetGLVersionMixin (compat mode)");
                return false;
            }
            return true;
        }

        // 万法皆通 mixin 写错
        if (mixinClassName.contains("com.github.yimeng261.maidspell.mixin.tlm.MaidFeedOwnerTaskMixin")) {
            return false;
        }

        if (mixinClassName.contains("com.konrados.ramization.mixin.MixinTextureAtlas")) {
            return false;
        }

        if (mixinClassName.contains("net.fodoth.skina.goldentweaks.mixin.feature.ae_better_villagers.AeBetterVillagersTradesMixin")) {
            return ModList.get().isLoaded("ae2cs") && ModList.get().isLoaded("neoecoae") && ModList.get().isLoaded("extendedae") && ModList.get().isLoaded("ae2helpers") && ModList.get().isLoaded("packagedauto");
        }

        // NONE：全部加载
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

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {}
}
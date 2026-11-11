package net.fodoth.skina.goldentweaks.compat.thermal;

/**
 * 标记接口：所有「专业化升级」组件都实现它。
 *
 * <p>Thermal 机器的专业化组件会改变机器可运行的配方集合，同时安装多个时只会生效一个
 * （取升级槽位里最先出现的那个，见 {@link ThermalSpecializations}）。</p>
 */
public interface GTSpecializationAugment {
}

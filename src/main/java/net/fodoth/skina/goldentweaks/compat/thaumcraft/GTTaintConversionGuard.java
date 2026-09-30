package net.fodoth.skina.goldentweaks.compat.thaumcraft;

/**
 * 腐化节点生物群系转化的旁路标记。
 *
 * <p>“禁用神秘腐化扩张”在 {@code ThaumcraftBiomeMutation.setColumn} 处统一拦截所有运行期
 * 腐化生物群系转化。腐化灵气节点的转化由独立开关控制，因此由节点自身在调用前标记这里，
 * 让全局开关放行，从而两套开关互相独立。</p>
 *
 * <p>仅在服务端主线程的方块实体 tick 中使用，标记用完即清（见
 * {@code AuraNodeBiomeConversionMixin}）。</p>
 */
public final class GTTaintConversionGuard {

    private static boolean nodeConversion;

    public static void beginNodeConversion() {
        nodeConversion = true;
    }

    public static void endNodeConversion() {
        nodeConversion = false;
    }

    public static boolean isNodeConversion() {
        return nodeConversion;
    }

    private GTTaintConversionGuard() {
    }
}

package net.fodoth.skina.goldentweaks.compat.mioicif;

/**
 * 电解机（{@code mio_icif:electrolyzer}）配方本身是单输入、每次固定消耗 1 个的格式，没有输入数量字段。
 * GoldenTweaks 约定把这些扩展写进配方的 {@code group} 字符串（该字段会被序列化器保留并同步到客户端），
 * 以逗号/分号/空格分隔的标记：
 *
 * <ul>
 *   <li>{@code input_count=N}：每次消耗 N 个输入（默认 1）；</li>
 *   <li>{@code no_energy}：该配方不产出化学能（只消耗 EU）。</li>
 * </ul>
 *
 * <p>示例：{@code "group": "input_count=4,no_energy"}。</p>
 */
public final class GTElectrolyzerInputCount {

    private static final String COUNT_PREFIX = "input_count=";
    private static final String NO_ENERGY = "no_energy";

    private GTElectrolyzerInputCount() {
    }

    /** 解析配方 group 里的输入数量；缺省/非法时返回 1（与官方行为一致）。 */
    public static int parse(String group) {
        for (String token : tokens(group)) {
            if (token.startsWith(COUNT_PREFIX)) {
                try {
                    int count = Integer.parseInt(token.substring(COUNT_PREFIX.length()).trim());
                    if (count > 0) {
                        return count;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 1;
    }

    /** 配方是否声明了不产出化学能。 */
    public static boolean noEnergy(String group) {
        for (String token : tokens(group)) {
            if (token.equals(NO_ENERGY)) {
                return true;
            }
        }
        return false;
    }

    private static String[] tokens(String group) {
        return group == null ? new String[0] : group.split("[,;\\s]+");
    }
}

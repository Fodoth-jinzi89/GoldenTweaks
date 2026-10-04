package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Certain Questing Additions (CQA) 的 {@code ChapterImageConfigGroupMixin} 是按老版本 FTB Quests 写的：
 * 它假定 {@code ChapterImageButton$3} 是 {@code ChapterImage#fillConfigGroup} 里那个匿名 ConfigGroup 子类，
 * 于是
 *
 * <pre>{@code
 * @Shadow @Final private String val$name;                      // 匿名类捕获的构造参数 name
 * @ModifyConstant(method = "getName", stringValue = "ftbquests.chapter.image")
 * }</pre>
 *
 * 而在 FTB Quests 2101.1.36 里 {@code ChapterImageButton$3} 只是 {@code ChapterImage.TextAlign} 的
 * <b>switch 映射表</b>（只有一段 {@code <clinit>}，既没有 {@code val$name} 也没有 {@code getName()}），
 * {@code ChapterImage#fillConfigGroup} 也早就不用匿名类了。于是 CQA 这个 mixin 在
 * "编辑任务书里的图片"（绘制 {@code ChapterImageButton}）时抛：
 *
 * <pre>
 * InvalidMixinException: @Shadow field val$name was not located in the target class
 *     dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3
 * </pre>
 *
 * <p>（crash-2026-09-30_23.52.34-client.txt，栈顶 {@code ChapterImageButton.maybeRenderText}）</p>
 *
 * <p>本 mixin 只负责把 CQA 要的东西补齐，让它的注入能安全地变成空操作：</p>
 * <ul>
 *     <li>{@code val$name} —— 供 CQA 的 {@code @Shadow} 绑定。这里不写初始化式：目标类没有构造器，
 *     Mixin 的实例字段初始化式需要往 {@code <init>} 里插代码，写了反而会出问题；留 null 是安全的，
 *     因为 CQA 的 {@code EntityIcon.isEntityIconString} 自带 null 判断。</li>
 *     <li>{@code getName()} —— CQA 还有一条 {@code @ModifyConstant(method = "getName")}，
 *     目标类里没有这个方法的话（配置里 {@code defaultRequire: 1}）注入依旧会失败，所以补一个返回
 *     原常量的方法。这个类只是 switch 表，没有任何调用方会调 {@code getName()}，因此是死代码。</li>
 * </ul>
 *
 * <p>priority 必须低于 CQA 的默认值 1000：Mixin 是<b>按 priority 升序</b>应用（{@code MixinInfo#compareTo}），
 * 优先级小的先应用；只有先补上字段/方法，CQA 的 {@code @Shadow}/{@code @ModifyConstant} 才能解析成功。
 * 原先写的 {@code Integer.MAX_VALUE} 会让本 mixin 排在 CQA 之后，等于没打补丁。</p>
 */
@Mixin(
        targets = "dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton$3",
        priority = 100,
        remap = false
)
public abstract class ChapterImageConfigGroupDummyMixin {

    private String val$name;

    public String getName() {
        return "ftbquests.chapter.image";
    }
}

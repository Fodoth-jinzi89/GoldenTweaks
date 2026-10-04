package net.fodoth.skina.goldentweaks.mixin.feature.ftbquests;

import dev.ftb.mods.ftbquests.events.QuestProgressEventData;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.List;

/**
 * 「产线搭建」章节：根任务打钩后，整章的子任务一起完成（它们的需求物品条目原样保留，仍然是任务目标，
 * 只是不用玩家自己一个个去点/去交）。
 *
 * <p>FTB 自带的 {@code Quest#checkForDependantCompletion} 只会给 {@code ProgressionMode.FLEXIBLE} 的后继
 * 任务补「进度已经满了」的条目，不会替玩家完成条目，所以这里在它的末尾补一段：把后继任务的每个
 * {@code Task} 直接 {@code TeamData#markTaskCompleted}。任务完成时 FTB 自己会判断
 * {@code Quest#isCompletedRaw}，满足就调用 {@code Quest#onCompleted} —— 于是补领奖励、通知、
 * 以及再往下的级联全部走 FTB 原逻辑，无需自己造轮子。子任务若没有任务条目（纯直通节点），
 * 则退回登录时那段 {@code isCompletedRaw} 判定。</p>
 *
 * <p><b>作用范围只限产线搭建章节组</b>（{@code chapter_groups.snbt} 里的
 * {@value #GT_PRODUCTION_GROUP}），其它章节的依赖关系保持原样，不会被这条规则影响。
 * 想改范围就改这个常量。还有一点：章节里剩下的非金币奖励（那 12 个物品奖励）也会因为
 * 一键完成而自动发到玩家手里。</p>
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.quest.Quest", remap = false)
public abstract class QuestDependantAutoCompleteMixin {

    private static final String GT_PRODUCTION_GROUP = "673C0DA1561F9823";

    @Inject(method = "checkForDependantCompletion", at = @At("TAIL"), remap = false)
    private void gt$completeProductionLineDependants(TeamData data, CallbackInfo ci) {
        Quest self = (Quest) (Object) this;
        Chapter chapter = self.getChapter();
        if (chapter == null || chapter.getGroup() == null
                || !GT_PRODUCTION_GROUP.equalsIgnoreCase(chapter.getGroup().getCodeString())) {
            return;
        }

        List<ServerPlayer> online = List.copyOf(data.getOnlineMembers());
        for (QuestObject object : self.getDependants()) {
            if (!(object instanceof Quest dependant) || data.isCompleted(dependant)) {
                continue;
            }

            // 这些章节用的是 dependency_requirement=all_started，而 FTB 的 setCompleted 不会顺带置 started；
            // 链式依赖里被本 mixin 直接完成的那一层如果没置 started，下一层就永远「依赖未满足」。
            // 这里补上 started，让后面的判定和界面状态都跟玩家自己点出来的一样。
            if (!data.isStarted(dependant)) {
                dependant.onStarted(new QuestProgressEventData<>(new Date(), data, dependant, online, List.of()));
            }

            for (Task task : dependant.getTasks()) {
                if (!data.isCompleted(task)) {
                    data.markTaskCompleted(task);
                }
            }

            if (!data.isCompleted(dependant)) {
                dependant.onCompleted(new QuestProgressEventData<>(new Date(), data, dependant, online, List.of()));
            }
        }
    }
}

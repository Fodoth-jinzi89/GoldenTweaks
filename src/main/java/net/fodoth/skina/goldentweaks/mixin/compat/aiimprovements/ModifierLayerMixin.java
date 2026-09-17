package net.fodoth.skina.goldentweaks.mixin.compat.aiimprovements;

import com.builtbroken.ai.improvements.modifier.editor.ModifierLayer;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI-Improvements 0.5.3（1.21.1 唯一构建，2024-06-14，上游不会修）的
 * {@code ModifierLayer.handle(Mob)} 会无条件遍历 {@code GoalSelector.getAvailableGoals()}
 * 并对每个元素直接调用 {@code WrappedGoal.getGoal()}，**没有 null 检查**；
 * 一旦该集合里出现 null 元素，服务端主线程就会
 * {@code NullPointerException: ... because "prioritizedGoal" is null} 崩服
 * （crash-2026-09-17_13.32.04-server.txt，栈顶 ModifierLayer.java:49）。
 *
 * <p>它的配置开关救不了：{@code allow_remove_calls} 只被 {@code FilteredRemove} 读取，
 * {@code ModifierSystem.onEntityJoinWorld → ModifierLevel.handle → ModifierLayer.handle}
 * 这条路径根本不读配置（javap 已确认）。所以这里走「方案 B」：在入口取消目标编辑，
 * 不遍历就不会 NPE。当前 {@code config/aiimprovements-common.toml} 里所有 {@code remove_*}
 * 都是 false，唯一开启的 {@code replace_look_controller} 走 {@code ModifierLevel} 的
 * {@code filters} 分支（独立路径），因此**功能零损失**。</p>
 *
 * <p>另外做一次**只读**的 null 探测并按 (选择器, 实体类型, 线程) 去重记 WARN：
 * 该 null 的真正来源尚未锁定（报告 3.3 推测是 Youer/Paper 异步路径或某个加速模组在
 * 并发写这个非线程安全的 fastutil 集合），这条日志是定位元凶的样本。这里刻意**不动集合**
 * （不 remove），避免在被并发破坏的集合上做写操作反而破坏数据。</p>
 */
@Mixin(value = ModifierLayer.class, remap = false)
public abstract class ModifierLayerMixin {

    @Shadow
    @Final
    public boolean combatAI;

    @Unique
    private static final Set<String> gt$reportedNullGoals = ConcurrentHashMap.newKeySet();

    @Unique
    private static final AtomicBoolean gt$patchActiveLogged = new AtomicBoolean();

    @Inject(method = "handle(Lnet/minecraft/world/entity/Mob;)V", at = @At("HEAD"), cancellable = true)
    private void gt$skipGoalEditingAfterNullCheck(Mob mob, CallbackInfo ci) {

        if (gt$patchActiveLogged.compareAndSet(false, true)) {
            GoldenTweaks.LOGGER.info(
                    "[AI-Improvements 兼容] 已跳过 ModifierLayer.handle 的目标编辑（dist {}；当前配置 remove_* 全为 false，"
                            + "该模组功能零损失。本条只打印一次）",
                    FMLEnvironment.dist
            );
        }

        gt$reportNullGoals(mob);
        ci.cancel();
    }

    @Unique
    private void gt$reportNullGoals(Mob mob) {

        GoalSelector selector = this.combatAI ? mob.targetSelector : mob.goalSelector;
        String selectorName = this.combatAI ? "targetSelector" : "goalSelector";

        int nulls = 0;
        for (WrappedGoal goal : selector.getAvailableGoals()) {
            if (goal == null) {
                nulls++;
            }
        }

        if (nulls == 0) {
            return;
        }

        String thread = Thread.currentThread().getName();
        if (!gt$reportedNullGoals.add(selectorName + '|' + mob.getType() + '|' + thread)) {
            return;
        }

        GoldenTweaks.LOGGER.warn(
                "[AI-Improvements 兼容] {} 的 {} 里有 {} 个 null 目标（维度 {}，线程 {}）→ 本次跳过目标编辑，"
                        + "请把这一行连同崩溃报告一起回贴以定位写入 null 的元凶",
                mob.getType(), selectorName, nulls, mob.level().dimension().location(), thread
        );
    }
}

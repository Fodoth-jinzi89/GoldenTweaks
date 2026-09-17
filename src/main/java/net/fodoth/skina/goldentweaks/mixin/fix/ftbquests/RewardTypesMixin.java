package net.fodoth.skina.goldentweaks.mixin.fix.ftbquests;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.Function;

/**
 * FTB Quests 2101.1.32 的奖励类型注册不是线程安全的，会让整个客户端启动失败
 * （latest (8).log，2026-09-17 22:12:41）：
 *
 * <pre>
 * java.util.ConcurrentModificationException: null
 *   at java.util.HashMap.computeIfAbsent(HashMap.java:1229)
 *   at dev.ftb.mods.ftbquests.quest.reward.RewardTypes.register(RewardTypes.java:16)
 *   at dev.ftb.mods.ftbquests.quest.reward.RewardTypes.register(RewardTypes.java:20)
 *   at com.kumoe.ftbquest_slot_rewards.FTBQSlotRewards.registerAllRewards(FTBQSlotRewards.java:25)
 *   at com.kumoe.ftbquest_slot_rewards.FTBQSlotRewards.&lt;init&gt;(FTBQSlotRewards.java:20)
 *   at net.neoforged.fml.javafmlmod.FMLModContainer.constructMod / ForkJoinPool...
 * Failed to create mod instance. ModID: ftbquest_slot_rewards
 * </pre>
 *
 * <p>{@code RewardTypes.TYPES} 是个普通 {@code HashMap}，而 NeoForge 是<b>并行</b>构造各个 mod 的
 * （stacks 里的 {@code ForkJoinPool}/{@code modloading-worker-0}）—— 本包里不止一个 FTBQ 扩展
 * （ftbquest_slot_rewards、questshop 等）在各自构造函数里注册奖励类型，两个线程同时进
 * {@code HashMap.computeIfAbsent} 就会抛 CME，接着 "Failed to create mod instance" ⇒ 启动崩溃。</p>
 *
 * <p>修法：把唯一会写 TYPES 的路径（{@code register} 里的 {@code Map#computeIfAbsent}）包进
 * {@code synchronized(TYPES)}，让注册串行化。注册完之后的读取、类型行为都不变
 * （预置的那十几种类型是在接口 {@code <clinit>} 里加的，类初始化由 JVM 保证单线程，本来就没有竞态）。</p>
 */
@Mixin(targets = "dev.ftb.mods.ftbquests.quest.reward.RewardTypes", remap = false)
public interface RewardTypesMixin {

    @Redirect(
            method = "register(Lnet/minecraft/resources/ResourceLocation;Ldev/ftb/mods/ftbquests/quest/reward/RewardType$Provider;Ljava/util/function/Supplier;Z)Ldev/ftb/mods/ftbquests/quest/reward/RewardType;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
            ),
            remap = false
    )
    private static Object gt$registerAtomically(Map<Object, Object> types, Object id,
                                               Function<Object, Object> factory) {

        synchronized (types) {
            return types.computeIfAbsent(id, factory);
        }
    }
}

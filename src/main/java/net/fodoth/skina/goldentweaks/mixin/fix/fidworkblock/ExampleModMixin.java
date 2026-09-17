package net.fodoth.skina.goldentweaks.mixin.fix.fidworkblock;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 「烟火（风味人间日常）」内嵌的子模组 <b>fidworkblock 1.0.0</b> 的教程书发放修复。
 *
 * <p>它原本的 {@code ExampleMod#giveStartBookIfFirstTime(ServerPlayer)} 这样判重：</p>
 * <pre>
 *   CompoundTag data = player.getPersistentData();
 *   if (data.getBoolean("fidworkblock_startbook_given")) return;
 *   data.putBoolean("fidworkblock_startbook_given", true);
 *   ... 给一本 startbook + 一条提示
 * </pre>
 * <p>问题在于玩家实体上的 ForgeData 实际上不随存档持久化 ⇒ 每次进世界标记都是空的，
 * 于是每次登录都发一本教程书（还会再弹一次提示）。</p>
 *
 * <p>这里改用**确定会保存**的玩家计分板 tag 作为真实依据（存在 world/data/scoreboard.dat 里）：
 * 已经有 tag 就直接拦掉；没有就补上 tag，并把 mod 自己的标记也一并写上（让它原本的判断也成立），
 * 其余逻辑（发物品、放入背包或掉在脚下、提示消息）完全走原方法。</p>
 */
@Mixin(targets = "com.fidtest.ExampleMod", remap = false)
public abstract class ExampleModMixin {

    /** 玩家计分板 tag，作为「已经发过教程书」的持久标记。 */
    @Unique
    private static final String GT_STARTBOOK_TAG = "goldentweaks$fid_startbook_given";

    @Inject(method = "giveStartBookIfFirstTime", at = @At("HEAD"), cancellable = true)
    private void gt$giveStartBookOnlyOnce(ServerPlayer player, CallbackInfo ci) {

        if (player.getTags().contains(GT_STARTBOOK_TAG)) {
            ci.cancel();
            return;
        }

        player.addTag(GT_STARTBOOK_TAG);
        player.getPersistentData().putBoolean("fidworkblock_startbook_given", true);
    }
}

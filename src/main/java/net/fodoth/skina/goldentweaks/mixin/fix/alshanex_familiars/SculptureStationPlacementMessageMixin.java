package net.fodoth.skina.goldentweaks.mixin.fix.alshanex_familiars;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 屏蔽「需要空旷的侧面」这条 placement 提示。
 *
 * <p>雕塑台在 {@code getStateForPlacement} 里检查自己的保留区，空间不足就向玩家发一条
 * actionbar 消息并返回 {@code null}（放置失败）。问题在于这个方法是<b>每次尝试放置都会调用</b>：
 * 判定又很严——侧面两格、后方 3×3×3 必须全空——所以手持方块对着任何已有结构右键，哪怕玩家只是想
 * 开个箱子或工作台，都会弹出这条消息。实测从 EMI 里取任意方块右键都能稳定复现。</p>
 *
 * <p>这里只拦掉那条消息，`{@code displayClientMessage}` 的调用换成就地返回，其余逻辑一律不碰：
 * 空间不够时方块依然放不下去，行为本身没有变化，只是不再用一句话打断每次右键。</p>
 *
 * <p>钩子落在 {@code displayClientMessage} 上而不是 {@code getStateForPlacement} 整体：后者还要
 * 返回正确的 {@code BlockState}，覆盖它就得重写整段放置逻辑，而消息是唯一的副作用，替换它最小。</p>
 */
@Mixin(targets = "net.alshanex.alshanex_familiars.block.SculptureStationBlock", remap = false)
public class SculptureStationPlacementMessageMixin {

    @Redirect(
            method = "getStateForPlacement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void gt$silenceNeedsSpace(net.minecraft.world.entity.player.Player player,
                                      net.minecraft.network.chat.Component message, boolean actionBar) {
        // Dropped on purpose. Every other outcome of the check — the null state that refuses the
        // placement — is left untouched, so this only stops the message, not the rule.
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.createsubmarine;

import com.maxenonyme.createsubmarine.submarine.client.CreateSubmarineClientEvents;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 深海 3.4.1 起，戴着 Create 护目镜看船壳方块/物品时会多出一行「基础深度」
 * （{@code create_submarine.gui.goggles.base_depth}，真实/混合水压模式下改叫「最大深度」）提示。
 * 模组没有为这行提示提供开关，且它是该事件处理里唯一的内容，因此这里直接取消整段注入。
 */
@Mixin(CreateSubmarineClientEvents.class)
public class GogglesDepthTooltipMixin {

    @Inject(
            method = "onTooltip",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void goldentweaks$hideHullDepthTooltip(
            ItemTooltipEvent event,
            CallbackInfo ci
    ) {
        ci.cancel();
    }
}

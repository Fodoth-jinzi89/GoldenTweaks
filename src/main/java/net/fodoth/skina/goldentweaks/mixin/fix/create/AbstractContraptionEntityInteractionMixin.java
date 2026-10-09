package net.fodoth.skina.goldentweaks.mixin.fix.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SophisticatedStorageMountedStorage;
import net.fodoth.skina.goldentweaks.compat.sophisticatedstorage.SsContraptionInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContraptionEntity.class)
public abstract class AbstractContraptionEntityInteractionMixin {

    // Create 的存储交互在右键任意面都会开界面且不传 hit 方向；这里对精致存储容器改成 SS 本身语义：
    // 开口面右键 → 装升级 / 存入槽位；其它情况不拦截（继续走默认打开界面）。
    @Inject(method = "handlePlayerInteraction", at = @At("HEAD"), cancellable = true)
    private void gt$sophisticatedStorageItemInteraction(Player player, BlockPos localPos, Direction side,
                                                        InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContraptionEntity self = (AbstractContraptionEntity) (Object) this;
        Contraption contraption = self.getContraption();
        if (contraption == null) {
            return;
        }
        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info == null) {
            return;
        }
        if (!(contraption.getStorage().getAllItemStorages().get(localPos) instanceof SophisticatedStorageMountedStorage mounted)) {
            return;
        }
        if (SsContraptionInteraction.handleItemInteraction(serverPlayer, contraption, mounted, info, side, hand)) {
            cir.setReturnValue(true);
        }
    }
}

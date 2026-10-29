package net.fodoth.skina.goldentweaks.mixin.balance.createoreexcavation;

import com.tom.createores.block.MultiblockPart.MultiblockMainPart;
import com.tom.createores.item.MultiBlockItem;
import net.fodoth.skina.goldentweaks.compat.createoreexcavation.IGTOreVeinOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create Ore Excavation 的多方块物品一次会摆出整台机器（主方块 + 若干幽灵部件），
 * 这里在摆放完成后把放置者记为矿脉的所有者。
 *
 * <p>只有实现了 {@link IGTOreVeinOwner} 的主方块（钻机 / 提取器）会被记录，
 * 动能输入、物品/流体端口这类部件不受影响。</p>
 */
@Mixin(value = MultiBlockItem.class, remap = false)
public abstract class MultiBlockItemOwnerMixin {

    @Inject(method = "placeBlock", at = @At("TAIL"))
    private void gt$captureVeinOwner(BlockPlaceContext context, BlockState state,
                                     CallbackInfoReturnable<Boolean> cir) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        if (!(((BlockItem) (Object) this).getBlock() instanceof MultiblockMainPart mainPart)) {
            return;
        }
        Level level = context.getLevel();
        if (level.isClientSide || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos start = context.getClickedPos().offset(mainPart.getStart(facing));
        Vec3i size = mainPart.getSize(facing);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockEntity blockEntity = level.getBlockEntity(start.offset(x, y, z));
                    if (blockEntity instanceof IGTOreVeinOwner owner && owner.gt$getVeinOwner() == null) {
                        owner.gt$setVeinOwner(player.getUUID());
                    }
                }
            }
        }
    }
}

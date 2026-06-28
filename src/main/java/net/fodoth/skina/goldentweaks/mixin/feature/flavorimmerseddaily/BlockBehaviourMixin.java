package net.fodoth.skina.goldentweaks.mixin.feature.flavorimmerseddaily;

import com.fidtest.block.entity.SteamboxBlockEntity;
import com.fidtest.registration.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
    )
    private void goldenTweaks$onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston,
            CallbackInfo ci
    ) {
        if (level.isClientSide) return;

        if (!state.is(ModBlocks.STEAMBOX.get())) return;

        if (state.is(newState.getBlock())) return;

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof SteamboxBlockEntity sb) {

            for (ItemStack item : sb.getAllItems()) {
                if (!item.isEmpty()) {
                    Containers.dropItemStack(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            item.copy()
                    );
                }
            }

            sb.clearItems();
        }
    }

    /**
     * 拦截 useItemOn 方法，当目标方块是 BottleBlock 时，执行与空手右键相同的逻辑 这个是森罗酒馆的 顺带就写在这里了
     */
    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void onUseItemOn(ItemStack p_316304_, BlockState p_316362_, Level p_316459_, BlockPos p_316366_, Player p_316132_, InteractionHand p_316595_, BlockHitResult p_316140_, CallbackInfoReturnable<ItemInteractionResult> cir) {
        // 获取被点击的方块
        BlockState state = p_316459_.getBlockState(p_316140_.getBlockPos());
        BlockPos pos = p_316140_.getBlockPos();

        // 只处理 BottleBlock
        if (!(state.getBlock() instanceof BottleBlock)) {
            return;
        }

        // 在服务端执行掉落逻辑
        if (p_316459_ instanceof ServerLevel serverLevel) {

            // 它忘记获取BE了
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos));

            // 给玩家掉落物
            drops.forEach(stack -> ItemHandlerHelper.giveItemToPlayer(p_316132_, stack));

            // 将方块设置为空气
            p_316459_.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);

            // 播放音效
            p_316459_.playSound(null, pos, SoundType.STONE.getPlaceSound(),
                    p_316132_.getSoundSource(),
                    1.0F, 1.0F);
        }

        // 返回成功，取消原方法执行
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}
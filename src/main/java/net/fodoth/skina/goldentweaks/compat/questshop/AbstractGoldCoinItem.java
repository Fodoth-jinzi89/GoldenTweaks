package net.fodoth.skina.goldentweaks.compat.questshop;

import com.holysweet.questshop.item.CoinItem;
import com.holysweet.questshop.service.CoinsService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractGoldCoinItem extends CoinItem {

    public AbstractGoldCoinItem(Properties properties) {
        super(properties);
    }

    /** 子类只需要定义倍率 */
    protected abstract int multiplier();

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand) {

        ItemStack itemInHand = player.getItemInHand(usedHand);

        if (level.isClientSide()) {
            level.playSound(
                    player,
                    player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.0F
            );
            return InteractionResultHolder.consume(itemInHand);
        }

        int amountConsumed = player.isShiftKeyDown()
                ? itemInHand.getCount()
                : 1;

        itemInHand.shrink(amountConsumed);

        CoinsService.add(
                (ServerLevel) level,
                (ServerPlayer) player,
                amountConsumed * multiplier()
        );

        if (!itemInHand.isEmpty()) {
            return InteractionResultHolder.success(itemInHand);
        }

        player.setItemInHand(usedHand, ItemStack.EMPTY);
        return InteractionResultHolder.consume(itemInHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag)  {
        tooltipComponents.add(Component.literal("￥" + multiplier()).withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return super.getName(stack)
                .copy()
                .withStyle(ChatFormatting.GOLD);
    }
}
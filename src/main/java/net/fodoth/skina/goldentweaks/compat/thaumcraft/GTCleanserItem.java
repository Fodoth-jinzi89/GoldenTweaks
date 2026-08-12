package net.fodoth.skina.goldentweaks.compat.thaumcraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import thaumcraft.common.network.TCNetwork;
import thaumcraft.common.warp.TCWarpEvents;

public class GTCleanserItem extends Item {

    private static final FoodProperties CLEANSER_FOOD = new FoodProperties.Builder()
            .alwaysEdible()
            .nutrition(0)
            .saturationModifier(0.0F)
            .build();

    public GTCleanserItem() {
        super(new Item.Properties()
                .food(CLEANSER_FOOD)
                .stacksTo(64)
                .rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 24;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level,
                                              @NotNull LivingEntity entityLiving) {
        if (entityLiving instanceof Player player) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

                int totalWarp = TCWarpEvents.getTotalWarp(player);
                if (totalWarp > 0) {
                    TCWarpEvents.setPermanentWarp(player, 0);
                    TCWarpEvents.setTemporaryWarp(player, 0);
                    TCWarpEvents.setStickyWarp(player, 0);
                    if (player instanceof ServerPlayer serverPlayer) {
                        TCNetwork.sendWarpSync(serverPlayer);
                    }
                }
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return stack;
    }
}

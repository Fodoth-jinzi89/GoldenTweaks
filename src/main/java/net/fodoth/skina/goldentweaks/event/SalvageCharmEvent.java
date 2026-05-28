package net.fodoth.skina.goldentweaks.event;

import com.chen1335.apotheosisThings.component.SalvagingCharmConfig;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = "goldentweaks")
public class SalvageCharmEvent {

    private static final String COOLDOWN_TAG =
            "goldentweaks_salvage_charm_cooldown";

    private static final int COOLDOWN_TICKS = 10;

    /*
     * Cached reflection metadata
     */
    private static Object SALVAGING_CHARM_HOLDER;

    private static Object SALVAGING_CHARM_CONFIG_HOLDER;

    private static Method HOLDER_GET;

    private static Field RARITY_FIELD;

    private static Method DYNAMIC_HOLDER_GET_ID;

    static {

        try {

            /*
             * ATItems.SALVAGING_CHARM
             */
            Class<?> atItemsClass = Class.forName(
                    "com.chen1335.apotheosisThings.object.ATItems"
            );

            Field charmField =
                    atItemsClass.getDeclaredField(
                            "SALVAGING_CHARM"
                    );

            SALVAGING_CHARM_HOLDER =
                    charmField.get(null);

            /*
             * DeferredHolder#get
             */
            HOLDER_GET =
                    SALVAGING_CHARM_HOLDER
                            .getClass()
                            .getMethod("get");

            /*
             * SalvageItem#rarity
             */
            Class<?> salvageItemClass = Class.forName(
                    "dev.shadowsoffire.apotheosis.affix.salvaging.SalvageItem"
            );

            RARITY_FIELD =
                    salvageItemClass.getDeclaredField(
                            "rarity"
                    );

            RARITY_FIELD.setAccessible(true);

            /*
             * DynamicHolder#getId
             */
            Class<?> dynamicHolderClass = Class.forName(
                    "dev.shadowsoffire.placebo.reload.DynamicHolder"
            );

            DYNAMIC_HOLDER_GET_ID =
                    dynamicHolderClass.getMethod("getId");

            /*
             * ATDataComponents.SALVAGING_CHARM_CONFIG
             */
            Class<?> dataComponentsClass = Class.forName(
                    "com.chen1335.apotheosisThings.object.ATDataComponents"
            );

            Field componentField =
                    dataComponentsClass.getDeclaredField(
                            "SALVAGING_CHARM_CONFIG"
                    );

            SALVAGING_CHARM_CONFIG_HOLDER =
                    componentField.get(null);

        } catch (Exception exception) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to initialize SalvageCharmEvent reflection cache.",
                    exception
            );
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {

        Player player = event.getEntity();

        /*
         * 仅服务端执行
         */
        if (player.level().isClientSide) {
            return;
        }

        /*
         * cooldown
         */
        if (getCooldown(player) > 0) {

            event.setCanceled(true);

            event.setCancellationResult(
                    InteractionResult.SUCCESS
            );

            return;
        }

        try {

            ItemStack charm =
                    player.getMainHandItem();

            ItemStack salvage =
                    player.getOffhandItem();

            /*
             * 延迟获取 item
             */
            Item salvagingCharm =
                    (Item) HOLDER_GET.invoke(
                            SALVAGING_CHARM_HOLDER
                    );

            /*
             * 主手必须是 charm
             */
            if (!charm.is(salvagingCharm)) {
                return;
            }

            /*
             * 必须是 SalvageItem
             */
            Object dynamicHolder =
                    RARITY_FIELD.get(
                            salvage.getItem()
                    );

            if (dynamicHolder == null) {
                return;
            }

            /*
             * 获取 rarity id
             */
            ResourceLocation rarityId =
                    (ResourceLocation)
                            DYNAMIC_HOLDER_GET_ID.invoke(
                                    dynamicHolder
                            );

            /*
             * 防止 empty:empty
             */
            if (rarityId == null
                    || rarityId.equals(
                    ResourceLocation.fromNamespaceAndPath(
                            "empty",
                            "empty"
                    )
            )) {

                GoldenTweaks.LOGGER.warn(
                        "Blocked invalid rarity id: {}",
                        rarityId
                );

                return;
            }

            /*
             * registry lookup
             */
            LootRarity rarity =
                    RarityRegistry.INSTANCE.getValue(
                            rarityId
                    );

            if (rarity == null) {

                GoldenTweaks.LOGGER.warn(
                        "Failed to resolve rarity: {}",
                        rarityId
                );

                return;
            }

            /*
             * 创建 config
             */
            SalvagingCharmConfig config =
                    new SalvagingCharmConfig(
                            rarity
                    );

            /*
             * 延迟获取 component
             */
            @SuppressWarnings("unchecked")
            DataComponentType<SalvagingCharmConfig> component =
                    (DataComponentType<SalvagingCharmConfig>)
                            HOLDER_GET.invoke(
                                    SALVAGING_CHARM_CONFIG_HOLDER
                            );

            /*
             * 写入 component
             */
            charm.set(
                    component,
                    config
            );

            /*
             * 消耗副手
             */
            salvage.shrink(1);

            /*
             * 强制同步容器
             */
            player.containerMenu.broadcastChanges();

            /*
             * cooldown
             */
            setCooldown(
                    player,
                    COOLDOWN_TICKS
            );

            /*
             * 动画
             */
            player.swing(
                    event.getHand()
            );

            /*
             * 音效
             */
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.1F
            );

            /*
             * 阻止原逻辑继续执行
             */
            event.setCanceled(true);

            event.setCancellationResult(
                    InteractionResult.SUCCESS
            );

        } catch (Exception exception) {

            GoldenTweaks.LOGGER.warn(
                    "Failed to apply salvaging charm config.",
                    exception
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        int cooldown =
                getCooldown(player);

        if (cooldown > 0) {

            setCooldown(
                    player,
                    cooldown - 1
            );
        }
    }

    private static int getCooldown(
            Player player
    ) {

        CompoundTag tag =
                player.getPersistentData();

        return tag.getInt(
                COOLDOWN_TAG
        );
    }

    private static void setCooldown(
            Player player,
            int ticks
    ) {

        CompoundTag tag =
                player.getPersistentData();

        if (ticks <= 0) {

            tag.remove(
                    COOLDOWN_TAG
            );

        } else {

            tag.putInt(
                    COOLDOWN_TAG,
                    ticks
            );
        }
    }
}


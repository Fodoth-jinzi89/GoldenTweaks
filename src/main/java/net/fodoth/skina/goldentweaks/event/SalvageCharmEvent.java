package net.fodoth.skina.goldentweaks.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@EventBusSubscriber(modid = "goldentweaks")
public class SalvageCharmEvent {

    private static final String COOLDOWN_TAG =
            "goldentweaks_salvage_charm_cooldown";

    private static final int COOLDOWN_TICKS = 10;

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {

        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        // 冷却期间直接禁用右键
        if (getCooldown(player) > 0) {
            event.setCanceled(true);
            return;
        }

        try {

            ItemStack charm = player.getMainHandItem();
            ItemStack salvage = player.getOffhandItem();

            /*
             * 获取：
             * ATItems.SALVAGING_CHARM
             */
            Class<?> atItemsClass = Class.forName(
                    "com.chen1335.apotheosisThings.object.ATItems"
            );

            Field charmField = atItemsClass.getDeclaredField("SALVAGING_CHARM");
            Object holder = charmField.get(null);

            Method getMethod = holder.getClass().getMethod("get");
            Item charmItem = (Item) getMethod.invoke(holder);

            // 主手必须是 SALVAGING_CHARM
            if (!charm.is(charmItem)) {
                return;
            }

            /*
             * 判断是否是 SalvageItem
             */
            Class<?> salvageItemClass = Class.forName(
                    "dev.shadowsoffire.apotheosis.affix.salvaging.SalvageItem"
            );

            if (!salvageItemClass.isInstance(salvage.getItem())) {
                return;
            }

            /*
             * 获取 rarity 字段
             */
            Field rarityField =
                    salvageItemClass.getDeclaredField("rarity");

            rarityField.setAccessible(true);

            Object dynamicHolder =
                    rarityField.get(salvage.getItem());

            /*
             * DynamicHolder#get
             */
            Method dynamicHolderGet =
                    dynamicHolder.getClass().getMethod("get");

            Object rarity =
                    dynamicHolderGet.invoke(dynamicHolder);

            /*
             * new SalvagingCharmConfig(rarity)
             */
            Class<?> configClass = Class.forName(
                    "com.chen1335.apotheosisThings.component.SalvagingCharmConfig"
            );

            Constructor<?> configConstructor =
                    configClass.getDeclaredConstructors()[0];

            configConstructor.setAccessible(true);

            Object config =
                    configConstructor.newInstance(rarity);

            /*
             * 获取 ATDataComponents.SALVAGING_CHARM_CONFIG
             */
            Class<?> dataComponentsClass = Class.forName(
                    "com.chen1335.apotheosisThings.object.ATDataComponents"
            );

            Field componentField =
                    dataComponentsClass.getDeclaredField(
                            "SALVAGING_CHARM_CONFIG"
                    );

            Object componentHolder = componentField.get(null);

            Method componentGet =
                    componentHolder.getClass().getMethod("get");

            Object dataComponentType =
                    componentGet.invoke(componentHolder);

            /*
             * ItemStack#set(component, value)
             */
            Method setMethod = null;

            for (Method method : ItemStack.class.getMethods()) {

                if (!method.getName().equals("set")) {
                    continue;
                }

                Class<?>[] params = method.getParameterTypes();

                if (params.length != 2) {
                    continue;
                }

                if (!params[0].isInstance(dataComponentType)) {
                    continue;
                }

                setMethod = method;
                break;
            }

            if (setMethod == null) {
                return;
            }

            setMethod.invoke(charm, dataComponentType, config);

            // 消耗一个副手物品
            salvage.shrink(1);

            // 设置冷却
            setCooldown(player, COOLDOWN_TICKS);

            // 动画
            player.swing(event.getHand());

            // 音效
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.1F
            );

            event.setCanceled(true);

        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        int cooldown = getCooldown(player);

        if (cooldown > 0) {
            setCooldown(player, cooldown - 1);
        }
    }

    private static int getCooldown(Player player) {

        CompoundTag tag = player.getPersistentData();

        return tag.getInt(COOLDOWN_TAG);
    }

    private static void setCooldown(Player player, int ticks) {

        CompoundTag tag = player.getPersistentData();

        if (ticks <= 0) {
            tag.remove(COOLDOWN_TAG);
        } else {
            tag.putInt(COOLDOWN_TAG, ticks);
        }
    }
}
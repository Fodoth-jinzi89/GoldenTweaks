package net.fodoth.skina.goldentweaks.compat.mioicif;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 「防化」附魔的运行时查询。
 *
 * <p>mio_icif 的辐射判定只认写死的防化服/量子套/纳米套，不看附魔。这里把带附魔的任意一件护甲
 * 视作等效防化装备，用来补上附魔这条线。</p>
 *
 * <p>判定沿用它自己的规则：只要角色身上有 <b>任意一件</b> 带「防化」附魔的护甲，就认为已受保护。</p>
 */
public final class GTHazmatEnchantment {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "hazmat_protection");

    private GTHazmatEnchantment() {
    }

    /** 附魔可能尚未从注册表同步到客户端，查不到时安全地返回 null。 */
    private static Holder<Enchantment> holder(LivingEntity entity) {
        if (entity == null || entity.level() == null) {
            return null;
        }
        return entity.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ID)
                .orElse(null);
    }

    public static boolean isEnchanted(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment != null
                && !stack.isEmpty()
                && stack.getEnchantmentLevel(enchantment) > 0;
    }

    /**
     * 身上是否有一件带「防化」附魔的护甲。
     *
     * <p>玩家遍历四个护甲槽；其它生物走 {@code getArmorSlots()}，这样傀儡、宠物等也能覆盖到。</p>
     */
    public static boolean protects(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        Holder<Enchantment> holder = holder(entity);
        if (holder == null) {
            return false;
        }

        if (entity instanceof Player player) {
            for (ItemStack stack : player.getArmorSlots()) {
                if (isEnchanted(stack, holder)) {
                    return true;
                }
            }
            return false;
        }

        for (ItemStack stack : entity.getArmorSlots()) {
            if (isEnchanted(stack, holder)) {
                return true;
            }
        }
        return false;
    }
}

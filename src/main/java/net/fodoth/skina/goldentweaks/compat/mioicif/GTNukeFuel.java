package net.fodoth.skina.goldentweaks.compat.mioicif;

import com.miophas.singularity_iteration.common.item.resource.mio_icif_nuclear_material;
import net.fodoth.skina.goldentweaks.config.GoldenTweaksCommonConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 让核弹的中心槽接受任何「铀类」物品，而不只是奇点迭代自己的那一个类。
 *
 * <p>原版判断是 {@code stack.getItem() instanceof mio_icif_nuclear_material}，这是个类检查：
 * 别的模组的铀块（AllTheOres 等）不是它的子类，于是既放不进去，即便塞进去辐射量也算成 0。
 * 换矿词典里的铀标签后，凡是标准化的铀都能用。</p>
 *
 * <p>辐射值只有那一套类有，外来物品取一个可配置的默认值。默认值按奇点迭代自己的普通铀
 * （{@code item_uran}，0.3）设置，所以一块外来铀块和它自家的一块铀等价。</p>
 */
public final class GTNukeFuel {

    /**
     * 铀类物品的矿词典标签，奇点迭代自己也是用它给铀材料分组的。
     *
     * <p>选 {@code c:nuclear} 而不是 {@code c:storage_blocks/uranium}：前者是这把机器真正关心的
     * 「可裂变材料」集合，粉末、锭、燃料棒、铀块都在里面；后者只覆盖方块形态，会让原本能用的
     * 铀粉反而失效。</p>
     */
    public static final TagKey<Item> NUCLEAR = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "nuclear"));

    private GTNukeFuel() {
    }

    /** 该物品能不能当中心槽的核装药。 */
    public static boolean isNuclear(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // 自家物品仍走原来的类判断：它带着真实的辐射值，比标签更精确。
        if (stack.getItem() instanceof mio_icif_nuclear_material) {
            return true;
        }
        return stack.is(NUCLEAR);
    }

    /**
     * 一件物品贡献的辐射量。
     *
     * <p>自家材料取它自己的数值；标签里的外来物品（AllTheOres 的铀块等）用配置的默认值，
     * 否则它们的辐射会是 0，核弹就只剩 IC_TNT 那部分威力。</p>
     */
    public static float radioactivity(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        if (stack.getItem() instanceof mio_icif_nuclear_material nuclear) {
            return nuclear.getRadioactivity();
        }
        if (stack.is(NUCLEAR)) {
            return GoldenTweaksCommonConfig.getNukeForeignFuelRadioactivity();
        }
        return 0.0F;
    }
}

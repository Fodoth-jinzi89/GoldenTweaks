package net.fodoth.skina.goldentweaks.mixin.fix.createmechanicalcompanion;

import java.util.Optional;
import net.myr.createmechanicalcompanion.entity.CustomWolf;
import net.myr.createmechanicalcompanion.item.MechanicalWolfLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * 动力狼连接器（mechanical_wolf_link）装备在 head 槽第 0 格以外的位置时，动力狼会不断生成又消失。
 *
 * <p>{@link CustomWolf} 每刻通过 {@code checkForDuplicate} 校验自己是否仍归属于玩家身上装备的连接器，
 * 但它只查 {@code curiosInventory.findCurio("head", 0)}，即 head 类型的第 0 格。玩家头上若有多格
 *（其它模组扩容了 head 槽），连接器放在第 1 格及以后时这里取到的是空格或别的头饰，于是判定「连接器
 * 已不在」并 {@code discard()} 掉自己；连接器物品的 {@code curioTick} 下一帧又发现狼不在而重新召唤，
 * 形成「不断生成消失」的循环。</p>
 *
 * <p>这里把这次查询改成遍历整个 head 槽，再交回原有逻辑比对 WolfUUID；只要连接器还戴在任意一个激活
 * 的 head 槽上，狼就能持续存在。与 Curios 本身一致，未激活的槽位不计入（未激活时 {@code curioTick}
 * 也不会被调用，连接器对狼的维护本来就已停止）。</p>
 */
@Mixin(value = CustomWolf.class, remap = false)
public class CustomWolfHeadSlotMixin {

    @Redirect(
            method = "checkForDuplicate",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/theillusivec4/curios/api/type/capability/ICuriosItemHandler;findCurio(Ljava/lang/String;I)Ljava/util/Optional;"))
    private Optional<SlotResult> gt$findLinkInAnyHeadSlot(ICuriosItemHandler curios, String identifier, int index) {
        for (SlotResult result : curios.findCurios(identifier)) {
            if (result.stack().getItem() instanceof MechanicalWolfLink) {
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }
}

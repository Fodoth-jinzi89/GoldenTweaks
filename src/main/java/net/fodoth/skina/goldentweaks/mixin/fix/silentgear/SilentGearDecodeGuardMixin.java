package net.fodoth.skina.goldentweaks.mixin.fix.silentgear;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.resources.ResourceLocation;
import net.silentchaos512.gear.core.DataResourceManager;
import net.silentchaos512.gear.gear.GearJsonException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Silent Gear 在解码材料/部件/天赋时，只要其引用的物品在整合包里不存在（换包、模组更新后 id 失效等），
 * {@code tryDecode} 就会抛出 {@link GearJsonException}；该异常会一路冒到资源重载，导致整个存档无法进入。
 * <p>
 * 这里把「解码 + 校验 + 附加数据 + 登记」整体包起来：解析失败的那一条直接跳过并写日志，
 * 其余条目照常加载。解码返回 {@code null} 时，后面几步也会被挡住，不会把空值塞进管理器。
 */
@Mixin(value = DataResourceManager.class, remap = false)
public class SilentGearDecodeGuardMixin {

    @WrapOperation(
            method = "onResourceManagerReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/silentchaos512/gear/core/DataResourceManager;tryDecode(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Lcom/google/gson/JsonObject;)Ljava/lang/Object;"
            )
    )
    private static Object gt$skipUndecodable(DataResourceManager<?> instance, ResourceLocation id, String pack,
                                             JsonObject json, Operation<Object> original) {
        try {
            return original.call(instance, id, pack, json);
        } catch (GearJsonException e) {
            GoldenTweaks.LOGGER.warn("Skipped silent gear data \"{}\" from pack \"{}\": {}",
                    id, pack, e.getMessage());
            return null;
        }
    }

    @WrapOperation(
            method = "onResourceManagerReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/silentchaos512/gear/core/DataResourceManager;validate(Ljava/lang/Object;Lcom/google/gson/JsonObject;)V"
            )
    )
    private static void gt$skipValidateUndecodable(DataResourceManager<?> instance, Object value, JsonObject json,
                                                   Operation<Void> original) {
        if (value != null) {
            original.call(instance, value, json);
        }
    }

    @WrapOperation(
            method = "onResourceManagerReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/silentchaos512/gear/core/DataResourceManager;attachExtraData(Ljava/lang/Object;Ljava/lang/String;Lcom/google/gson/JsonObject;)V"
            )
    )
    private static void gt$skipAttachUndecodable(DataResourceManager<?> instance, Object value, String pack,
                                                 JsonObject json, Operation<Void> original) {
        if (value != null) {
            original.call(instance, value, pack, json);
        }
    }

    @WrapOperation(
            method = "onResourceManagerReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/silentchaos512/gear/core/DataResourceManager;tryAddObject(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)V"
            )
    )
    private static void gt$skipAddUndecodable(DataResourceManager<?> instance, ResourceLocation id, Object value,
                                              Operation<Void> original) {
        if (value != null) {
            original.call(instance, id, value);
        }
    }
}

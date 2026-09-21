package net.fodoth.skina.goldentweaks.mixin.fix.oneenoughitem;

import net.fodoth.skina.goldentweaks.compat.oei.OeiRawTagResolver;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * OEI/OELib 在数据包 reload 期间校验与展开物品标签，但原版要到所有 reload listener 结束后才把
 * tags/item 绑定进注册表。此时
 * {@code Utils#isTagExists(ResourceLocation, HolderLookup.RegistryLookup)} 会返回 false，
 * 规则被 {@code ItemReplacementValidator} 判定为 {@code deferred} 并永久搁置（OELib 不会重试、
 * {@code getAllData()} 也读不到），于是统一规则在服务端全部失效，表现为重复的矿物锭随机出现。
 *
 * <p>查询注册表失败（或注册表为 null）时回退到数据包原始 tags/item 自行展开；
 * 注册表可用时行为完全不变。</p>
 */
@Mixin(targets = "com.mafuyu404.oneenoughitem.util.Utils", remap = false)
public abstract class UtilsMixin {

    @Inject(
            method = "isTagExists(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/core/HolderLookup$RegistryLookup;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$isTagExistsFallback(
            ResourceLocation tagId,
            HolderLookup.RegistryLookup<Item> registryLookup,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (gt$boundInRegistry(tagId, registryLookup)) {
            return;
        }
        cir.setReturnValue(OeiRawTagResolver.hasTag(tagId));
    }

    @Inject(
            method = "getItemsOfTag(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/core/HolderLookup$RegistryLookup;)Ljava/util/Collection;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gt$getItemsOfTagFallback(
            ResourceLocation tagId,
            HolderLookup.RegistryLookup<Item> registryLookup,
            CallbackInfoReturnable<Collection<Item>> cir
    ) {
        if (gt$boundInRegistry(tagId, registryLookup)) {
            return;
        }
        cir.setReturnValue(OeiRawTagResolver.itemsOf(tagId));
    }

    @Unique
    private static boolean gt$boundInRegistry(ResourceLocation tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
        return registryLookup != null && registryLookup.get(TagKey.create(Registries.ITEM, tagId)).isPresent();
    }
}

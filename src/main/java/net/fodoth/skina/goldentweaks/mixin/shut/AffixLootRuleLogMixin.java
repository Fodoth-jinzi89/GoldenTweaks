package net.fodoth.skina.goldentweaks.mixin.shut;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Apotheosis 的词缀掉落规则 {@code AffixLootRule} 找不到「该物品 / 该稀有度 / 该词缀类型」可用的
 * 词缀时，会直接打一条 ERROR：
 *
 * <pre>Failed to execute AffixLootRule (no affixes available) &lt;物品&gt;/&lt;稀有度&gt;/&lt;类型&gt;/&lt;分类&gt;!</pre>
 *
 * <p>这在整合包里属于「规则该跳过」的正常分支（例如 ancientreforging 的稀有度给胸甲配了能力词缀规则，
 * 但没有匹配的能力词缀），并不是需要玩家/作者处理的错误，却会随着每次掉落反复刷屏。这里把该条日志降级为
 * debug，保留排查线索但不污染正常日志。</p>
 */
@Mixin(targets = "dev.shadowsoffire.apotheosis.loot.LootRule$AffixLootRule", remap = false)
public class AffixLootRuleLogMixin {

    @Redirect(method = "execute",
            at = @At(value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            remap = false)
    private static void gt$quietMissingAffixes(Logger logger, String message, Object itemId, Object rarity, Object type, Object category) {
        logger.debug(message, itemId, rarity, type, category);
    }
}

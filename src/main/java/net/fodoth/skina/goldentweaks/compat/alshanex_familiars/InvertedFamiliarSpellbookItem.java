package net.fodoth.skina.goldentweaks.compat.alshanex_familiars;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellDataRegistryHolder;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.alshanex.alshanex_familiars.registry.PetSpellRegistry;
import net.alshanex.familiarslib.item.AbstractFamiliarSpellbookItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.alshanex.familiarslib.registry.AttributeRegistry.FAMILIAR_DAMAGE;
import static net.alshanex.familiarslib.registry.AttributeRegistry.FAMILIAR_RESIST;

public class InvertedFamiliarSpellbookItem
        extends AbstractFamiliarSpellbookItem {

    private static final Component DESCRIPTION =
            Component.translatable(
                    "item.goldentweaks.inverted_familiar_spellbook.desc"
            ).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

    public InvertedFamiliarSpellbookItem() {
        super(
                SpellDataRegistryHolder.of(
                        new SpellDataRegistryHolder(
                                PetSpellRegistry.FAMILIAR_SWAP,
                                5
                        )
                ),
                11
        );

    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            Item.TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {

        super.appendHoverText(
                stack,
                context,
                tooltipComponents,
                tooltipFlag
        );

        String vanillaDescription =
                Component.translatable(
                        "item.familiarslib.familiar_spellbook.desc"
                ).getString();

        tooltipComponents.removeIf(component ->
                component.getString()
                        .equals(vanillaDescription)
        );

        tooltipComponents.add(DESCRIPTION);
    }

    @Override
    protected AttributeContainer[] getSpellbookAttributes() {
        return new AttributeContainer[]{new AttributeContainer(AttributeRegistry.MAX_MANA, 1000.0F, AttributeModifier.Operation.ADD_VALUE), new AttributeContainer(AttributeRegistry.CAST_TIME_REDUCTION, 0.50, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.SPELL_POWER, 1.00, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.MANA_REGEN, 1.00, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.SPELL_RESIST, 0.50, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 1.00, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(FAMILIAR_DAMAGE, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), new AttributeContainer(FAMILIAR_RESIST, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)};
    }


}

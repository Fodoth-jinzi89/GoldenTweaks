package net.fodoth.skina.goldentweaks.mixin.fix.irons_spellbooks;

import de.cadentem.additional_attributes.compat.irons_spellbooks.ISAttributes;
import de.cadentem.additional_attributes.compat.irons_spellbooks.SpellUtils;
import de.cadentem.additional_attributes.config.ServerConfig;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = SpellUtils.class, remap = false)
public class SpellUtilsMixin {

    /**
     * @author GoldenTweaks
     * @reason Prevent crash when spell/school attribute is missing
     */
    @Overwrite
    public static int calculateSpellLevel(@Nullable LivingEntity livingEntity, AbstractSpell spell, int originalLevel) {

        if (livingEntity == null) {
            return originalLevel;
        }

        ResourceLocation spellResource = spell.getSpellResource();
        ResourceLocation schoolResource = spell.getSchoolType().getId();

        Registry<Attribute> registry = BuiltInRegistries.ATTRIBUTE;

        Holder<Attribute> schoolAttribute = goldenTweaks$getAttribute(
                registry,
                "school/" + schoolResource.getNamespace() + "/" + schoolResource.getPath()
        );

        Holder<Attribute> spellAttribute = goldenTweaks$getAttribute(
                registry,
                "spell/" + spellResource.getNamespace() + "/" + spellResource.getPath()
        );

        List<AttributeModifier> addition = new ArrayList<>();
        List<AttributeModifier> multiplyBase = new ArrayList<>();
        List<AttributeModifier> multiplyTotal = new ArrayList<>();

        goldenTweaks$fillModifiers(livingEntity, ISAttributes.SPELL_GENERAL, addition, multiplyBase, multiplyTotal);

        if (schoolAttribute != null) {
            goldenTweaks$fillModifiers(livingEntity, schoolAttribute, addition, multiplyBase, multiplyTotal);
        }

        if (spellAttribute != null) {
            goldenTweaks$fillModifiers(livingEntity, spellAttribute, addition, multiplyBase, multiplyTotal);
        }

        double base = originalLevel;

        for (AttributeModifier modifier : addition) {
            base += modifier.amount();
        }

        double result = base;

        for (AttributeModifier modifier : multiplyBase) {
            result += base * modifier.amount();
        }

        for (AttributeModifier modifier : multiplyTotal) {
            result *= 1.0F + modifier.amount();
        }

        return spell.getMaxLevel() == 1
                && result > 1.0
                && !ServerConfig.ALLOW_MAX_LEVEL_ONE_INCREASES.get()
                ? originalLevel
                : (int) Mth.clamp(result, 0.0, Math.max(100, originalLevel));
    }

    @Unique
    private static Holder<Attribute> goldenTweaks$getAttribute(Registry<Attribute> registry, String path) {

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                "additional_attributes",
                path
        );

        ResourceKey<Attribute> key = ResourceKey.create(
                BuiltInRegistries.ATTRIBUTE.key(),
                id
        );

        Optional<Holder.Reference<Attribute>> optional = registry.getHolder(key);

        return optional.orElse(null);
    }

    @Unique
    private static void goldenTweaks$fillModifiers(
            LivingEntity livingEntity,
            Holder<Attribute> attribute,
            List<AttributeModifier> addition,
            List<AttributeModifier> multiplyBase,
            List<AttributeModifier> multiplyTotal
    ) {

        AttributeInstance instance = livingEntity.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        for (AttributeModifier modifier : instance.getModifiers()) {

            switch (modifier.operation()) {

                case ADD_VALUE ->
                        addition.add(modifier);

                case ADD_MULTIPLIED_BASE ->
                        multiplyBase.add(modifier);

                case ADD_MULTIPLIED_TOTAL ->
                        multiplyTotal.add(modifier);
            }
        }
    }
}
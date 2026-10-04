package net.fodoth.skina.goldentweaks.compat.traveloptics;

import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * traveloptics 召唤物的友军保护。
 *
 * <p>近战、远程、魔法等任何来源的伤害都不得命中召唤者本人及其所属生物
 * （touhou 女仆、alshanex_familiars 魔宠、其它 iron 召唤物、主人驯服的生物），
 * 同时禁止召唤物将这些生物设为目标；目标即将被设定时直接清空。</p>
 */
public final class TravelOpticsSummonProtection {

    private static final String SUMMON_PACKAGE = "com.gametechbc.traveloptics";

    private static final java.util.Set<String> DEBUG_SEEN =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static final ClassValue<Boolean> IS_TRAVELOPTICS_SUMMON = new ClassValue<>() {

        @Override
        protected Boolean computeValue(Class<?> type) {
            return IMagicSummon.class.isAssignableFrom(type)
                    && type.getName().startsWith(SUMMON_PACKAGE);
        }
    };

    /**
     * 技能召唤的实体 -> 施法者。部分 traveloptics 召唤物的 {@code getSummoner()} 返回 null，
     * 这里记录 iron 的 {@link SpellSummonEvent} 中带出的施法者，作为主人解析的兜底。
     */
    private static final Map<Entity, Entity> SUMMON_OWNERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private TravelOpticsSummonProtection() {
    }

    @SubscribeEvent
    public static void onSpellSummon(SpellSummonEvent<?> event) {

        Entity creature = event.getCreature();
        Entity caster = event.getCaster();

        if (creature == null || caster == null || creature.level().isClientSide) {
            return;
        }

        captureOwner(creature, caster);
    }

    /**
     * 记录召唤物 -> 召唤者。被 {@code SummonedXxx.setSummoner} 的 mixin 调用，
     * 不依赖召唤物自身（可能损坏的）{@code getSummoner()} 字段。
     */
    public static void captureOwner(Entity creature, Entity owner) {

        if (creature == null || owner == null) {
            return;
        }

        SUMMON_OWNERS.put(creature, owner);
    }

    /**
     * 去掉灾变为这些召唤物附加的数值缩放（{@code cataclysm} 命名空间的 MAX_HEALTH / ATTACK_DAMAGE
     * 修正器，来自 cataclysm-common.toml 的 health_multiplier / attack_multiplier），
     * 让召唤物按技能自身（tooltip / 模组内部）以及灾变默认基础值走，而不是跟着整套灾变缩放走。
     */
    public static void applyIntendedStats(LivingEntity entity) {

        stripCataclysmModifiers(entity.getAttribute(Attributes.MAX_HEALTH));
        stripCataclysmModifiers(entity.getAttribute(Attributes.ATTACK_DAMAGE));
    }

    private static void stripCataclysmModifiers(@Nullable AttributeInstance instance) {

        if (instance == null) {
            return;
        }

        for (AttributeModifier modifier : Set.copyOf(instance.getModifiers())) {
            if ("cataclysm".equals(modifier.id().getNamespace())) {
                instance.removeModifier(modifier.id());
            }
        }
    }

    /**
     * 无主的 traveloptics 召唤物（刷怪蛋/指令生成）直接清除，避免其攻击玩家。
     */
    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {

        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();

        if (isTravelOpticsSummon(entity) && !hasOwnerReference(entity)) {
            entity.discard();
        }
    }

    /**
     * 禁止召唤物将主人或主人的生物设为目标。
     */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {

        LivingEntity mob = event.getEntity();

        if (isTravelOpticsSummon(mob)) {
            gtDebug(
                    "changeTarget mob=" + mob.getClass().getSimpleName()
                            + " summoner=" + describeOwner(mob)
                            + " newTarget=" + (event.getNewAboutToBeSetTarget() == null
                            ? "null"
                            : event.getNewAboutToBeSetTarget().getClass().getSimpleName())
            );
        }

        LivingEntity summoner = getSummonerIfTravelOpticsSummon(mob);

        if (summoner == null) {
            return;
        }

        if (isProtected(summoner, event.getNewAboutToBeSetTarget())) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    /**
     * 阻止召唤物对主人及主人的生物造成任何伤害。
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        DamageSource source = event.getSource();

        Entity attacker = source.getEntity();

        if (!isTravelOpticsSummon(attacker)) {
            Entity direct = source.getDirectEntity();
            attacker = direct instanceof Projectile projectile ? projectile.getOwner() : direct;
        }

        if (isTravelOpticsSummon(attacker)) {
            gtDebug(
                    "incoming victim=" + event.getEntity().getClass().getSimpleName()
                            + " src=" + (source.getEntity() == null ? "null" : source.getEntity().getClass().getSimpleName())
                            + " direct=" + (source.getDirectEntity() == null ? "null" : source.getDirectEntity().getClass().getSimpleName())
            );
        }

        LivingEntity summoner = getSummonerIfTravelOpticsSummon(attacker);

        if (summoner == null) {
            return;
        }

        if (isProtected(summoner, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @Nullable
    private static LivingEntity getSummonerIfTravelOpticsSummon(Entity entity) {

        if (!isTravelOpticsSummon(entity)) {
            return null;
        }

        return resolveOwner(entity);
    }

    private static boolean isTravelOpticsSummon(Entity entity) {
        return entity != null && IS_TRAVELOPTICS_SUMMON.get(entity.getClass());
    }

    private static boolean isProtected(LivingEntity summoner, @Nullable Entity target) {

        if (target == null) {
            return false;
        }

        if (target == summoner) {
            return true;
        }

        LivingEntity targetOwner = resolveOwner(target);

        if (targetOwner != null) {
            return targetOwner == summoner
                    || targetOwner.isAlliedTo(summoner)
                    || summoner.isAlliedTo(targetOwner);
        }

        return target.isAlliedTo(summoner)
                || summoner.isAlliedTo(target);
    }

    /**
     * 解析实体的主人：召唤物取召唤者（失败则用记录值 / 存档中的 Summoner），宠物取拥有者，普通生物返回 null。
     */
    @Nullable
    private static LivingEntity resolveOwner(Entity entity) {

        if (entity instanceof AbstractSpellCastingPet pet) {
            LivingEntity summoner = pet.getSummoner();
            if (summoner != null) {
                return summoner;
            }
        } else if (entity instanceof IMagicSummon summon) {
            Entity owner = summon.getSummoner();
            if (owner instanceof LivingEntity living) {
                return living;
            }
        } else if (entity instanceof OwnableEntity ownable) {
            LivingEntity owner = ownable.getOwner();
            if (owner != null) {
                return owner;
            }
        }

        LivingEntity captured = capturedOwner(entity);
        if (captured != null) {
            return captured;
        }

        UUID uuid = recordedSummonerUuid(entity);
        return uuid == null ? null : resolveByUuid(entity, uuid);
    }

    /**
     * 是否存在任何主人线索：记录值、{@code getSummoner()} 或存档中的 Summoner UUID。
     */
    private static boolean hasOwnerReference(Entity entity) {

        if (capturedOwner(entity) != null) {
            return true;
        }

        if (entity instanceof IMagicSummon summon && summon.getSummoner() != null) {
            return true;
        }

        if (entity instanceof AbstractSpellCastingPet pet && pet.getSummoner() != null) {
            return true;
        }

        if (entity instanceof OwnableEntity ownable && ownable.getOwner() != null) {
            return true;
        }

        return recordedSummonerUuid(entity) != null;
    }

    @Nullable
    private static LivingEntity capturedOwner(Entity entity) {

        Entity owner = SUMMON_OWNERS.get(entity);

        return owner instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Nullable
    private static UUID recordedSummonerUuid(Entity entity) {

        try {
            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
            return tag.hasUUID("Summoner") ? tag.getUUID("Summoner") : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    private static LivingEntity resolveByUuid(Entity entity, UUID uuid) {

        if (entity.level() instanceof ServerLevel serverLevel) {
            Entity owner = serverLevel.getEntity(uuid);
            if (owner instanceof LivingEntity living) {
                return living;
            }
        }

        return null;
    }

    private static String describeOwner(Entity entity) {

        if (entity instanceof IMagicSummon) {
            LivingEntity owner = resolveOwner(entity);
            return owner == null ? "null" : owner.getClass().getSimpleName();
        }

        return "-";
    }

    private static void gtDebug(String message) {

        if (DEBUG_SEEN.size() < 400 && DEBUG_SEEN.add(message)) {
            GoldenTweaks.LOGGER.info("[GT-DBG] {}", message);
        }
    }
}

package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.compat.thaumcraft.SafeSheepModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.client.renderers.entity.LegacyThaumcraftMobRenderer;
import thaumcraft.common.entities.LegacyThaumcraftMobEntity;
import thaumcraft.common.entities.LegacyThaumcraftMobKind;

/**
 * Two fixes for Thaumcraft's catch-all legacy mob renderer:
 *
 * <p><b>1. Model de-duplication.</b> {@code TCClientSetup.registerRenderers} registers this renderer once
 * <em>per entity type</em> (29 types), and the 2-arg constructor unconditionally bakes/creates all 28
 * {@code EntityModel} fields for each instance — so the game ends up with 29 copies of the whole mob model
 * set (~9 MB each, ~260 MB total). This mixin captures the first instance's models into a static array and
 * re-points every later instance at the same shared objects. {@code render()} only reads them through
 * {@code modelFor(entity)} and assigns the result to the mutable {@code MobRenderer.model} field, so sharing
 * them across renderers is safe (rendering is single-threaded and {@code setupAnim} runs before each draw).</p>
 *
 * <p><b>2. Sheep model.</b> {@code TAINT_SHEEP} uses {@link SafeSheepModel} (same bone hierarchy as vanilla
 * {@code SheepModel}) so OptiFine CEM / ETF / FreshAnimations can replace it, and to avoid the
 * {@code (Sheep)} cast inside {@code SheepModel}.</p>
 */
@Mixin(value = LegacyThaumcraftMobRenderer.class, remap = false)
public abstract class LegacyThaumcraftMobRendererMixin {

    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> humanoidModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> brainyVillagerModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> cultistModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> giantModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> endermanModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> eldritchCrabModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> eldritchGuardianModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> eldritchGolemModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> fireBatModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> pechModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintacleModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintacleSmallModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintacleGiantModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintSheepModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintSporeModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintSporeSwarmerModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> taintSwarmModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> watcherModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> wispModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> trunkModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> witchModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> villagerModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> spiderModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> slimeModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> creeperModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> chickenModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> cowModel;
    @Shadow @Final @Mutable private EntityModel<LegacyThaumcraftMobEntity> pigModel;

    /** index 0-27 = the fields above, index 28 = the SafeSheepModel. */
    @Unique
    private static EntityModel<LegacyThaumcraftMobEntity>[] gt$sharedModels;

    @Unique
    private EntityModel<LegacyThaumcraftMobEntity> gt$sheepModel;

    @Inject(method = "<init>*", at = @At("RETURN"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void gt$onConstructed(EntityRendererProvider.Context context,
                                  EntityModel<LegacyThaumcraftMobEntity> fallback,
                                  CallbackInfo ci) {
        if (gt$sharedModels == null) {
            gt$sharedModels = new EntityModel[]{
                    this.humanoidModel,
                    this.brainyVillagerModel,
                    this.cultistModel,
                    this.giantModel,
                    this.endermanModel,
                    this.eldritchCrabModel,
                    this.eldritchGuardianModel,
                    this.eldritchGolemModel,
                    this.fireBatModel,
                    this.pechModel,
                    this.taintacleModel,
                    this.taintacleSmallModel,
                    this.taintacleGiantModel,
                    this.taintSheepModel,
                    this.taintSporeModel,
                    this.taintSporeSwarmerModel,
                    this.taintSwarmModel,
                    this.watcherModel,
                    this.wispModel,
                    this.trunkModel,
                    this.witchModel,
                    this.villagerModel,
                    this.spiderModel,
                    this.slimeModel,
                    this.creeperModel,
                    this.chickenModel,
                    this.cowModel,
                    this.pigModel,
                    new SafeSheepModel(context.bakeLayer(ModelLayers.SHEEP))
            };
        }

        EntityModel<LegacyThaumcraftMobEntity>[] shared = gt$sharedModels;
        this.humanoidModel = shared[0];
        this.brainyVillagerModel = shared[1];
        this.cultistModel = shared[2];
        this.giantModel = shared[3];
        this.endermanModel = shared[4];
        this.eldritchCrabModel = shared[5];
        this.eldritchGuardianModel = shared[6];
        this.eldritchGolemModel = shared[7];
        this.fireBatModel = shared[8];
        this.pechModel = shared[9];
        this.taintacleModel = shared[10];
        this.taintacleSmallModel = shared[11];
        this.taintacleGiantModel = shared[12];
        this.taintSheepModel = shared[13];
        this.taintSporeModel = shared[14];
        this.taintSporeSwarmerModel = shared[15];
        this.taintSwarmModel = shared[16];
        this.watcherModel = shared[17];
        this.wispModel = shared[18];
        this.trunkModel = shared[19];
        this.witchModel = shared[20];
        this.villagerModel = shared[21];
        this.spiderModel = shared[22];
        this.slimeModel = shared[23];
        this.creeperModel = shared[24];
        this.chickenModel = shared[25];
        this.cowModel = shared[26];
        this.pigModel = shared[27];
        this.gt$sheepModel = shared[28];
    }

    /**
     * TAINT_SHEEP returns SafeSheepModel, so OptiFine CEM / ETF can replace its texture/model.
     */
    @Inject(method = "modelFor", at = @At("HEAD"), cancellable = true)
    private void gt$onModelFor(LegacyThaumcraftMobEntity entity,
                               CallbackInfoReturnable<EntityModel<LegacyThaumcraftMobEntity>> cir) {
        if (entity.getLegacyKind() == LegacyThaumcraftMobKind.TAINT_SHEEP) {
            cir.setReturnValue(this.gt$sheepModel);
        }
    }
}

package net.fodoth.skina.goldentweaks.compat.thaumcraft.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import org.jetbrains.annotations.NotNull;
import thaumcraft.client.renderers.entity.mob.MobRenderStyle;
import thaumcraft.client.renderers.entity.mob.MobRenderStyles;
import thaumcraft.common.entities.LegacyThaumcraftMobEntity;
import thaumcraft.common.entities.LegacyThaumcraftMobKind;

import java.util.EnumMap;
import java.util.Map;

/** Each renderer retains only the styles it has actually used, with its own models and layers. */
public final class GTLegacyMobStyles extends EnumMap<LegacyThaumcraftMobKind, MobRenderStyle> {
    private final EntityRendererProvider.Context context;
    private final RenderLayerParent<LegacyThaumcraftMobEntity, EntityModel<LegacyThaumcraftMobEntity>> parent;
    private final EntityModel<LegacyThaumcraftMobEntity> model;

    public GTLegacyMobStyles(@NotNull EntityRendererProvider.Context context,
                             @NotNull RenderLayerParent<LegacyThaumcraftMobEntity, EntityModel<LegacyThaumcraftMobEntity>> parent,
                             @NotNull EntityModel<LegacyThaumcraftMobEntity> model) {
        super(LegacyThaumcraftMobKind.class);
        this.context = context;
        this.parent = parent;
        this.model = model;
    }

    @Override
    public synchronized MobRenderStyle get(Object key) {
        MobRenderStyle style = super.get(key);
        if (style == null && key instanceof LegacyThaumcraftMobKind kind) {
            // Keep the upstream factory authoritative. Its other styles are temporary, never shared
            // across parents; a renderer queried for a second kind builds that kind independently.
            Map<LegacyThaumcraftMobKind, MobRenderStyle> styles = MobRenderStyles.create(context, parent, model);
            style = styles.get(kind);
            if (style == null) {
                throw new IllegalStateException("Missing Thaumcraft render style: " + kind);
            }
            put(kind, style);
        }
        return style;
    }
}

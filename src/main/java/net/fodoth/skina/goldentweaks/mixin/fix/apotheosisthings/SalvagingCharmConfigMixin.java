package net.fodoth.skina.goldentweaks.mixin.fix.apotheosisthings;

import com.chen1335.apotheosisThings.component.SalvagingCharmConfig;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;

@Mixin(
        value = SalvagingCharmConfig.class,
        remap = false
)
public abstract class SalvagingCharmConfigMixin {

    @Final
    @Shadow(remap = false)
    @Mutable
    public static StreamCodec<
                RegistryFriendlyByteBuf,
                SalvagingCharmConfig
                > STREAM_CODEC;

    static {

        STREAM_CODEC = StreamCodec.of(

                /*
                 * encode
                 */
                (
                        RegistryFriendlyByteBuf buf,
                        SalvagingCharmConfig config
                ) -> {

                    LootRarity rarity =
                            config.rarity();

                    ResourceLocation id =
                            RarityRegistry.INSTANCE.getKey(
                                    rarity
                            );

                    /*
                     * reload 后旧对象失效
                     */
                    if (id == null) {

                        id = ResourceLocation.fromNamespaceAndPath(
                                "apotheosis",
                                "common"
                        );
                    }

                    ResourceLocation.STREAM_CODEC.encode(
                            buf,
                            id
                    );
                },

                /*
                 * decode
                 */
                (RegistryFriendlyByteBuf buf) -> {

                    ResourceLocation id =
                            ResourceLocation.STREAM_CODEC.decode(
                                    buf
                            );

                    LootRarity rarity =
                            RarityRegistry.INSTANCE.getValue(
                                    id
                            );

                    /*
                     * registry lookup failed
                     */
                    if (rarity == null) {

                        rarity =
                                RarityRegistry.INSTANCE.getValue(
                                        ResourceLocation.fromNamespaceAndPath(
                                                "apotheosis",
                                                "common"
                                        )
                                );
                    }

                    return new SalvagingCharmConfig(
                            rarity
                    );
                }
        );
    }
}
# libs —— 本地编译依赖（不随仓库分发）

本目录存放编译期需要的第三方模组 jar，由根目录 `build.gradle` 通过 `fileTree` 与 `flatDir` 引入：

| 子目录 | Gradle 配置 | 说明 |
|---|---|---|
| `compileOnly/` | `compileOnly fileTree(...)` | 仅编译期可见，不会进运行期 classpath |
| `implementation/` | `implementation fileTree(...)` | 编译期与运行期都可见 |
| `runtimeOnly/` | `runtimeOnly fileTree(...)` | 仅运行期可见（当前为空） |
| `reference/` | 无 | 仅供人工查阅的反编译参考 jar，不参与编译 |

这些 jar 的版权归各自模组作者所有，因此 **不随本仓库分发**，仓库里该目录只保留本说明文件。
下面的表格是编译时用到的完整清单，方便按「模组名 + 版本」自行补齐。

约定：

- 文件名形如 `[中文标签]模组名-版本.jar`，`[中文标签]` 是维护者自己的分组（`核心` = 主要模组、
  `基础` = 前置、`库` = 库），可以忽略；`fileTree` 收集整个目录，**文件名不重要，版本才重要**。
- 缺失的类会在 `compileJava` 阶段以 `cannot find symbol` 报错，按报错补对应模组即可。
- 不要把同一模组的多个版本放进同一个子目录，否则会出现重复类冲突。同一模组同时出现在
  `compileOnly/` 与 `implementation/` 时只保留 `implementation/` 那份（它已包含编译期可见性）。


## compileOnly/ —— 141 个 jar（仅编译期可见）

| 模组 | 版本 | 文件名 |
|---|---|---|
| AcademyCraft-neoforge | `1.21.1-0.0.8-rebuilt` | `AcademyCraft-neoforge-1.21.1-0.0.8-rebuilt.jar` |
| AdvancedLootInfo-neoforge | `1.21.1-1.11.0` | `AdvancedLootInfo-neoforge-1.21.1-1.11.0.jar` |
| ae2-pattern-encoding-access-terminal | `1.21.1-1.1.0-rc.4` | `ae2-pattern-encoding-access-terminal-1.21.1-1.1.0-rc.4.jar` |
| AE2-QoL-Client | `mc1.21.1-2.0.1` | `AE2-QoL-Client-mc1.21.1-2.0.1.jar` |
| ae2_auto_pattern_upload | `2.0.0` | `ae2_auto_pattern_upload-2.0.0.jar` |
| ae2cs | `1.21.1-neoforge-1.1.12` | `ae2cs-1.21.1-neoforge-1.1.12.jar` |
| ae2helpers | `1.0.1` | `ae2helpers-1.0.1.jar` |
| ae_better_villagers | `1.15.1.b` | `ae_better_villagers-1.15.1.b.jar` |
| aeallpattern | `1.21.1-neoforge-0.1.7` | `aeallpattern-1.21.1-neoforge-0.1.7.jar` |
| aero_cam_sync | `1.3.1` | `aero_cam_sync-1.3.1.jar` |
| AlltheCompatibility | `1.21.1-(v.3.9.1)` | `AlltheCompatibility-1.21.1-(v.3.9.1).jar` |
| allthecompressed | `1.21.1-4.4.0` | `allthecompressed-1.21.1-4.4.0.jar` |
| alltheores | `3.2.0_neoforge_1.21.1` | `alltheores-3.2.0_neoforge_1.21.1.jar` |
| alshanex_familiars | `1.21.1_v4.0.1` | `alshanex_familiars-1.21.1_v4.0.1.jar` |
| ancientreforging | `1.8.5` | `ancientreforging-1.8.5.jar` |
| Annuus-neoforge | `1.0.16-fi2` | `Annuus-neoforge-1.0.16-fi2.jar` |
| Apotheosis | `1.21.1-8.6.0` | `Apotheosis-1.21.1-8.6.0.jar` |
| apotheosis_things | `2101.1.1` | `apotheosis_things-2101.1.1.jar` |
| ApothicAttributes | `1.21.1-2.10.1` | `ApothicAttributes-1.21.1-2.10.1.jar` |
| ApothicEnchanting | `1.21.1-1.6.0` | `ApothicEnchanting-1.21.1-1.6.0.jar` |
| architectury | `13.0.8-neoforge` | `architectury-13.0.8-neoforge.jar` |
| avaritia_integration | `1.0` | `avaritia_integration-1.0.jar` |
| avaritia_more_items | `1.21.1-NeoForge-beta-1.0.1` | `avaritia_more_items-1.21.1-NeoForge-beta-1.0.1.jar` |
| BiomesOPlenty-neoforge | `1.21.1-21.1.0.13` | `[核心]BiomesOPlenty-neoforge-1.21.1-21.1.0.13.jar` |
| BuildingWands-neoforge | `MC1.21.1-3.0.5` | `BuildingWands-neoforge-MC1.21.1-3.0.5.jar` |
| carryon-neoforge | `1.21.1-2.2.6.13` | `carryon-neoforge-1.21.1-2.2.6.13.jar` |
| CarryOnAeroCompat | `1.21.1-1.1.1` | `CarryOnAeroCompat-1.21.1-1.1.1.jar` |
| CBC-Military-Supplement | `1.21.1-2.1.0` | `CBC-Military-Supplement-1.21.1-2.1.0.jar` |
| ccbtweaks | `1.0.0-b.27+1.21.1-neoforge` | `ccbtweaks-1.0.0-b.27+1.21.1-neoforge.jar` |
| certain_questing_additions-neoforge | `1.2.0.4+mc1.21.1` | `certain_questing_additions-neoforge-1.2.0.4+mc1.21.1.jar` |
| cfwinfo | `1.6.0` | `cfwinfo-1.6.0.jar` |
| cmpackagecouriers-neoforge | `2.2.4` | `cmpackagecouriers-neoforge-2.2.4.jar` |
| cmverticaladditions-neoforge | `1.0.0` | `cmverticaladditions-neoforge-1.0.0.jar` |
| compactmachines-core-api | `7.0.81` | `compactmachines-core-api-7.0.81.jar` |
| compactmachines-neoforge | `7.0.81` | `compactmachines-neoforge-7.0.81.jar` |
| continuity | `3.0.0+0.0.1+1.21.1.neoforge-all` | `[库]continuity-3.0.0+0.0.1+1.21.1.neoforge-all.jar` |
| copycats | `3.0.4+mc.1.21.1-neoforge` | `copycats-3.0.4+mc.1.21.1-neoforge.jar` |
| cosmeticarmorreworkedforked-neoforge | `1.21.1-0.0.4` | `cosmeticarmorreworkedforked-neoforge-1.21.1-0.0.4.jar` |
| cosmeticcorpsecompat | `1.21.1-NeoForge-4.0.1` | `cosmeticcorpsecompat-1.21.1-NeoForge-4.0.1.jar` |
| create-apothic-enchanting | `0.1.0` | `create-apothic-enchanting-0.1.0.jar` |
| create-enchantment-industry | `2.5.0` | `create-enchantment-industry-2.5.0.jar` |
| create_fantasizing | `1.21.1-1.1.3` | `create_fantasizing-1.21.1-1.1.3.jar` |
| create_optical | `0.4.2` | `create_optical-0.4.2.jar` |
| create_pattern_schematics | `2.0.10` | `[机械动力：模式蓝图] create_pattern_schematics-2.0.10.jar` |
| create_ratatouille | `1.21.1-1.3.9-2` | `create_ratatouille-1.21.1-1.3.9-2.jar` |
| create_submarine | `2.1.3` | `create_submarine-2.1.3.jar` |
| createbigcannons | `5.11.6+mc.1.21.1` | `createbigcannons-5.11.6+mc.1.21.1.jar` |
| createdieselgenerators | `1.21.1-1.3.12` | `createdieselgenerators-1.21.1-1.3.12.jar` |
| CreateDragonsPlus | `1.11.3` | `CreateDragonsPlus-1.11.3.jar` |
| createvoidway | `0.2.4+mc1.21.1` | `createvoidway-0.2.4+mc1.21.1.jar` |
| croptopia-neoforge | `1.21.1-4.2.4` | `[核心]croptopia-neoforge-1.21.1-4.2.4.jar` |
| Cucumber | `1.21.1-8.0.16` | `[库]Cucumber-1.21.1-8.0.16.jar` |
| dev.eriksonn.aeronautics.aeronautics-neoforge | `1.21.1-1.3.0` | `dev.eriksonn.aeronautics.aeronautics-neoforge-1.21.1-1.3.0.jar` |
| dev.simulated_team.simulated.simulated-neoforge | `1.21.1-1.3.0` | `dev.simulated_team.simulated.simulated-neoforge-1.21.1-1.3.0.jar` |
| dyeable-components | `1.0.1+mc1.21.1` | `dyeable-components-1.0.1+mc1.21.1.jar` |
| emilink | `1.1.10` | `emilink-1.1.10.jar` |
| exspectriments | `0.3.0_mapped_moj_1.21.1` | `exspectriments-0.3.0_mapped_moj_1.21.1.jar` |
| ExtendedAE | `1.21-2.2.35-neoforge` | `ExtendedAE-1.21-2.2.35-neoforge.jar` |
| extendedae_plus | `1.6.1` | `extendedae_plus-1.6.1.jar` |
| ExtendedCrafting | `1.21.1-7.0.8` | `[核心]ExtendedCrafting-1.21.1-7.0.8.jar` |
| familiarslib | `1.21.1-1.7.1` | `familiarslib-1.21.1-1.7.1.jar` |
| fancymenu_neoforge | `3.9.10_MC_1.21.1` | `fancymenu_neoforge_3.9.10_MC_1.21.1.jar` |
| FarmersDelight | `1.21.1-1.3.3` | `FarmersDelight-1.21.1-1.3.3.jar` |
| fastquit | `3.0.0+1.20.6_mapped_moj_1.21.1` | `fastquit-3.0.0+1.20.6_mapped_moj_1.21.1.jar` |
| fidworkblock | `1.0.0` | `fidworkblock-1.0.0.jar` |
| flavor_immersed_daily | `2026.9.12` | `flavor_immersed_daily-2026.9.12.jar` |
| fluidlogistics | `1.0.9` | `fluidlogistics-1.0.9.jar` |
| flywheel-neoforge | `1.21.1-1.0.6` | `flywheel-neoforge-1.21.1-1.0.6.jar` |
| Fractal | `1.8.0+1.21.1-neoforge` | `Fractal-1.8.0+1.21.1-neoforge.jar` |
| ftb-chunks-neoforge | `2101.1.21` | `ftb-chunks-neoforge-2101.1.21.jar` |
| ftb-library-neoforge | `2101.1.35` | `ftb-library-neoforge-2101.1.35.jar` |
| ftb-quests-neoforge | `2101.1.32` | `ftb-quests-neoforge-2101.1.32.jar` |
| ftb-teams-neoforge | `2101.1.10` | `ftb-teams-neoforge-2101.1.10.jar` |
| ftb-ultimine-neoforge | `2101.1.15` | `ftb-ultimine-neoforge-2101.1.15.jar` |
| ftb-xmod-compat-neoforge | `21.1.11` | `ftb-xmod-compat-neoforge-21.1.11.jar` |
| ftbquest_slot_rewards | `1.21.1-1.1.1` | `ftbquest_slot_rewards-1.21.1-1.1.1.jar` |
| ftbquestsentityvis | `1.7.0-1.21.1-neoforge` | `ftbquestsentityvis-1.7.0-1.21.1-neoforge.jar` |
| ftbquestslangsplitter | `1.0.6` | `ftbquestslangsplitter-1.0.6.jar` |
| geckolib-neoforge | `1.21.1-4.8.4` | `[库]geckolib-neoforge-1.21.1-4.8.4.jar` |
| hazennstuff | `1.4.0.12` | `hazennstuff-1.4.0.12.jar` |
| hazentouvelib | `1.0.2` | `hazentouvelib-1.0.2.jar` |
| HostileNeuralNetworks | `1.21.1-6.5.0` | `HostileNeuralNetworks-1.21.1-6.5.0.jar` |
| interiors | `1.21.1-neoforge-0.6.1` | `interiors-1.21.1-neoforge-0.6.1.jar` |
| irons_jewelry | `1.21.1-1.6.1.1` | `irons_jewelry-1.21.1-1.6.1.1.jar` |
| irons_lib | `1.21.1-2.1.0` | `irons_lib-1.21.1-2.1.0.jar` |
| irons_spellbooks | `1.21.1-3.16.2` | `irons_spellbooks-1.21.1-3.16.2.jar` |
| iss_magicfromtheeast | `1.1.5` | `iss_magicfromtheeast-1.1.5.jar` |
| JAOPCA | `1.21.1-5.0.13.21` | `[核心]JAOPCA-1.21.1-5.0.13.21.jar` |
| jei | `1.21.1-neoforge-19.27.0.340` | `jei-1.21.1-neoforge-19.27.0.340.jar` |
| kotlin-reflect | `2.3.0` | `kotlin-reflect-2.3.0.jar` |
| kotlin-stdlib | `2.3.0` | `kotlin-stdlib-2.3.0.jar` |
| kotlin-stdlib-jdk7 | `2.3.0` | `kotlin-stdlib-jdk7-2.3.0.jar` |
| kotlin-stdlib-jdk8 | `2.3.0` | `kotlin-stdlib-jdk8-2.3.0.jar` |
| kotlinx-coroutines-core-jvm | `1.10.2` | `kotlinx-coroutines-core-jvm-1.10.2.jar` |
| kotlinx-coroutines-jdk8 | `1.10.2` | `kotlinx-coroutines-jdk8-1.10.2.jar` |
| kotlinx-serialization-core-jvm | `1.9.0` | `kotlinx-serialization-core-jvm-1.9.0.jar` |
| kotlinx-serialization-json-jvm | `1.9.0` | `kotlinx-serialization-json-jvm-1.9.0.jar` |
| kubejs-neoforge | `2101.7.2-build.368` | `kubejs-neoforge-2101.7.2-build.368.jar` |
| L_Ender's Cataclysm | `1.21.1-3.27` | `[核心]L_Ender'sCataclysm1.21.1-3.27.jar` |
| linearbearing | `1.3.5` | `linearbearing-1.3.5.jar` |
| lootjs-neoforge | `1.21.1-3.7.0` | `lootjs-neoforge-1.21.1-3.7.0.jar` |
| maid_storage_manager | `1.15.6` | `maid_storage_manager-1.15.6.jar` |
| mekanicalcreate | `0.2.6-mc1.21.1-neoforge` | `mekanicalcreate-0.2.6-mc1.21.1-neoforge.jar` |
| Mekanism | `1.21.1-10.7.19.85` | `[核心]Mekanism-1.21.1-10.7.19.85.jar` |
| MekanismGenerators | `1.21.1-10.7.19.85` | `[核心]MekanismGenerators-1.21.1-10.7.19.85.jar` |
| modonomicon | `1.21.1-neoforge-1.120.3` | `modonomicon-1.21.1-neoforge-1.120.3.jar` |
| neoecoae | `21.1.1` | `neoecoae-21.1.1.jar` |
| neoguanniao | `2.9` | `neoguanniao-2.9.jar` |
| Northstar | `0.6.4+1.21.1` | `Northstar-0.6.4+1.21.1.jar` |
| OELib-neoforge | `1.21.1-0.2.3` | `[核心]OELib-neoforge-1.21.1-0.2.3.jar` |
| oneenoughitem-neoforge | `1.21.1-1.0.8` | `[核心]oneenoughitem-neoforge-1.21.1-1.0.8.jar` |
| PackagedAuto | `1.21.1-4.0.8.21` | `[核心]PackagedAuto-1.21.1-4.0.8.21.jar` |
| papi-neoforge | `1.21.1-1.2.1` | `papi-neoforge-1.21.1-1.2.1.jar` |
| Patchouli | `1.21.1-93-NEOFORGE` | `[核心]Patchouli-1.21.1-93-NEOFORGE.jar` |
| patternbetter | `1.3.3-1.21.1` | `patternbetter-1.3.3-1.21.1.jar` |
| Placebo | `1.21.1-9.9.1` | `[库]Placebo-1.21.1-9.9.1.jar` |
| player-animation-lib-forge | `2.0.4+1.21.1` | `[基础]player-animation-lib-forge-2.0.4+1.21.1.jar` |
| questshop | `1.3.0` | `questshop-1.3.0.jar` |
| ramization | `1.0.0` | `ramization-1.0.0.jar` |
| Registrate | `MC1.21-1.3.0+67` | `Registrate-MC1.21-1.3.0+67.jar` |
| revelationary-neoforge | `1.4.1+1.21.1` | `[库]revelationary-neoforge-1.4.1+1.21.1.jar` |
| ruok-neoforge | `1.21.1_Pre-Release_5-1.7.4` | `ruok-neoforge_1.21.1_Pre-Release_5-1.7.4.jar` |
| sampleintegration | `1.0.8+1.21.1` | `sampleintegration-1.0.8+1.21.1.jar` |
| silent-gear | `1.21.1-neoforge-4.2.1.1` | `silent-gear-1.21.1-neoforge-4.2.1.1.jar` |
| silent-lib | `1.21.1-neoforge-10.6.0` | `[库]silent-lib-1.21.1-neoforge-10.6.0.jar` |
| silentgems | `1.21.1-neoforge-5.1.3` | `[核心]silentgems-1.21.1-neoforge-5.1.3.jar` |
| solcarrot | `1.21.1-1.16.6` | `solcarrot-1.21.1-1.16.6.jar` |
| spectrum | `1.12.4-1.21.1-neo` | `spectrum-1.12.4-1.21.1-neo.jar` |
| stabilized | `1.0.0` | `[x]stabilized-1.0.0.jar` |
| supplementaries-neoforge | `1.21.1-3.6.4` | `[核心]supplementaries-neoforge-1.21.1-3.6.4.jar` |
| thedarkcolour.kfflang | `5.11.0` | `thedarkcolour.kfflang-5.11.0.jar` |
| thedarkcolour.kfflib | `5.11.0` | `thedarkcolour.kfflib-5.11.0.jar` |
| thedarkcolour.kffmod | `5.11.0` | `thedarkcolour.kffmod-5.11.0.jar` |
| touhou_little_maid_spell | `1.21.1-1.8.1-neoforge` | `touhou_little_maid_spell-1.21.1-1.8.1-neoforge.jar` |
| touhou_lost_maid | `0.0.2` | `touhou_lost_maid-0.0.2.jar` |
| tracks_plus | `1.0.5` | `tracks_plus-1.0.5.jar` |
| trading_floor | `3.0.16` | `trading_floor-3.0.16.jar` |
| TravelersTitles | `1.21.1-NeoForge-5.1.3` | `TravelersTitles-1.21.1-NeoForge-5.1.3.jar` |
| traveloptics | `4.4.0.1-1.21.1` | `traveloptics-4.4.0.1-1.21.1.jar` |
| wcwt（ME 综合工作终端） | `1.3.8` | `wcwt-1.3.8.jar-ME综合工作终端.jar` |
| yammo | `1.0.6` | `yammo-1.0.6.jar` |

## implementation/ —— 40 个 jar（编译期与运行期可见）

| 模组 | 版本 | 文件名 |
|---|---|---|
| advanced_infusion | `0.1.0` | `advanced_infusion-0.1.0.jar` |
| appliedenergistics2 | `19.2.17` | `[核心]appliedenergistics2-19.2.17.jar` |
| attributefix-neoforge | `1.21.1-21.1.3` | `[基础]attributefix-neoforge-1.21.1-21.1.3.jar` |
| bookshelf-neoforge | `1.21.1-21.1.81` | `[库]bookshelf-neoforge-1.21.1-21.1.81.jar` |
| bountiful-neoforge | `8.0.0-beta.2` | `bountiful-neoforge-8.0.0-beta.2.jar` |
| create | `1.21.1-6.0.10` | `[核心]create-1.21.1-6.0.10.jar` |
| create-aeronautics-bundled | `1.21.1-1.3.1` | `create-aeronautics-bundled-1.21.1-1.3.1.jar` |
| curios-neoforge | `9.5.1+1.21.1` | `[库]curios-neoforge-9.5.1+1.21.1.jar` |
| emi | `1.1.24+1.21.1+neoforge` | `emi-1.1.24+1.21.1+neoforge.jar` |
| entity_model_features | `3.2.4-1.21-neoforge` | `[基础]entity_model_features-3.2.4-1.21-neoforge.jar` |
| entity_texture_features | `1.21-neoforge-7.1` | `[基础]entity_texture_features_1.21-neoforge-7.1.jar` |
| forbiddenmagic | `1.0.0-port.29` | `forbiddenmagic-1.0.0-port.29.jar` |
| guideme | `21.1.15` | `[核心]guideme-21.1.15.jar` |
| iris-neoforge | `1.8.14-beta.1+mc1.21.1` | `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar` |
| Jade | `1.21.1-NeoForge-15.10.6` | `Jade-1.21.1-NeoForge-15.10.6.jar` |
| jecharacters | `1.21-neoforge-4.5.24` | `jecharacters-1.21-neoforge-4.5.24.jar` |
| kaleidoscopecookery | `1.4.1-neoforge+mc1.21.1` | `kaleidoscopecookery-1.4.1-neoforge+mc1.21.1.jar` |
| kaleidoscopetavern | `1.1.2-neoforge+mc1.21.1` | `kaleidoscopetavern-1.1.2-neoforge+mc1.21.1.jar` |
| kambrik-neoforge | `8.0.0-beta.2` | `kambrik-neoforge-8.0.0-beta.2.jar` |
| kotlinforforge | `5.11.0-all` | `[库]kotlinforforge-5.11.0-all.jar` |
| KubeJS Thaumcraft | `1.1.3+21.1.234` | `KubeJS Thaumcraft-1.1.3+21.1.234.jar` |
| L_Ender's Cataclysm | `1.21.1-3.31` | `L_Ender's Cataclysm 1.21.1-3.31.jar` |
| lionfishapi | `3.0` | `lionfishapi-3.0.jar` |
| lootr-neoforge | `1.21.1-1.11.38.123` | `lootr-neoforge-1.21.1-1.11.38.123.jar` |
| nodalmechanics | `1.0.0-port.6` | `nodalmechanics-1.0.0-port.6.jar` |
| prickle-neoforge | `1.21.1-21.1.11` | `[库]prickle-neoforge-1.21.1-21.1.11.jar` |
| Re-Avaritia-neoforge | `1.21.1-1.3.9.9-release` | `Re-Avaritia-neoforge-1.21.1-1.3.9.9-release.jar` |
| renderblender | `1.0.0` | `renderblender-1.0.0.jar` |
| sable-neoforge | `1.21.1-2.0.3` | `sable-neoforge-1.21.1-2.0.3.jar` |
| scex-thaumcraft-aspects-jei | `1.21.1-1.0.0-rc5` | `scex-thaumcraft-aspects-jei-1.21.1-1.0.0-rc5.jar` |
| sodium-neoforge | `0.8.13-beta.2+mc1.21.1` | `sodium-neoforge-0.8.13-beta.2+mc1.21.1.jar` |
| taintedmagic | `1.0.0-port.29` | `taintedmagic-1.0.0-port.29.jar` |
| thaumcraft | `0.2.2.95-port.242` | `thaumcraft-0.2.2.95-port.242.jar` |
| thaumcraftcelestial | `1.0.0-port.24` | `thaumcraftcelestial-1.0.0-port.24.jar` |
| thaumic_tinkerer | `1.0.0-port.59` | `thaumic_tinkerer-1.0.0-port.59.jar` |
| thaumicbases | `2.1.189.32-port.6` | `thaumicbases-2.1.189.32-port.6.jar` |
| ThaumicEnergistics | `1.21.1-neoforge-2.3.15-alpha` | `ThaumicEnergistics-1.21.1-neoforge-2.3.15-alpha.jar` |
| toomanyrecipeviewers | `0.9.0+mc.21.1` | `toomanyrecipeviewers-0.9.0+mc.21.1.jar` |
| worldedit-mod | `7.3.8` | `[基础]worldedit-mod-7.3.8.jar` |
| yeetusexperimentus-neoforge | `87.0.0` | `yeetusexperimentus-neoforge-87.0.0.jar` |

## reference/ —— 15 个 jar（仅供人工查阅，不参与编译）

| 模组 | 版本 | 文件名 |
|---|---|---|
| gadomancy | `1.4.8` | `gadomancy-1.4.8.jar` |
| tcnodetracker | `1.4.0` | `tcnodetracker-1.4.0.jar` |
| Thaumcraft | `1.7.10-4.2.3.5a` | `Thaumcraft-1.7.10-4.2.3.5a.jar` |
| thaumcraft | `0.2.2.95-port.152` | `[神秘时代]thaumcraft-0.2.2.95-port.152.jar` |
| Thaumcraft4Tweaks | `1.5.39` | `Thaumcraft4Tweaks-1.5.39.jar` |
| ThaumcraftResearchTweaks | `1.3.0` | `ThaumcraftResearchTweaks-1.3.0.jar` |
| Thaumic Machina | `1.7.10-0.2.1` | `Thaumic Machina-1.7.10-0.2.1.jar` |
| Thaumic-Based | `1.8.13` | `Thaumic-Based-1.8.13.jar` |
| Thaumic-Exploration | `1.4.8-GTNH` | `Thaumic-Exploration-1.4.8-GTNH.jar` |
| thaumicboots | `1.4.14` | `thaumicboots-1.4.14.jar` |
| thaumicenergistics | `1.7.14-GTNH` | `thaumicenergistics-1.7.14-GTNH.jar` |
| ThaumicHorizons | `1.7.9` | `ThaumicHorizons-1.7.9.jar` |
| thaumicinsurgence | `0.4.0` | `thaumicinsurgence-0.4.0.jar` |
| WarpTheory | `1.5.0-GTNH` | `WarpTheory-1.5.0-GTNH.jar` |
| WitchingGadgets | `1.7.25-GTNH` | `WitchingGadgets-1.7.25-GTNH.jar` |

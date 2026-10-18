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
- 本目录是编译依赖的**唯一来源**：`build.gradle` 只留 `icu4j`、`ponder`（提供 `net.createmod.catnip`）、
  `glsl-processor` 这几个纯库，以及整合包里也没有的 `optitritium`/`rrls`/`raritycore`；
  其余模组一律通过 `fileTree` 从本目录取。模组版本以整合包实例的 `mods/` 目录为准，
  更新模组时把新 jar 换进来、`build.gradle` 不用动。


## compileOnly/ —— 195 个 jar（仅编译期可见）

| 模组 | 版本 | 文件名 |
|---|---|---|
| AcademyCraft-neoforge | `0.0.28-scex-machineui` | `AcademyCraft-neoforge-1.21.1-0.0.28-scex-machineui.jar` |
| AI-Improvements | `1.21-0.5.3` | `AI-Improvements-1.21-0.5.3.jar` |
| watut | `1.2.7-neoforge-1.21.1` | `watut-1.2.7-neoforge-1.21.1.jar` |
| AdvancedLootInfo-neoforge | `2.5.0` | `AdvancedLootInfo-neoforge-1.21.1-2.5.0.jar` |
| ae2-pattern-encoding-access-terminal | `1.21.1-1.1.0-rc.4` | `ae2-pattern-encoding-access-terminal-1.21.1-1.1.0-rc.4.jar` |
| AE2-QoL-Client | `2.0.2` | `[C]-aeqc-2.0.2-neoforge-1.21.1.jar` |
| ae2_auto_pattern_upload | `2.0.0` | `ae2_auto_pattern_upload-2.0.0.jar` |
| ae2cs | `1.2.1` | `[S]-ae2cs-1.2.1-neoforge-1.21.1.jar` |
| ae2helpers | `1.0.1` | `ae2helpers-1.0.1.jar` |
| ae_better_villagers | `1.15.1.b` | `ae_better_villagers-1.15.1.b.jar` |
| aeallpattern | `0.2.6` | `[S]-aeallpattern-0.2.6-neoforge-1.21.1.jar` |
| aero_cam_sync | `1.3.1` | `aero_cam_sync-1.3.1.jar` |
| AlltheCompatibility | `1.21.1-(v.3.9.1)` | `AlltheCompatibility-1.21.1-(v.3.9.1).jar` |
| allthecompressed | `1.21.1-4.4.0` | `allthecompressed-1.21.1-4.4.0.jar` |
| alltheores | `3.2.0_neoforge_1.21.1` | `alltheores-3.2.0_neoforge_1.21.1.jar` |
| alshanex_familiars | `1.21.1_v4.0.4` | `alshanex_familiars-1.21.1_v4.0.4.jar` |
| ancientreforging | `1.8.6` | `ancientreforging-1.8.6.jar` |
| Annuus-neoforge | `1.0.16-fi2` | `Annuus-neoforge-1.0.16-fi2.jar` |
| Apotheosis | `8.9.0` | `Apotheosis-1.21.1-8.9.0.jar` |
| apotheosis_things | `2101.1.2` | `[S]-apotheosis_things-2101.1.2-neoforge-1.21.1.jar` |
| ApothicAttributes | `2.11.0` | `ApothicAttributes-1.21.1-2.11.0.jar` |
| ApothicEnchanting | `1.6.4` | `ApothicEnchanting-1.21.1-1.6.4.jar` |
| architectury | `13.0.11` | `[S]-architectury-13.0.11-neoforge-1.21.1.jar` |
| avaritia_integration | `1.0` | `avaritia_integration-1.0.jar` |
| BiomesOPlenty-neoforge | `1.21.1-21.1.0.13` | `[核心]BiomesOPlenty-neoforge-1.21.1-21.1.0.13.jar` |
| BuildingWands-neoforge | `MC1.21.1-3.0.5` | `BuildingWands-neoforge-MC1.21.1-3.0.5.jar` |
| carryon-neoforge | `1.21.1-2.2.6.13` | `carryon-neoforge-1.21.1-2.2.6.13.jar` |
| CarryOnAeroCompat | `1.21.1-1.1.1` | `CarryOnAeroCompat-1.21.1-1.1.1.jar` |
| CBC-Military-Supplement | `3.0.6-1.21.1` | `CBC-Military-Supplement-1.21.1-3.0.7.jar` |
| ccbtweaks | `1.0.0-b.28` | `[C]-ccbtweaks-1.0.0-b.28-neoforge-1.21.1.jar` |
| IMBlocker | `5.6.2.1` | `IMBlocker-5.6.2.1-neoforge+1.20.6-1.21.8.jar` |
| certain_questing_additions-neoforge | `1.2.0.4+mc1.21.1` | `certain_questing_additions-neoforge-1.2.0.4+mc1.21.1.jar` |
| cfwinfo | `1.9.1` | `[S]-cfwinfo-1.9.1-neoforge-1.21.1.jar` |
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
| create_fantasizing | `1.2.0-b3` | `[S]-create_fantasizing-1.2.0-b3-neoforge-1.21.1.jar` |
| create_optical | `0.4.2` | `create_optical-0.4.2.jar` |
| create_pattern_schematics | `2.0.10` | `[机械动力：模式蓝图] create_pattern_schematics-2.0.10.jar` |
| create_ratatouille | `1.4.0` | `[S]-ratatouille-1.4.0-neoforge-1.21.1.jar` |
| create_submarine | `3.4.1` | `ALPHA-2 create_submarine-3.4.1.jar` |
| createbigcannons | `5.11.7` | `[S]-createbigcannons-5.11.7-neoforge-1.21.1.jar` |
| createdieselgenerators | `1.21.1-1.3.12` | `createdieselgenerators-1.21.1-1.3.12.jar` |
| CreateDragonsPlus | `1.11.9` | `CreateDragonsPlus-1.11.9.jar` |
| createvoidway | `0.2.4+mc1.21.1` | `createvoidway-0.2.4+mc1.21.1.jar` |
| croptopia-neoforge | `1.21.1-4.2.4` | `[核心]croptopia-neoforge-1.21.1-4.2.4.jar` |
| Cucumber | `1.21.1-8.0.16` | `[库]Cucumber-1.21.1-8.0.16.jar` |
| dev.eriksonn.aeronautics.aeronautics-neoforge | `1.21.1-1.3.0` | `dev.eriksonn.aeronautics.aeronautics-neoforge-1.21.1-1.3.0.jar` |
| dev.simulated_team.simulated.simulated-neoforge | `1.21.1-1.3.0` | `dev.simulated_team.simulated.simulated-neoforge-1.21.1-1.3.0.jar` |
| dyeable-components | `1.0.1+mc1.21.1` | `dyeable-components-1.0.1+mc1.21.1.jar` |
| emilink | `21.1.16` | `emilink-21.1.16.jar` |
| exspectriments | `0.3.0_mapped_moj_1.21.1` | `exspectriments-0.3.0_mapped_moj_1.21.1.jar` |
| ExtendedAE | `1.21-2.2.41-neoforge` | `ExtendedAE-1.21-2.2.41-neoforge.jar` |
| extendedae_plus | `1.6.1` | `extendedae_plus-1.6.1.jar` |
| ExtendedCrafting | `7.0.9` | `ExtendedCrafting-1.21.1-7.0.9.jar` |
| familiarslib | `1.21.1-1.8` | `familiarslib-1.21.1-1.8.jar` |
| fancymenu_neoforge | `3.9.14` | `fancymenu_neoforge_3.9.14_MC_1.21.1.jar` |
| FarmersDelight | `1.3.4` | `FarmersDelight-1.21.1-1.3.4.jar` |
| fastquit | `3.0.0+1.20.6_mapped_moj_1.21.1` | `fastquit-3.0.0+1.20.6_mapped_moj_1.21.1.jar` |
| fidworkblock | `1.0.0` | `fidworkblock-1.0.0.jar` |
| flavor_immersed_daily | `1.21.1-neoforge-2026.10.13` | `flavor_immersed_daily-1.21.1-neoforge-2026.10.13.jar` |
| fluidlogistics | `1.3.1` | `fluidlogistics-1.3.1-mc1.21.1.jar` |
| flywheel-neoforge | `1.21.1-1.0.6` | `flywheel-neoforge-1.21.1-1.0.6.jar` |
| Fractal | `1.8.0+1.21.1-neoforge` | `Fractal-1.8.0+1.21.1-neoforge.jar` |
| ftb-chunks-neoforge | `2101.1.22` | `ftb-chunks-neoforge-2101.1.22.jar` |
| ftb-library-neoforge | `2101.1.37` | `ftb-library-neoforge-2101.1.37.jar` |
| ftb-quests-neoforge | `2101.1.36` | `ftb-quests-neoforge-2101.1.36.jar` |
| ftb-teams-neoforge | `2101.1.11` | `ftb-teams-neoforge-2101.1.11.jar` |
| ftb-ultimine-neoforge | `2101.1.15` | `ftb-ultimine-neoforge-2101.1.15.jar` |
| ftb-xmod-compat-neoforge | `21.1.12` | `ftb-xmod-compat-neoforge-21.1.12.jar` |
| ftbquest_slot_rewards | `1.21.1-1.1.1` | `ftbquest_slot_rewards-1.21.1-1.1.1.jar` |
| ftbquestsentityvis | `1.12.0` | `ftbquestsentityvis-1.12.0-1.21.1-neoforge.jar` |
| ftbquestslangsplitter | `1.0.7` | `ftbquestslangsplitter-1.0.7.jar` |
| geckolib-neoforge | `4.9.3` | `geckolib-neoforge-1.21.1-4.9.3.jar` |
| hazennstuff | `1.4.0.12` | `hazennstuff-1.4.0.12.jar` |
| hazentouvelib | `1.0.2` | `hazentouvelib-1.0.2.jar` |
| HostileNeuralNetworks | `6.5.1` | `[S]-hostilenetworks-6.5.1-neoforge-1.21.1.jar` |
| interiors | `1.21.1-neoforge-0.6.1` | `interiors-1.21.1-neoforge-0.6.1.jar` |
| irons_jewelry | `1.21.1-1.6.1.1` | `irons_jewelry-1.21.1-1.6.1.1.jar` |
| irons_lib | `1.21.1-2.2.0` | `irons_lib-1.21.1-2.2.0.jar` |
| irons_spellbooks | `1.21.1-3.16.3` | `irons_spellbooks-1.21.1-3.16.3.jar` |
| iss_magicfromtheeast | `1.1.5` | `iss_magicfromtheeast-1.1.5.jar` |
| JAOPCA | `5.0.14.23` | `JAOPCA-1.21.1-5.0.14.23.jar` |
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

| linearbearing | `1.3.5` | `linearbearing-1.3.5.jar` |
| lootjs-neoforge | `1.21.1-3.7.0` | `lootjs-neoforge-1.21.1-3.7.0.jar` |
| maid_storage_manager | `1.15.6` | `maid_storage_manager-1.15.6.jar` |
| mekanicalcreate | `0.2.8` | `[S]-mekanicalcreate-0.2.8-neoforge-1.21.1.jar` |
| Mekanism | `1.21.1-10.7.19.85` | `[核心]Mekanism-1.21.1-10.7.19.85.jar` |
| MekanismGenerators | `1.21.1-10.7.19.85` | `[核心]MekanismGenerators-1.21.1-10.7.19.85.jar` |
| modonomicon | `1.120.7` | `modonomicon-1.21.1-neoforge-1.120.7.jar` |
| neoecoae | `21.2.6` | `neoecoae-21.2.6.jar` |
| neoguanniao | `3.6` | `[S]-neoguanniao-3.6-neoforge-1.21.1.jar` |
| Northstar | `0.6.6+1.21.1` | `Northstar-0.6.6+1.21.1.jar` |
| OELib-neoforge | `1.21.1-0.2.3` | `[核心]OELib-neoforge-1.21.1-0.2.3.jar` |
| oneenoughitem-neoforge | `1.21.1-1.0.8` | `[核心]oneenoughitem-neoforge-1.21.1-1.0.8.jar` |
| PackagedAuto | `1.21.1-4.0.8.21` | `[核心]PackagedAuto-1.21.1-4.0.8.21.jar` |
| papi-neoforge | `1.21.1-1.3.0` | `papi-neoforge-1.21.1-1.3.0.jar` |
| Patchouli | `1.21.1-93-NEOFORGE` | `[核心]Patchouli-1.21.1-93-NEOFORGE.jar` |
| patternbetter | `1.3.3-1.21.1` | `patternbetter-1.3.3-1.21.1.jar` |
| Placebo | `9.9.4` | `Placebo-1.21.1-9.9.4.jar` |
| player-animation-lib-forge | `2.0.4+1.21.1` | `[基础]player-animation-lib-forge-2.0.4+1.21.1.jar` |
| questshop | `1.3.0` | `questshop-1.3.0.jar` |
| ramization | `1.0.0` | `ramization-1.0.0.jar` |
| Registrate | `MC1.21-1.3.0+67` | `Registrate-MC1.21-1.3.0+67.jar` |
| revelationary-neoforge | `1.5.2+1.21.1` | `[S]-revelationary-1.5.2-neoforge-1.21.1.jar` |
| ruok-neoforge | `1.21.1_Pre-Release_5-1.7.4` | `ruok-neoforge_1.21.1_Pre-Release_5-1.7.4.jar` |
| sampleintegration | `1.0.8+1.21.1` | `sampleintegration-1.0.8+1.21.1.jar` |
| silent-gear | `1.21.1-neoforge-4.2.1.1` | `silent-gear-1.21.1-neoforge-4.2.1.1.jar` |
| silent-lib | `1.21.1-neoforge-10.6.0` | `[库]silent-lib-1.21.1-neoforge-10.6.0.jar` |
| silentgems | `1.21.1-neoforge-5.1.3` | `[核心]silentgems-1.21.1-neoforge-5.1.3.jar` |
| solcarrot | `1.21.1-1.16.6` | `solcarrot-1.21.1-1.16.6.jar` |
| sophisticatedcore | `1.5.2` | `[S]-sophisticatedcore-1.5.2-neoforge-1.21.1.jar` |
| sophisticatedstorage | `1.5.87` | `[S]-sophisticatedstorage-1.5.87-neoforge-1.21.1.jar` |
| spectrum | `1.12.9-1.21.1-neo` | `spectrum-1.12.9-1.21.1-neo.jar` |
| stabilized | `1.0.1` | `[S]-stabilized-1.0.1-neoforge-1.21.1.jar` |
| supplementaries-neoforge | `1.21.1-4.0.2` | `supplementaries-1.21.1-4.0.2-neoforge.jar` |
| thedarkcolour.kfflang | `5.11.0` | `thedarkcolour.kfflang-5.11.0.jar` |
| thedarkcolour.kfflib | `5.11.0` | `thedarkcolour.kfflib-5.11.0.jar` |
| thedarkcolour.kffmod | `5.11.0` | `thedarkcolour.kffmod-5.11.0.jar` |
| touhou_little_maid_spell | `1.21.1-1.8.1-neoforge` | `touhou_little_maid_spell-1.21.1-1.8.1-neoforge.jar` |
| touhou_lost_maid | `0.0.2` | `touhou_lost_maid-0.0.2.jar` |
| tracks_plus | `1.0.6b7` | `tracks_plus-1.0.6b7.jar` |
| trading_floor | `3.0.16` | `trading_floor-3.0.16.jar` |
| trade_cycling | `1.21.1-1.0.23` | `trade-cycling-neoforge-1.21.1-1.0.23.jar` |
| TravelersTitles | `1.21.1-NeoForge-5.1.3` | `TravelersTitles-1.21.1-NeoForge-5.1.3.jar` |
| traveloptics | `4.4.0.1-1.21.1` | `traveloptics-4.4.0.1-1.21.1.jar` |
| wcwt（ME 综合工作终端） | `1.3.10` | `wcwt-1.3.10.jar` |
| yammo | `1.0.6` | `yammo-1.0.6.jar` |
| itemphysiclite | `1.6.13` | `ItemPhysicLite_NEOFORGE_v1.6.13_mc1.21.1.jar` |
| moonlight | `1.21.1-3.7.2` | `moonlight-1.21.1-3.7.2-neoforge.jar` |
| create_stuff_additions | `2.1.4` | `[S]-create_sa-2.1.4-neoforge-1.21.1.jar` |
| eccentric_tome | `1.21.1-1.2.0` | `[S]-eccentrictome-1.2.0-neoforge-1.21.1.jar` |
| alltheleaks | `1.1.8+1.21.1-neoforge` | `[C]-alltheleaks-1.1.8-neoforge-1.21.1.jar` |
| create_dreams_and_desires | `2.3a-BETA` | `[S]-dndesires-2.3a-BETA-neoforge-1.21.1.jar` |
| vista | `1.21.1-5.5.7` | `[S]-vista-5.5.7-neoforge-1.21.1.jar` |
| modernui | `3.13.0.1` | `[C]-modernui-3.13.0.1-neoforge-1.21.1.jar` |
| ldlib2 | `2.2.29` | `[S]-ldlib2-2.2.29-neoforge-1.21.1.jar` |
| additional_attributes | `1.2.2` | `[S]-additional_attributes-1.2.2-neoforge-1.21.jar` |
| connector | `2.0.0-beta.17` | `[S]-connector-2.0.0-beta.17-neoforge-1.21.1.jar` |
| connectorextras | `1.12.1` | `[S]-connectorextras-1.12.1-neoforge-1.21.1.jar` |
| iceberg | `1.3.2` | `[S]-iceberg-1.3.2-neoforge-1.21.1.jar` |
| legendarytooltips | `1.5.5` | `[C]-legendarytooltips-1.5.5-neoforge-1.21.1.jar` |
| prism | `1.0.11` | `[C]-prism-1.0.11-neoforge-1.21.1.jar` |
| maidbeacon | `1.0.1-bugfix` | `[S]-maidbeacon-1.0.1-bugfix-neoforge-1.21.1.jar` |
| mekmm | `1.3.3` | `[S]-mekmm-1.3.3-neoforge-1.21.1.jar` |
| mekanism_extras | `1.4.0` | `[S]-mekanism_extras-1.4.0-neoforge-1.21.1.jar` |
| igleelib | `1.21.1-1.2.7` | `[S]-igleelib-1.2.7-neoforge-1.21.1.jar` |
| evolvedmekanism | `1.2.1-fix3` | `[S]-evolvedmekanism-1.2.1-fix3-neoforge-1.21.1.jar` |
| mekanism_weaponry | `2.1.5` | `[S]-mekanism_weaponry-2.1.5-neoforge-1.21.1.jar` |
| citresewn | `1.2.2+1.21` | `[C]-citresewn-1.2.2-fabric-1.21.1.jar` |
| colorwheel | `1.3.0+mc1.21.1` | `[C]-colorwheel-1.3.0-neoforge-1.21.1.jar` |
| veil | `1.21.1-4.3.2` | `veil-neoforge-1.21.1-4.3.2.jar`（从整合包 sable 的内嵌 jar 中取出） |
| xaerolib | `1.21.1-1.7.3` | `xaerolib-neoforge-1.21.1-1.7.3.jar`（从整合包 xaerominimap 的内嵌 jar 中取出） |

## implementation/ —— 44 个 jar（编译期与运行期可见）

| 模组 | 版本 | 文件名 |
|---|---|---|
| advanced_infusion | `0.1.0` | `advanced_infusion-0.1.0.jar` |
| appliedenergistics2 | `19.2.17` | `[核心]appliedenergistics2-19.2.17.jar` |
| attributefix-neoforge | `1.21.1-21.1.3` | `[基础]attributefix-neoforge-1.21.1-21.1.3.jar` |
| avaritia_more_items | `1.21.1-NeoForge-beta-1.0.6` | `[S]-avaritia_more_items-beta-1.0.6-neoforge-1.21.1.jar` |
| bookshelf-neoforge | `1.21.1-21.1.81` | `[库]bookshelf-neoforge-1.21.1-21.1.81.jar` |
| bountiful-neoforge | `8.0.0-beta.2` | `bountiful-neoforge-8.0.0-beta.2.jar` |
| create | `1.21.1-6.0.10` | `[核心]create-1.21.1-6.0.10.jar` |
| create-aeronautics-bundled | `1.3.2` | `create-aeronautics-bundled-1.21.1-1.3.2.jar` |
| curios-neoforge | `9.5.1+1.21.1` | `[库]curios-neoforge-9.5.1+1.21.1.jar` |
| emi | `1.1.24+1.21.1+neoforge` | `emi-1.1.24+1.21.1+neoforge.jar` |
| entity_model_features | `3.3.12` | `entity_model_features-3.3.12-1.21-neoforge.jar` |
| entity_texture_features | `7.2.6` | `entity_texture_features-7.2.6-1.21-neoforge.jar` |
| forbiddenmagic | `1.0.0-port.33` | `forbiddenmagic-1.0.0-port.33.jar` |
| guideme | `21.1.19` | `guideme-21.1.19.jar` |
| iris-neoforge | `1.8.14-beta.1+mc1.21.1` | `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar` |
| Jade | `1.21.1-NeoForge-15.10.6` | `Jade-1.21.1-NeoForge-15.10.6.jar` |
| jecharacters | `1.21-neoforge-4.5.24` | `jecharacters-1.21-neoforge-4.5.24.jar` |
| kaleidoscopecookery | `1.6.0-neoforge+mc1.21.1` | `kaleidoscopecookery-1.6.0-neoforge+mc1.21.1.jar` |
| kaleidoscopetavern | `1.2.0-neoforge+mc1.21.1` | `[S]-kaleidoscope_tavern-1.2.0-neoforge-1.21.1.jar` |
| kambrik-neoforge | `8.0.0-beta.2` | `kambrik-neoforge-8.0.0-beta.2.jar` |
| kotlinforforge | `5.11.0-all` | `[库]kotlinforforge-5.11.0-all.jar` |
| KubeJS Thaumcraft | `1.1.3+21.1.234` | `KubeJS Thaumcraft-1.1.3+21.1.234.jar` |
| L_Ender's Cataclysm | `3.33` | `L_Ender's Cataclysm 1.21.1-3.33.jar` |
| lionfishapi | `3.1` | `[S]-lionfishapi-3.1-neoforge-1.21.1.jar` |
| lootr-neoforge | `1.21.1-1.11.38.127` | `lootr-neoforge-1.21.1-1.11.38.127.jar` |
| nodalmechanics | `1.0.0-port.6` | `nodalmechanics-1.0.0-port.6.jar` |
| prickle-neoforge | `1.21.1-21.1.11` | `[库]prickle-neoforge-1.21.1-21.1.11.jar` |
| Re-Avaritia-neoforge | `1.4.2` | `Re-Avaritia-neoforge-1.21.1-1.4.2-release.jar` |
| renderblender | `1.0.0` | `renderblender-1.0.0.jar` |
| sable-neoforge | `2.0.5` | `[S]-sable-2.0.5-neoforge-1.21.1.jar` |
| scex-thaumcraft-aspects-jei | `1.21.1-1.0.0-rc5` | `scex-thaumcraft-aspects-jei-1.21.1-1.0.0-rc5.jar` |
| sodium-neoforge | `0.8.13+mc1.21.1` | `sodium-neoforge-0.8.13+mc1.21.1.jar` |
| taintedmagic | `1.0.0-port.33` | `taintedmagic-1.0.0-port.33.jar` |
| thaumcraft | `0.2.2.95-port.253` | `thaumcraft-0.2.2.95-port.253.jar` |
| thaumcraftcelestial | `1.0.0-port.24` | `thaumcraftcelestial-1.0.0-port.24.jar` |
| thaumic_tinkerer | `1.0.0-port.64` | `thaumic_tinkerer-1.0.0-port.64.jar` |
| thaumicbases | `2.1.189.32-port.21` | `thaumicbases-2.1.189.32-port.21.jar` |
| ThaumicEnergistics | `1.21.1-neoforge-2.3.15-alpha` | `ThaumicEnergistics-1.21.1-neoforge-2.3.15-alpha.jar` |
| thaumichorizons | `1.21.1-0.1.0-scex-dev.41` | `thaumichorizons-1.21.1-0.1.0-scex-dev.41.jar` |
| toomanyrecipeviewers | `0.9.0+mc.21.1` | `toomanyrecipeviewers-0.9.0+mc.21.1.jar` |
| worldedit-mod | `7.3.8` | `[基础]worldedit-mod-7.3.8.jar` |
| yeetusexperimentus-neoforge | `87.0.0` | `yeetusexperimentus-neoforge-87.0.0.jar` |
| touhou_little_maid | `1.5.3-neoforge+mc1.21.1` | `[S]-touhou_little_maid-1.5.3-neoforge-1.21.1.jar` |

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

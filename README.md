# GoldenTweaks

[English](#english) | [中文](#中文)

适用于 Minecraft **1.21.1 / NeoForge** 的大型整合包兼容、优化与联动增强模组。

---

# English

A NeoForge compatibility, bug-fix, optimization and mod-integration patch mod for Minecraft 1.21.1,
designed for modpacks that are meant to be played as long-running worlds rather than short
demonstrations of individual mods.

GoldenTweaks is a **patch layer**: it ships no gameplay overhaul of its own. It focuses on keeping
different mods consistent with one another, removing bugs and unintended friction, unifying items
that several mods duplicate, and providing optional client-side conveniences — without imposing a
single way of playing on every player. Most of GoldenTweaks is not visible as a new gameplay
feature; its main purpose is to make existing mods behave correctly together.

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248+ |
| Java | 21 |
| Version | 4.7.5 |
| Author | Fodoth_jinzi89 |
| License | PolyForm Noncommercial 1.0.0 |

## Contents

- [Design principles](#design-principles)
- [What it does](#what-it-does)
- [Installation](#installation)
- [Configuration](#configuration)
- [How it works](#how-it-works)
- [Building from source](#building-from-source)
- [Project layout](#project-layout)
- [Data & scripts](#data--scripts)
- [Known limitations](#known-limitations)
- [License](#license)

## Design principles

GoldenTweaks is developed around a few principles:

- **Compatibility over content** — most changes exist to make existing mods work together, not to add
  another progression system.
- **Optional over mandatory** — player-facing conveniences are preferably client-side and individually
  configurable.
- **Fix unintended behaviour, preserve intended behaviour** — compatibility patches should remove bugs
  and conflicts without unnecessarily redesigning a mod.
- **Long-term worlds over short-term spectacle** — stability, consistency and small details matter
  even when they are invisible in a showcase.

## What it does

The numbers below describe the scope of maintenance rather than a goal in themselves.

### 1. Compatibility fixes

Per-mod mixin packages that repair crashes, dead features and version drift. `neoforge.mods.toml`
declares **130 optional mod dependencies**; the mixin config plugin (`GoldenTweaksMixinPlugin`)
decides per mixin whether it applies, and gates whole groups on the mods that are loaded, so a
patch is never applied against a mod that is not installed.

| Area | Examples of what is fixed |
|---|---|
| Tech & storage | AE2 (terminal search name cache, facades), AE2 Peat, AE2WTLib, AE2 auto pattern upload, ExtendedAE Plus, NeoECOAE, pipez_optimizer, Mekanism (energy-port safety, multiblock data, compact-space power), Mekanism Extras, Evolved Mekanism chemixer, Mekanism Weaponry, Create (symmetry wand, sequencer, sound scapes, JEI subtypes), Create Diesel Generators, Create Enchantment Industry, Create Submarine, Aeronautics, Copycats, Ponder |
| Magic | Thaumcraft (aspect registry, crucible/infusion/arcane categories, research categories, ore features, overworld biome generation, thaumatorium, jars, taint spread, golem behaviour), Thaumic Tinkerer, Thaumic Bases (alchemical furnace), Thaumcraft Celestial, Astral Sorcery, Irons Spellbooks, Spectrum, Apotheosis / Apotheosis Things / Apothic Enchanting, Forbidden Magic, Eidolon, Alshanex Familiars, AcademyCraft, Traveloptics summons |
| Rendering | Flywheel, Geckolib, ModernUI (emoji font & text render type), Veil, Sable, RenderBlender (cosmic render queue), Iris, Ponder, CMD/PackageCouriers, Astral Sorcery (focal-point column & beam), Thermal (recipe-category backgrounds) |
| QoL & UI | EMI (hidden/duplicate stacks, search kernel), JEI, TMRV (RecipeViewers scroll behaviour), Bountiful, Lootr, Quest Shop localization, FTB Quests (chapter-image items, R/U), FTB Ultimine, Tipsmod, Waystones/Traveler's Titles, Xaero, Building Wands, Carry On, Patchouli |
| Others | Registrate, Placebo, Compact Machines, Jaopca, Eccentric Tome, Watut, CCTweaks, Annus, Tritium, RRLS, Silent Gear material book, NeoGuanNiao, Touhou Little Maid / Lost Maid / Spell, maid storage manager, OEI (One Enough Item) |

A handful of cases cannot be expressed as a mixin (wrong mixin shipped by another mod, language
file splitting, Carry On + Aeronautics interaction); those are handled with ASM bytecode edits
driven from the mixin config plugin.

### 2. Cross-mod integration

GoldenTweaks ships a large data pack (`data/goldentweaks/`, ~23.7k JSON files) that bridges mods
without requiring KubeJS or a custom pack — connecting otherwise independent mods into the same
world:

- **Item unification (OEI)** — merges the ores, dusts, gems, crops and foods that several mods
  duplicate onto one canonical provider, so a pack mixing many tech and magic mods does not end up
  with five kinds of silver ingot. Includes a startup self-check and an optional self-heal reload
  (`data/oei/replacements/`)
- **Thaumcraft** — 20 451 item aspects across 275 namespaces, 624 entity aspects,
  18 crucible recipes, 6 infusion recipes, arcane crafting recipes, 38 aspect definitions and
  9 custom researches (Brain Jar research, Thaumic Microscopy storage scanning, Shard Alchemy,
  Primordial Replication, Runed Tablet, Crimson Rites, Ultimate Ingot, Infusion Intercepter,
  Warp Theory Cleanser), plus a reworked Thaumonomicon "aspect knowledge" page with
  scanned-source lists and JEI/EMI aspect page limiting
- **Silent Gear** — 663 generated materials (586 of them cross-mod `compat/*` materials) so
  other mods' metals, gems and woods can be used as tool parts
- **Create** — mechanical crafting, sequenced assembly and extended crafting recipes
- **Extended Crafting / Spectrum / Alshanex Familiars / Touhou Little Maid Spell** —
  additional table recipes for cross-mod items
- **Compact Machines** — 144 room templates and controllable mob spawning inside rooms
- **Quest Shop** — five tiers of gold coins with configurable multipliers, shop category/entries
- **Bountiful** — bounty pools and decrees for 37 professions (alchemist, arcanist, AE2 engineer,
  mechanic, wizard, chef, …)
- **AE2 auto pattern upload** — provider selection screen, icon sync and network packets

### 3. Log hygiene

GoldenTweaks suppresses known, repetitive and non-actionable messages while leaving actionable errors
visible. **47 mixins** under `mixin/shut/` silence benign output that otherwise floods `latest.log` in
large packs: sound engine and sound manager warnings, model/sprite/blockstate loading noise, shader
pipeline errors, Realms availability checks when offline, recipe manager and tag loader decode errors,
and the periodic update notices / debug logs of a number of mods. Where a mod's own logger is worth
keeping, its output is forwarded through GoldenTweaks with throttling and de-duplication instead —
DEBUG/TRACE dropped, WARN/ERROR kept.

### 4. Optional client features

These features are intentionally client-side and configurable per player. They are not intended to
define how an entire modpack should be played.

| Feature | Description |
|---|---|
| Right-click pickup | Pick up nearby dropped items with right-click, including experience orbs. The whole feature is client-side and per-player (synced to the server, which clamps it), with reach, target cap, standing / sneaking / full-inventory behaviour configurable |
| Lootr quick loot | Hold right-click to take loot from Lootr containers, with open animation, flying items and pickup grouping options |
| EMI fast search | Replaces EMI's search kernel with a 1–3 gram inverted index and bitset evaluation instead of materialising candidate lists; also skips building vanilla's suffix-array index (`emiFastSearch`, `emiSkipVanillaSearchIndex`) |
| GUI inspector | Shift + middle-click dumps the hovered / held item (id, components, NBT) to the log, optionally to the clipboard |
| Semantic font wrap | Width-aware wrapping for CJK text in vanilla widgets and tooltips |
| Astral Sorcery focal column | Redraws the Astral Sorcery focal-point column with a vanilla render type where Astral Sorcery's own shader draws no pixels |
| Building Wands preview | Configurable block preview for Building Wands |
| Window follows launcher | Re-applies the launcher's `--width/--height` once on startup, where NeoForge's early window can pin the size instead |

### 5. Balance & gameplay adjustments

All of these are config toggles with sane defaults:

- **Always edible** — all food can be eaten regardless of hunger; food-with-effect semantics are preserved
- **Lost Maid drop** — Touhou Lost Maid drops a configurable set of items on death (chance configurable)
- **Garbage station** — a village structure block that periodically fills nearby trash cans with random daily loot (Kaleidoscope Cookery integration)
- **Trash can** — Kaleidoscope Cookery trash can tweaks
- **Thaumcraft golem simplification** — golems harvest, chop and fish in place instead of pathfinding, avoiding golems that can never reach their target in the ported version
- **Thaumcraft taint spread** — stops taint spreading, with separate toggles for pure / sinister / tainted node biome conversion
- **Compact-space power** — machines inside Compact Machines treat the compact dimension as full daylight; the output fraction is configurable and applied uniformly to Mekanism and its addon solar / wind machines
- **AcademyCraft** — ability damage scaling from Silent Gear `magic_damage`, and skills that respect FTB Chunks claim protection instead of bypassing it
- **Evolved Mekanism** solar panel output multiplier
- **Mekanism Extras** recycler factory stack upgrades from Absolute tier up
- **Cataclysm** balance adjustments
- **Advanced Infusion** item disabling
- **Irons Jewelry** haggler discount caps (count and percentage)
- **Thaumcraft** caching (object tags, crucible recipes, research tree), arcane crafting cache size,
  Thaumonomicon aspect-source paging, arcane workbench vanilla-recipe support, Earth Shock harm mode

### 6. Localization

Built-in Simplified Chinese (`zh_cn`) language files for **114 mod namespaces** (including guide
book page text for AE2, Croptopia, PackagedAuto and FTB Chunks), plus GoldenTweaks' own strings in
both `zh_cn` and `en_us` (~1150 keys each).

## Installation

GoldenTweaks can be installed on both dedicated servers and clients. Server-side compatibility fixes
are independent of the optional client-side features; those features are configured independently by
each player and do not require the entire server to adopt the same settings.

1. Install Java 21 and NeoForge for Minecraft 1.21.1 on the machine that should run it.
2. Drop `goldentweaks-<version>.jar` into the `mods/` folder of the server and/or the client.
3. Optional (client): install [Cloth Config](https://modrinth.com/mod/cloth-config) to get the in-game
   config screen. Without it the config file still works, it is just edited by hand.

The mod works with no other mods installed — every compatibility patch activates only when its
target mod is present.

## Configuration

Two NeoForge configs are registered (`config/goldentweaks-client.toml`,
`config/goldentweaks-common.toml`), grouped as:

| Group | Contents |
|---|---|
| `pickup` (client) | Right-click pickup reach, target cap, delay threshold, standing / sneaking / full-inventory / continuous pickup, experience orbs, Lootr quick-loot options — each player sets their own and the client syncs them to the server |
| `feature` | Always-edible food, Lost Maid death drops |
| `balance` | Thaumcraft golems / taint, Mekanism solar, Mekanism Extras stacking, compact-space power, Cataclysm balance, Advanced Infusion items, Irons Jewelry haggler, AcademyCraft skills |
| `oei` | Item-unification startup self-check and self-heal reload |
| `misc` (client) | Building Wands block preview, EMI fast search & vanilla-index skip, Astral Sorcery focal column, window follows launcher |
| `debug` (client) | GUI inspector and clipboard copy |

All values are readable without a config library: the code falls back to defaults while the config
is not loaded yet, so tweaks never crash on a missing/edited file.

## How it works

- **Mixins** (`goldentweaks.mixins.json`) — 363 mixins: 229 common + 134 client-only, split into
  `balance/`, `feature/`, `fix/`, `optimization/` and `shut/` packages that mirror the target mod's
  package layout.
- **Mixin config plugin** (`GoldenTweaksMixinPlugin`) — decides per mixin whether it applies, based
  on the loaded mod list (`FMLLoader`), and performs the few ASM transformations that mixins cannot
  express.
- **Bootstrap** (`GoldenTweaks`) — registers configs, items, block entities and event handlers for
  the mods that are actually present.
- **Data pack** — recipes, item/entity aspects, Silent Gear materials, bounty pools, tags, loot
  tables, item-unification rules and structure additions.
- **Networking** — a small set of C2S/S2C packets for pickup requests, per-player pickup settings,
  consumable sync, material book opening and pattern-provider icons.

## Building from source

```bash
git clone https://github.com/Fodoth-jinzi89/GoldenTweaks.git
cd GoldenTweaks
./gradlew build            # jar in build/libs/
./gradlew runClient        # dev client
./gradlew runServer        # dev server
./gradlew genIntellijRuns  # IDE run configurations
```

Requires JDK 21. Gradle, NeoForge and Parchment versions live in `gradle.properties`.

> **`libs/` is not shipped.** The build compiles against a set of third-party mod jars
> (`libs/compileOnly`, `libs/implementation`, `libs/runtimeOnly`) that are deliberately **not**
> distributed with this repository — see [`libs/README.md`](libs/README.md) for the expected layout.
> A fresh clone will fail at `compileJava` with unresolved symbols until those jars are provided.

Continuous integration: `.github/workflows/gradle-publish.yml` builds the mod and, when a release is
published, attaches `build/libs/*.jar` to that release.

## Project layout

```
src/main/java/net/fodoth/skina/goldentweaks/
├── GoldenTweaks.java     # mod entry point, mod-presence bootstrap
├── compat/               # per-mod integration: items, blocks, recipes, events, ASM helpers
│   ├── ae2autopatternupload/  alshanex_familiars/  astralsorcery/  cataclysm/  compactmachines/
│   ├── create/  ftbquests/  kaleidoscope/  lootr/  mekanism/  neoecoae/  oei/
│   ├── questshop/  renderblender/  snack_cabinet/  thaumcraft/  thaumicbases/  thermal/  ...
├── config/               # config specs + Cloth Config screen
├── debug/                # GUI inspector
├── event/                # NeoForge event handlers
├── mixin/
│   ├── GoldenTweaksMixinPlugin.java   # per-mixin gating + ASM edits
│   ├── balance/          # numeric / mechanical balance tweaks
│   ├── feature/          # new gameplay behaviour
│   ├── fix/              # bug & crash fixes (one package per mod)
│   ├── optimization/     # general optimizations
│   └── shut/             # log / spam suppression
├── network/              # packets + client handlers (pickup settings sync)
└── util/                 # shared helpers (emi, tmrv, debug, ...)

src/main/resources/
├── goldentweaks.mixins.json
├── assets/               # own assets + zh_cn language files for other mods
└── data/                 # own data + compat data (aspects, materials, recipes, unification)

script/                   # python generators / data pipelines (not shipped in the jar)
```

## Data & scripts

`script/` contains the offline generators used to maintain the data pack. They read mod jars and
resource lists and write JSON into `src/main/resources/data/`:

| Script | Purpose |
|---|---|
| `script/resource_locations/` | Extracts the item list of each mod from its jar and maintains `data/goldentweaks/recipe/thaumcraft/aspects/<mod>/<item>.json` |
| `script/silentgear/` | Table-driven generator for Silent Gear materials |
| `script/thaumcraft/` | Aspect list maintenance and Thaumcraft research/recipe helpers |
| `script/oei/` | Item-unification rule generation for ores, dusts, crops and foods |
| `script/bountiful/` | Bounty pool / decree generation (pricing, professions) |
| `script/materials/`, `script/translation/`, `script/analysis/`, ... | Support tools and notes |

Data changes that rename an id (for example a Silent Gear material) can invalidate already-crafted
items in existing worlds — check `update_log.md` before upgrading a live server.

## Known limitations

- NeoForge 1.21.1 only. There is no Fabric build and none is planned.
- Compatibility patches target specific mod versions. Mod updates can invalidate them; each release
  notes what changed in [`update_log.md`](update_log.md).
- Mixins are gated by mod presence, but a *supported* mod running a very different version may still
  cause errors — report the mod list and `latest.log` when that happens.
- The bundled `zh_cn` files are community-quality translations of third-party mods; if the upstream
  mod ships its own translation, both are merged by the resource pack system.

## License

[PolyForm Noncommercial License 1.0.0](LICENSE) © Fodoth_jinzi89.

You may use, modify and redistribute this source for noncommercial purposes; commercial
distribution requires the author's permission.

Third-party mod jars used only for compilation are **not** part of this repository and remain under
their own licenses.

---

# 中文

适用于 Minecraft **1.21.1 / NeoForge** 的大型整合包兼容、优化与联动增强模组。

GoldenTweaks 是一层「补丁」：它本身不重做玩法。它关注让不同模组彼此保持一致、消除 Bug 与非预期的
摩擦、统一被多个模组重复注册的物品，并把面向玩家的便利功能尽量做成客户端可选项 —— 而不是强制所有
玩家采用同一种玩法。GoldenTweaks 的大部分内容不会以「新增玩法」的形式出现，它的主要目的是让已有的
模组能够正确地协同工作。专为希望被长期游玩、而不只是展示单个模组内容的整合包设计。

| | |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.248+ |
| Java | 21 |
| 版本 | 4.7.5 |
| 作者 | Fodoth_jinzi89 |
| 许可证 | PolyForm Noncommercial 1.0.0 |

## 目录

- [设计原则](#设计原则)
- [功能概览](#功能概览)
- [安装](#安装)
- [配置](#配置)
- [实现方式](#实现方式)
- [从源码构建](#从源码构建)
- [项目结构](#项目结构)
- [数据与脚本](#数据与脚本)
- [已知限制](#已知限制)
- [许可证](#许可证)

## 设计原则

GoldenTweaks 的开发遵循几个原则：

- **兼容优先于内容** —— 大多数修改用于让已有模组共同工作，而不是再增加一套独立的成长系统。
- **可选而非强制** —— 面向玩家的便利功能尽可能放在客户端，并允许每名玩家独立配置。
- **修复非预期行为，而非随意重做玩法** —— 兼容补丁应当消除 Bug 与冲突，而不是在没有必要时重新
  设计目标模组。
- **长期世界优先于短期展示** —— 稳定性、一致性和细节即使无法出现在宣传视频中，也同样值得维护。

## 功能概览

以下数字用于说明维护范围，而不是项目本身的目标。

### 1. 兼容修复

每个模组一个 mixin 分包，修复崩溃、失效功能与版本漂移。`neoforge.mods.toml` 里声明了
**130 个可选依赖**，mixin 配置插件（`GoldenTweaksMixinPlugin`）按已加载的模组逐个决定 mixin
是否生效，并整组开关与模组绑定的兼容补丁，因此不会把补丁作用在未安装的模组上。

| 领域 | 修复内容举例 |
|---|---|
| 科技与存储 | AE2（终端搜索名称缓存、伪装板）、AE2 Peat、AE2WTLib、AE2 自动样板上传、ExtendedAE Plus、NeoECOAE、pipez_optimizer、Mekanism（能量端口安全、多方块数据、紧凑空间供电）、Mekanism Extras、Evolved Mekanism 化学混合器、Mekanism Weaponry、Create（对称之杖、序列组装、音景、JEI 子类型）、Create 柴油发电机、Create 附魔工业、Create 潜艇、航空学、Copycats、Ponder |
| 魔法 | 神秘时代（要素注册、坩埚/注魔/奥术分类、研究分类、矿石特征、主世界群系生成、神秘炼药炉、罐子、腐化蔓延、傀儡行为）、神秘工匠、神秘基础学（炼金炉）、天象神秘学、星辉魔法、Iron's Spellbooks、Spectrum、Apotheosis 系列、禁忌魔法、Eidolon、Alshanex Familiars、AcademyCraft、Traveloptics 召唤物 |
| 渲染 | Flywheel、Geckolib、ModernUI（emoji 字体与文本渲染类型）、Veil、Sable、RenderBlender（宇宙渲染队列）、Iris、Ponder、CMPackageCouriers、星辉魔法（降星点光柱与光束）、Thermal（配方分类背景） |
| 体验与界面 | EMI（隐藏/重复堆叠、搜索内核）、JEI、TMRV（配方界面滚动）、Bountiful、Lootr、任务商店本地化、FTB Quests（章节图片物品、R/U）、FTB Ultimine、Tipsmod、旅行者标题/路标石、Xaero、建筑魔杖、Carry On、Patchouli |
| 其他 | Registrate、Placebo、Compact Machines、Jaopca、Eccentric Tome、Watut、CCTweaks、Annus、Tritium、RRLS、Silent Gear 材料书、NeoGuanNiao、东方小女仆 / Lost Maid / Spell、女仆存储管理、OEI（One Enough Item） |

少数场景无法用 mixin 表达（别的模组自带写错的 mixin、语言文件拆分、Carry On 与航空学的交互），
这些在 mixin 配置插件里用 ASM 字节码改写处理。

### 2. 跨模组联动

GoldenTweaks 自带一份大型数据包（`data/goldentweaks/`，约 2.37 万个 JSON），无需 KubeJS 或自定义
整合包即可桥接模组 —— 把原本互相独立的模组接进同一个世界：

- **物品统一（OEI）** —— 把被多个模组重复注册的矿石、粉末、宝石、作物与食物归并到同一个规范提供方，
  让混装大量科技/魔法模组的整合包不再出现五种银锭。附带启动自检与可选的自愈重载
  （`data/oei/replacements/`）
- **神秘时代** —— 275 个命名空间共 **20451 条物品要素**、624 条实体要素、18 个坩埚配方、
  6 个注魔配方、奥术合成配方、38 个要素定义，以及 9 项自定义研究（缸中研究者、魔导显微扫描、
  碎片炼金、原始珍珠复制、符文石板、绯红仪式、终极锭注魔、注魔拦截器、扭曲理论净化）；
  另重做魔导手册「要素知识」页（要素来源列表、悬停显示已扫描物品、JEI/EMI 页数限制）
- **Silent Gear** —— 生成 663 个材料（其中 586 个是跨模组 `compat/*` 材料），让其它模组的
  金属、宝石、木材可以直接做工具部件
- **机械动力** —— 机械合成、序列组装、组合合成配方
- **Extended Crafting / Spectrum / Alshanex Familiars** —— 跨模组工作台配方
- **Compact Machines** —— 144 个房间模板，并可控制房间内生物生成
- **任务商店** —— 五档金币（倍率可配）与商店分类/条目
- **Bountiful** —— 37 个职业的悬赏池与悬赏令（炼金术士、秘术师、AE2 工程师、机械师、巫师、厨师等）
- **AE2 自动样板上传** —— 样板供应商选择界面、图标同步与网络包

### 3. 日志卫生

GoldenTweaks 会抑制已知的、重复出现且无需用户处理的日志，同时保留需要实际处理的错误。
`mixin/shut/` 下 **47 个 mixin** 负责静默大型整合包中会刷爆 `latest.log` 的无害输出：音效引擎与
音效管理器告警、模型/贴图/方块状态加载噪音、着色器管线报错、离线时的 Realms 可用性检查、配方管理器
与标签加载的解码错误，以及一批模组的更新提示与调试日志。对于仍有保留价值的模组日志，则改为经
GoldenTweaks 转发并做节流/去重 —— 丢弃 DEBUG/TRACE、保留 WARN/ERROR，而不是整段静音。

### 4. 可选的客户端功能

这些功能有意设计为客户端功能，并由每名玩家独立配置。它们不用于规定整个整合包应当采用的玩法方式。

| 功能 | 说明 |
|---|---|
| 右键拾取 | 右键拾取附近掉落物，包含经验球。整个功能在客户端、按玩家独立配置（同步给服务端，服务端会做范围裁剪），拾取距离、目标上限、非潜行/潜行/背包已满行为均可配置 |
| Lootr 快速拾取 | 按住右键直接拿取 Lootr 容器内战利品（开箱动画、飞行动画、拾取分组可配） |
| EMI 快速搜索 | 用 1~3 gram 倒排索引 + 位集求值替换 EMI 原搜索内核，不再物化候选列表；并可跳过原版后缀数组索引的构建（`emiFastSearch`、`emiSkipVanillaSearchIndex`） |
| GUI 调试器 | Shift + 鼠标中键把悬浮/手持物品（id、数据组件、NBT）打印到日志，可复制到剪贴板 |
| 语义字体换行 | 原版界面与提示框里的 CJK 文本按宽度正确换行 |
| 星辉降星点光柱 | 星辉魔法自身着色器画不出像素时，改用原版渲染类型补画降星点光柱 |
| 建筑魔杖预览 | 建筑魔杖的方块预览可配置 |
| 窗口跟随启动器 | 启动后重设一次窗口尺寸为启动器传入的 `--width/--height`，避免被 NeoForge 早期窗口锁定 |

### 5. 平衡与玩法调整

全部为可配置开关，默认值保守：

- **始终可食用** —— 无视饥饿度即可食用所有食物，并保留带效果食物的语义
- **女仆死亡掉落** —— 东方 Lost Maid 死亡时掉落可配置的物品（概率可配）
- **垃圾站** —— 与森罗万象·厨艺联动的村庄结构方块，每天向附近垃圾桶随机填充物品
- **垃圾桶增强** —— 森罗万象·厨艺垃圾桶行为调整
- **神秘时代傀儡简化** —— 傀儡原地完成收获/伐木/钓鱼，不再寻路，规避移植版傀儡走不到目标的问题
- **神秘时代腐化蔓延** —— 停止腐化扩散，并为纯净/邪恶/腐化节点的群系转化各自提供独立开关
- **紧凑空间供电** —— 紧凑空间维度内的机器视为白天，输出比例可配，并统一覆盖 Mekanism 及其附属的
  太阳能/风力机组
- **AcademyCraft** —— 技能伤害按 Silent Gear 的 `magic_damage` 增伤，且技能会遵守 FTB Chunks 领地保护
- **Evolved Mekanism** 太阳能板发电倍率
- **Mekanism Extras** 回收工厂在绝对等级及以上支持堆叠升级
- **灾变（Cataclysm）** 数值平衡
- **Advanced Infusion** 物品禁用
- **Irons Jewelry** 讲价折扣上限（次数与百分比）
- **神秘时代** 缓存（对象标签、坩埚配方、研究树）、奥术合成缓存大小、魔导手册要素来源分页、
  奥术工作台支持原版配方、地裂伤害模式

### 6. 本地化

内置 **114 个模组命名空间**的简体中文语言文件（含 AE2、Croptopia、PackagedAuto、FTB Chunks 等的
指南书正文），GoldenTweaks 自身的中英文本各约 1150 个键。

## 安装

GoldenTweaks 既可装在专用服务器上，也可装在客户端。服务端的兼容修复与可选的客户端功能相互独立；
这些客户端功能由每名玩家独立配置，不需要整个服务器采用同一套设置。

1. 在需要运行的机器上安装 Java 21 与 Minecraft 1.21.1 对应的 NeoForge。
2. 把 `goldentweaks-<版本>.jar` 放进服务器和/或客户端的 `mods/` 目录。
3. 可选（客户端）：安装 [Cloth Config](https://modrinth.com/mod/cloth-config) 以获得游戏内配置
   界面；不装也能用，只是需要手动编辑配置文件。

兼容补丁只在目标模组存在时生效，可以直接加入现有整合包。

## 配置

注册两份 NeoForge 配置（`config/goldentweaks-client.toml`、`config/goldentweaks-common.toml`）：

| 分组 | 内容 |
|---|---|
| `pickup`（客户端） | 右键拾取距离、目标上限、延迟阈值、非潜行/潜行/背包已满/连续拾取、经验球、Lootr 快速拾取选项 —— 每名玩家各自配置，客户端会同步给服务端 |
| `feature` | 始终可食用、女仆死亡掉落 |
| `balance` | 神秘时代傀儡与腐化、太阳能倍率、Mekanism Extras 堆叠、紧凑空间供电、灾变平衡、Advanced Infusion 物品、Irons Jewelry 讲价、AcademyCraft 技能 |
| `oei` | 物品统一启动自检与自愈重载 |
| `misc`（客户端） | 建筑魔杖方块预览、EMI 快速搜索与跳过原版索引、星辉降星点光柱、窗口跟随启动器 |
| `debug`（客户端） | GUI 调试器与剪贴板复制 |

所有取值在配置尚未加载时会回落到默认值，因此改动配置文件不会导致启动崩溃。

## 实现方式

- **Mixin**（`goldentweaks.mixins.json`）—— 共 363 个：229 个通用 + 134 个仅客户端，按
  `balance/`、`feature/`、`fix/`、`optimization/`、`shut/` 分包，并镜像目标模组的包结构。
- **Mixin 配置插件**（`GoldenTweaksMixinPlugin`）—— 依据已加载模组列表（`FMLLoader`）逐个决定
  mixin 是否生效，并承担少数无法用 mixin 表达的 ASM 改写。
- **入口**（`GoldenTweaks`）—— 注册配置、物品、方块实体与事件处理器，全部以「模组存在」为前提。
- **数据包** —— 配方、物品/实体要素、Silent Gear 材料、悬赏池、标签、战利品表、物品统一规则与结构追加。
- **网络** —— 少量 C2S/S2C 数据包：拾取请求、按玩家的拾取设定、消耗品同步、材料书打开、样板供应商图标。

## 从源码构建

```bash
git clone https://github.com/Fodoth-jinzi89/GoldenTweaks.git
cd GoldenTweaks
./gradlew build            # 产物在 build/libs/
./gradlew runClient        # 开发客户端
./gradlew runServer        # 开发服务端
./gradlew genIntellijRuns  # 生成 IDE 运行配置
```

需要 JDK 21。Gradle、NeoForge、Parchment 版本见 `gradle.properties`。

> **仓库不含 `libs/`。** 编译需要一批第三方模组 jar（`libs/compileOnly`、`libs/implementation`、
> `libs/runtimeOnly`），它们**刻意不随本仓库分发**，目录约定见 [`libs/README.md`](libs/README.md)。
> 新克隆的仓库在补齐这些 jar 之前会在 `compileJava` 阶段报符号缺失。

CI：`.github/workflows/gradle-publish.yml` 会构建模组，并在发布 Release 时把
`build/libs/*.jar` 作为附件上传。

## 项目结构

```
src/main/java/net/fodoth/skina/goldentweaks/
├── GoldenTweaks.java     # 模组入口，按已装模组做注册
├── compat/               # 各模组联动：物品、方块、配方、事件、ASM 辅助
│   ├── ae2autopatternupload/  alshanex_familiars/  astralsorcery/  cataclysm/  compactmachines/
│   ├── create/  ftbquests/  kaleidoscope/  lootr/  mekanism/  neoecoae/  oei/
│   ├── questshop/  renderblender/  snack_cabinet/  thaumcraft/  thaumicbases/  thermal/  ...
├── config/               # 配置规范与 Cloth Config 界面
├── debug/                # GUI 调试器
├── event/                # NeoForge 事件处理
├── mixin/
│   ├── GoldenTweaksMixinPlugin.java   # 逐 mixin 启用判断 + ASM 改写
│   ├── balance/          # 数值/机制平衡
│   ├── feature/          # 新增游戏行为
│   ├── fix/              # Bug 与崩溃修复（每个模组一个包）
│   ├── optimization/     # 通用优化
│   └── shut/             # 日志抑制
├── network/              # 网络包与客户端处理器（拾取设定同步）
└── util/                 # 工具类（emi、tmrv、debug 等）

src/main/resources/
├── goldentweaks.mixins.json
├── assets/               # 自身资源 + 其它模组的 zh_cn 语言文件
└── data/                 # 自身数据 + 兼容数据（要素、材料、配方、物品统一）

script/                   # Python 生成脚本与数据流水线（不打进 jar）
```

## 数据与脚本

`script/` 里是维护数据包用的离线生成脚本：读取模组 jar 与资源清单，把 JSON 写进
`src/main/resources/data/`。

| 脚本 | 用途 |
|---|---|
| `script/resource_locations/` | 从模组 jar 抽取物品清单，维护 `data/goldentweaks/recipe/thaumcraft/aspects/<模组>/<物品>.json` |
| `script/silentgear/` | 表驱动生成 Silent Gear 材料 |
| `script/thaumcraft/` | 要素清单维护与神秘时代研究/配方辅助 |
| `script/oei/` | 矿石、粉末、作物与食物的物品统一规则生成 |
| `script/bountiful/` | 悬赏池/悬赏令生成（计价、职业） |
| `script/materials/`、`script/translation/`、`script/analysis/` 等 | 配套工具与说明 |

会改变 id 的数据改动（例如 Silent Gear 材料改名）可能让已有存档里的成品失效，升级线上服务器前
请先看 [`update_log.md`](update_log.md)。

## 已知限制

- 仅支持 NeoForge 1.21.1，没有也不计划提供 Fabric 版本。
- 兼容补丁针对特定模组版本，模组更新后可能失效；每次发版的改动都记录在
  [`update_log.md`](update_log.md)。
- mixin 由「模组是否存在」控制开关，但受支持模组的差异过大版本仍可能报错，遇到时请附上模组列表与
  `latest.log`。
- 内置的 `zh_cn` 是第三方模组的社区质量翻译；若上游模组自带翻译，两者由资源包系统合并。

## 许可证

[PolyForm Noncommercial License 1.0.0](LICENSE) © Fodoth_jinzi89。

源码可用于非商业用途（使用、修改、再分发）；商业分发需获得作者许可。

仅用于编译的第三方模组 jar **不属于**本仓库，遵循各自许可证。

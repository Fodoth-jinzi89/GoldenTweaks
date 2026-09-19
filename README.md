# GoldenTweaks

[English](#english) | [中文](#中文)

适用于 Minecraft **1.21.1 / NeoForge** 的大型整合包兼容、优化与联动增强模组。

---

# English

A NeoForge compatibility, bug-fix, optimization and mod-integration patch mod for Minecraft 1.21.1,
built for large modpacks and long-running survival servers.

GoldenTweaks is a **patch layer**: it ships no gameplay overhaul of its own. It fixes crashes and
broken behaviour in individual mods, silences benign log spam, adds cross-mod content (recipes,
item aspects, materials) that would otherwise require a custom modpack data pack, and exposes
everything through a config file.

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248+ |
| Java | 21 |
| Version | 4.6 |
| Author | Fodoth_jinzi89 |
| License | PolyForm Noncommercial 1.0.0 |

## Contents

- [What it does](#what-it-does)
- [Installation](#installation)
- [Configuration](#configuration)
- [How it works](#how-it-works)
- [Building from source](#building-from-source)
- [Project layout](#project-layout)
- [Data & scripts](#data--scripts)
- [Known limitations](#known-limitations)
- [License](#license)

## What it does

### 1. Compatibility fixes

Per-mod mixin packages that repair crashes, dead features and version drift. `neoforge.mods.toml`
declares **126 optional mod dependencies**; the mixin config plugin (`GoldenTweaksMixinPlugin`)
decides per mixin whether it applies, and gates whole groups on the mods that are loaded, so a
patch is never applied against a mod that is not installed.

| Area | Examples of what is fixed |
|---|---|
| Tech & storage | AE2 (`MEStorageScreenSearch`, facades), AE2 Peat, AE2WTLib, AE2 auto pattern upload, ExtendedAE Plus, NeoECOAE, Mekanism (`APTPortEnergySafe`, multiblock data), Mekanism Extras, Evolved Mekanism chemixer, Mekanism Weaponry, Create (symmetry wand, sequencer, sound scapes, JEI subtypes), Create Diesel Generators, Create Enchantment Industry, Create Submarine, Aeronautics, Copycats, Ponder |
| Magic | Thaumcraft (aspect registry, crucible/infusion/arcane categories, research categories, ore features, overworld biome generation, thaumatorium, jars), Thaumic Tinkerer, Thaumic Bases, Thaumcraft Celestial, Irons Spellbooks, Spectrum, Apotheosis / Apotheosis Things / Apothic Enchanting, Forbidden Magic, Eidolon, Alshanex Familiars, Traveloptics summons |
| Rendering | Flywheel, Geckolib, ModernUI (emoji font & text render type), Veil, Sable, RenderBlender (cosmic render queue), Iris, Ponder, CMD/PackageCouriers |
| QoL & UI | EMI (hidden/duplicate stacks), JEI, TMRV (RecipeViewers scroll behaviour), Bountiful, Lootr, Quest Shop localization, FTB Quests, FTB Ultimine, Tipsmod, Waystones/Traveler's Titles, Xaero, Building Wands, Carry On |
| Others | Registrate, Placebo, Compact Machines, Jaopca, Eccentric Tome, Watut, CCTweaks, Annus, Tritium, RRLS, Silent Gear material book, NeoGuanNiao, Touhou Little Maid / Lost Maid / Spell, maid storage manager |

A handful of cases cannot be expressed as a mixin (wrong mixin shipped by another mod, language
file splitting, Carry On + Aeronautics interaction); those are handled with ASM bytecode edits
driven from the mixin config plugin.

### 2. Log spam suppression

**43 mixins** under `mixin/shut/` silence *benign* output that otherwise floods `latest.log` in
large packs: sound engine and sound manager warnings, model/sprite/blockstate loading noise,
shader pipeline errors, Realms availability checks when offline, recipe manager and tag loader
decode errors, and the periodic update notices / debug logs of a number of mods.

### 3. Quality of life

| Feature | Description |
|---|---|
| Right-click pickup | Pick up nearby dropped items with right-click, including experience orbs; continuous pickup, reach, target cap, through-walls and sneak behaviour are configurable |
| Always edible | All food can be eaten regardless of hunger; food-with-effect semantics are preserved |
| Lootr quick loot | Hold right-click to take loot from Lootr containers, with open animation, flying items and pickup grouping options |
| GUI inspector | Shift + middle-click dumps the hovered / held item (id, components, NBT) to the log, optionally to the clipboard |
| Semantic font wrap | Width-aware wrapping for CJK text in vanilla widgets and tooltips |
| Garbage station | A village structure block that periodically fills nearby trash cans with random daily loot (Kaleidoscope Cookery integration) |
| Lost maid drop | Touhou Lost Maid drops a configurable set of items on death (chance configurable) |
| Trash can | Kaleidoscope Cookery trash can tweaks |

### 4. Balance tweaks

All of these are config toggles with sane defaults:

- **Evolved Mekanism** solar panel output multiplier
- **Mekanism Extras** recycler factory stack upgrades from Absolute tier up
- **Cataclysm** balance adjustments
- **Advanced Infusion** item disabling
- **Irons Jewelry** haggler discount caps (count and percentage)
- **Thaumcraft** caching (object tags, crucible recipes, research tree), arcane crafting cache size,
  Thaumonomicon aspect-source paging, arcane workbench vanilla-recipe support, Earth Shock harm mode

### 5. Cross-mod content

GoldenTweaks ships a large data pack (`data/goldentweaks/`, ~19.7k JSON files) that bridges mods
without requiring KubeJS or a custom pack:

- **Thaumcraft** — 18 057 item aspects across 259 namespaces, 543 entity aspects,
  18 crucible recipes, 6 infusion recipes, arcane crafting recipes, 38 aspect definitions and
  9 custom researches (Brain Jar research, Thaumic Microscopy storage scanning, Shard Alchemy,
  Primordial Replication, Runed Tablet, Crimson Rites, Ultimate Ingot, Infusion Intercepter,
  Warp Theory Cleanser), plus a reworked Thaumonomicon "aspect knowledge" page with
  scanned-source lists and JEI/EMI aspect page limiting
- **Silent Gear** — 637 generated materials (560 of them cross-mod `compat/*` materials) so
  other mods' metals, gems and woods can be used as tool parts
- **Create** — mechanical crafting, sequenced assembly and extended crafting recipes
- **Extended Crafting / Spectrum / Alshanex Familiars / Touhou Little Maid Spell** —
  additional table recipes for cross-mod items
- **Compact Machines** — 144 room templates and controllable mob spawning inside rooms
- **Quest Shop** — five tiers of gold coins with configurable multipliers, shop category/entries
- **Bountiful** — bounty pools and decrees for 37 professions (alchemist, arcanist, AE2 engineer,
  mechanic, wizard, chef, …)
- **AE2 auto pattern upload** — provider selection screen, icon sync and network packets

### 6. Localization

Built-in Simplified Chinese (`zh_cn`) language files for **111 mod namespaces** (including guide
book page text for AE2, Croptopia, PackagedAuto and others), plus GoldenTweaks' own strings in
both `zh_cn` and `en_us` (1068 keys each).

## Installation

1. Install Java 21 and NeoForge for Minecraft 1.21.1.
2. Drop `goldentweaks-<version>.jar` into `.minecraft/mods/`.
3. Optional: install [Cloth Config](https://modrinth.com/mod/cloth-config) to get the in-game
   config screen. Without it the config file still works, it is just edited by hand.

The mod works with no other mods installed — every compatibility patch activates only when its
target mod is present.

## Configuration

Two NeoForge configs are registered (`config/goldentweaks-client.toml`,
`config/goldentweaks-common.toml`), grouped as:

| Group | Contents |
|---|---|
| `pickup` | Right-click pickup reach, target cap, delay threshold, through-walls / sneak / continuous pickup, experience orbs, Lootr quick-loot options |
| `feature` | Always-edible food, Lootr quick loot, Lost Maid death drops |
| `balance` | Evolved Mekanism solar multiplier, Mekanism Extras stacking, Cataclysm balance, Advanced Infusion items, Irons Jewelry haggler, Thaumcraft caches & aspect pages |
| `misc` (client) | Building Wands block preview, EMI search trigger threshold |
| `debug` (client) | GUI inspector and clipboard copy |

All values are readable without a config library: the code falls back to defaults while the config
is not loaded yet, so tweaks never crash on a missing/edited file.

## How it works

- **Mixins** (`goldentweaks.mixins.json`) — 285 mixins: 227 common + 58 client-only, split into
  `balance/`, `feature/`, `fix/`, `optimization/` and `shut/` packages that mirror the target mod's
  package layout.
- **Mixin config plugin** (`GoldenTweaksMixinPlugin`) — decides per mixin whether it applies, based
  on the loaded mod list (`FMLLoader`), and performs the few ASM transformations that mixins cannot
  express.
- **Bootstrap** (`GoldenTweaks`) — registers configs, items, block entities and event handlers for
  the mods that are actually present.
- **Data pack** — recipes, item/entity aspects, Silent Gear materials, bounty pools, tags, loot
  tables and structure additions.
- **Networking** — a small set of C2S/S2C packets for pickup requests, consumable sync, material
  book opening and pattern-provider icons.

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
│   ├── ae2autopatternupload/  alshanex_familiars/  cataclysm/  compactmachines/
│   ├── create/  exspectriments/  kaleidoscope/  lootr/  mekanism/  neoecoae/
│   ├── questshop/  renderblender/  snack_cabinet/  thaumcraft/  thaumicbases/  ...
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
├── network/              # packets + client handlers
└── util/                 # shared helpers (emi, tmrv, debug, ...)

src/main/resources/
├── goldentweaks.mixins.json
├── assets/               # own assets + zh_cn language files for other mods
└── data/                 # own data + compat data (aspects, materials, recipes, ...)

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

GoldenTweaks 是一层「补丁」：它本身不重做玩法，而是修复单个模组的崩溃与失效功能、抑制无害的
日志刷屏、补上跨模组联动内容（配方、物品要素、材料），并尽量把行为做成可配置项。专为大型整合包
与长期生存服务器设计。

| | |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.248+ |
| Java | 21 |
| 版本 | 4.6 |
| 作者 | Fodoth_jinzi89 |
| 许可证 | PolyForm Noncommercial 1.0.0 |

## 目录

- [功能概览](#功能概览)
- [安装](#安装)
- [配置](#配置)
- [实现方式](#实现方式)
- [从源码构建](#从源码构建)
- [项目结构](#项目结构)
- [数据与脚本](#数据与脚本)
- [已知限制](#已知限制)
- [许可证](#许可证)

## 功能概览

### 1. 兼容修复

每个模组一个 mixin 分包，修复崩溃、失效功能与版本漂移。`neoforge.mods.toml` 里声明了
**126 个可选依赖**，mixin 配置插件（`GoldenTweaksMixinPlugin`）按已加载的模组逐个决定 mixin
是否生效，并整组开关与模组绑定的兼容补丁，因此不会把补丁作用在未安装的模组上。

| 领域 | 修复内容举例 |
|---|---|
| 科技与存储 | AE2（终端搜索、伪装板）、AE2 Peat、AE2WTLib、AE2 自动样板上传、ExtendedAE Plus、NeoECOAE、Mekanism（能量端口安全、多方块数据）、Mekanism Extras、Evolved Mekanism 化学混合器、Mekanism Weaponry、Create（对称之杖、序列组装、音景、JEI 子类型）、Create 柴油发电机、Create 附魔工业、Create 潜艇、航空学、Copycats、Ponder |
| 魔法 | 神秘时代（要素注册、坩埚/注魔/奥术分类、研究分类、矿石特征、主世界群系生成、神秘炼药炉、罐子）、神秘工匠、神秘基础学、天象神秘学、Iron's Spellbooks、Spectrum、Apotheosis 系列、禁忌魔法、Eidolon、Alshanex Familiars、Traveloptics 召唤物 |
| 渲染 | Flywheel、Geckolib、ModernUI（emoji 字体与文本渲染类型）、Veil、Sable、RenderBlender（宇宙渲染队列）、Iris、Ponder、CMPackageCouriers |
| 体验与界面 | EMI（隐藏/重复堆叠）、JEI、TMRV（配方界面滚动）、Bountiful、Lootr、任务商店本地化、FTB Quests、FTB Ultimine、Tipsmod、旅行者标题/路标石、Xaero、建筑魔杖、Carry On |
| 其他 | Registrate、Placebo、Compact Machines、Jaopca、Eccentric Tome、Watut、CCTweaks、Annus、Tritium、RRLS、Silent Gear 材料书、NeoGuanNiao、东方小女仆 / Lost Maid / Spell、女仆存储管理 |

少数场景无法用 mixin 表达（别的模组自带写错的 mixin、语言文件拆分、Carry On 与航空学的交互），
这些在 mixin 配置插件里用 ASM 字节码改写处理。

### 2. 日志抑制

`mixin/shut/` 下 **43 个 mixin** 负责静默**无害**刷屏：音效引擎与音效管理器告警、模型/贴图/
方块状态加载噪音、着色器管线报错、离线时的 Realms 可用性检查、配方管理器与标签加载的解码错误，
以及一批模组的更新提示与调试日志。

### 3. 体验增强

| 功能 | 说明 |
|---|---|
| 右键拾取 | 右键拾取附近掉落物，包含经验球；连续拾取、拾取距离、目标数量上限、隔墙与潜行行为均可配置 |
| 始终可食用 | 无视饥饿度即可食用所有食物，并保留带效果食物的语义 |
| Lootr 快速拾取 | 按住右键直接拿取 Lootr 容器内战利品（开箱动画、飞行动画、拾取分组可配） |
| GUI 调试器 | Shift + 鼠标中键把悬浮/手持物品（id、数据组件、NBT）打印到日志，可复制到剪贴板 |
| 语义字体换行 | 原版界面与提示框里的 CJK 文本按宽度正确换行 |
| 垃圾站 | 与森罗万象·厨艺联动的村庄结构方块，每天向附近垃圾桶随机填充物品 |
| 女仆死亡掉落 | 东方 Lost Maid 死亡时掉落可配置的物品（概率可配） |
| 垃圾桶增强 | 森罗万象·厨艺垃圾桶行为调整 |

### 4. 平衡调整

全部为可配置开关，默认值保守：

- **Evolved Mekanism** 太阳能板发电倍率
- **Mekanism Extras** 回收工厂在绝对等级及以上支持堆叠升级
- **灾变（Cataclysm）** 数值平衡
- **Advanced Infusion** 物品禁用
- **Irons Jewelry** 讲价折扣上限（次数与百分比）
- **神秘时代** 缓存（对象标签、坩埚配方、研究树）、奥术合成缓存大小、魔导手册要素来源分页、
  奥术工作台支持原版配方、地裂伤害模式

### 5. 跨模组联动内容

GoldenTweaks 自带一份大型数据包（`data/goldentweaks/`，约 1.97 万个 JSON），无需 KubeJS 或
自定义整合包即可桥接模组：

- **神秘时代** —— 259 个命名空间共 **18057 条物品要素**、543 条实体要素、18 个坩埚配方、
  6 个注魔配方、奥术合成配方、38 个要素定义，以及 9 项自定义研究（缸中研究者、魔导显微扫描、
  碎片炼金、原始珍珠复制、符文石板、绯红仪式、终极锭注魔、注魔拦截器、扭曲理论净化）；
  另重做魔导手册「要素知识」页（要素来源列表、悬停显示已扫描物品、JEI/EMI 页数限制）
- **Silent Gear** —— 生成 637 个材料（其中 560 个是跨模组 `compat/*` 材料），让其它模组的
  金属、宝石、木材可以直接做工具部件
- **机械动力** —— 机械合成、序列组装、组合合成配方
- **Extended Crafting / Spectrum / Alshanex Familiars / 东方小女仆 Spell** —— 跨模组工作台配方
- **Compact Machines** —— 144 个房间模板，并可控制房间内生物生成
- **任务商店** —— 五档金币（倍率可配）与商店分类/条目
- **Bountiful** —— 37 个职业的悬赏池与悬赏令（炼金术士、秘术师、AE2 工程师、机械师、巫师、厨师等）
- **AE2 自动样板上传** —— 样板供应商选择界面、图标同步与网络包

### 6. 本地化

内置 **111 个模组命名空间**的简体中文语言文件（含 AE2、Croptopia、PackagedAuto 等的指南书
正文），GoldenTweaks 自身的中英文本各 1068 个键。

## 安装

1. 安装 Java 21 与 Minecraft 1.21.1 对应的 NeoForge。
2. 把 `goldentweaks-<版本>.jar` 放进 `.minecraft/mods/`。
3. 可选：安装 [Cloth Config](https://modrinth.com/mod/cloth-config) 以获得游戏内配置界面；
   不装也能用，只是需要手动编辑配置文件。

兼容补丁只在目标模组存在时生效，可以直接加入现有整合包。

## 配置

注册两份 NeoForge 配置（`config/goldentweaks-client.toml`、`config/goldentweaks-common.toml`）：

| 分组 | 内容 |
|---|---|
| `pickup` | 右键拾取距离、目标上限、延迟阈值、隔墙/潜行/连续拾取、经验球、Lootr 快速拾取选项 |
| `feature` | 始终可食用、Lootr 快速拾取、女仆死亡掉落 |
| `balance` | 太阳能倍率、Mekanism Extras 堆叠、灾变平衡、Advanced Infusion 物品、Irons Jewelry 讲价、神秘时代缓存与要素分页 |
| `misc`（客户端） | 建筑魔杖方块预览、EMI 搜索触发阈值 |
| `debug`（客户端） | GUI 调试器与剪贴板复制 |

所有取值在配置尚未加载时会回落到默认值，因此改动配置文件不会导致启动崩溃。

## 实现方式

- **Mixin**（`goldentweaks.mixins.json`）—— 共 285 个：227 个通用 + 58 个仅客户端，按
  `balance/`、`feature/`、`fix/`、`optimization/`、`shut/` 分包，并镜像目标模组的包结构。
- **Mixin 配置插件**（`GoldenTweaksMixinPlugin`）—— 依据已加载模组列表（`FMLLoader`）逐个决定
  mixin 是否生效，并承担少数无法用 mixin 表达的 ASM 改写。
- **入口**（`GoldenTweaks`）—— 注册配置、物品、方块实体与事件处理器，全部以「模组存在」为前提。
- **数据包** —— 配方、物品/实体要素、Silent Gear 材料、悬赏池、标签、战利品表与结构追加。
- **网络** —— 少量 C2S/S2C 数据包：拾取请求、消耗品同步、材料书打开、样板供应商图标。

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
│   ├── ae2autopatternupload/  alshanex_familiars/  cataclysm/  compactmachines/
│   ├── create/  exspectriments/  kaleidoscope/  lootr/  mekanism/  neoecoae/
│   ├── questshop/  renderblender/  snack_cabinet/  thaumcraft/  thaumicbases/  ...
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
├── network/              # 网络包与客户端处理器
└── util/                 # 工具类（emi、tmrv、debug 等）

src/main/resources/
├── goldentweaks.mixins.json
├── assets/               # 自身资源 + 其它模组的 zh_cn 语言文件
└── data/                 # 自身数据 + 兼容数据（要素、材料、配方等）

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

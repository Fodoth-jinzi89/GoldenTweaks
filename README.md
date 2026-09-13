# GoldenTweaks

[English](#english) | [中文](#中文)

---

# English

A NeoForge optimization, compatibility fix, and mod integration tweak mod for Minecraft 1.21.1.

GoldenTweaks covers:
- Compatibility patches for 50+ problematic mods
- Log spam suppression for 30+ noisy mods
- Gameplay quality-of-life features
- Cross-mod integration recipes, item aspects, and translations

It is designed for large modpacks and long-term survival servers.

---

## Features

### Compatibility Fixes — 50+ mods

| Category | Mods Fixed |
|---|---|
| **Tech & Storage** | AE2, AE2Peat, AE2WTLib, Mekanism, Evolved Mekanism, Create, Create Diesel Generators, Create Enchantment Industry, Create Submarine, Fluid Logistics, Mekanism Weaponry, Advanced Loot Info |
| **Magic** | Thaumcraft, Thaumic Tinkerer, Irons Spellbooks, Spectrum, Forbidden Magic, Apotheosis, Apotheosis Things, Apothic Enchanting, Eidolon Repraised, Alshanex Familiars |
| **Rendering** | Flywheel, Geckolib, ModernUI, Veil, Sable, CMPackageCouriers, Copycats |
| **QoL & UI** | EMI, JEI, Tipsmod, Quest Shop, Cloth Config, Config Tracker, Xaero's Minimap, Traveler's Titles, Bountiful, Lootr |
| **Others** | Annus, RRLS, Tritium, CCB Tweaks, Silent Gear, NeoGuanNiao, Touhou Little Maid, Touhou Lost Maid, Maid Beacon, JAOPCA, Exspectriments, Aeronautics, Eccentric Tome, NoApothesisNames, Too Many Recipe Viewers, Pattern Schematics, LCMOS, Modonomicon, Placebo, Registrate |

### Log Suppression — 30+ mods

GoldenTweaks silences benign but noisy log output from:
- Continuity, CIT Resewn, Custom Uniforms, Vistas, Xaero
- Various sound engine and model loading warnings
- Realms connectivity warnings (offline environments)
- Recipe manager and tag loader errors
- And more — see `mixin/shut/` for the full list

### Gameplay & QoL

| Feature | Description |
|---|---|
| **Right-click Pickup** | Pick up distant items by right-clicking; excess items fly toward player when inventory is full |
| **Always Edible** | All food items can be eaten regardless of hunger level; configurable per-item via tags |
| **GUI Debugger** | Shift + middle-click to dump hovered/handheld item info to logs; configurable |
| **Garbage Station** | Integrates with Kaleidoscope Cookery — village structure that fills nearby trash cans with randomized daily loot |

### Cross-mod Integration

GoldenTweaks bridges mods with custom content:
- **Thaumcraft**: JSON-driven arcane, crucible, and infusion recipes; item aspect registration; JEI aspect source page limiting
- **Alshanex Familiars**: Custom attribute system, inverted familiar spellbook, attribute sharing mechanics
- **Create**: Symmetry wand compatibility, custom sequenced assembly items
- **Kaleidoscope Cookery**: Garbage station floor block & village generation
- **Quest Shop**: Gold coin items with configurable multipliers
- **Cataclysm**: Additional compat content
- **Touhou Little Maid**: Integration enhancements

### Balance Adjustments

- Evolved Mekanism solar panel values (configurable, defaults to ~x2700 baseline)
- Mekanism Extras: Recycling Factory stacking upgrade support at Absolute tier and above
- All balance changes can be toggled in config

### Localization

- Built-in Chinese (zh_cn) translations for 50+ mods
- Guide book localizations (AE2, Croptopia, PackagedAuto, etc.)

---

## Environment

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge |
| Java | 21 |

---

## Installation

1. Install Java 21
2. Install NeoForge for Minecraft 1.21.1
3. Place `GoldenTweaks.jar` in your `mods` folder
4. (Optional) Install [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) for an in-game configuration screen

---

## Development

### Clone

```bash
git clone https://github.com/Fodoth-jinzi89/GoldenTweaks.git
```

### Generate IDE Runs

```bash
./gradlew genIntellijRuns
```

### Build

```bash
./gradlew build
```

Built jars are in `build/libs/`.

### Run

```bash
./gradlew runClient    # Launch test client
./gradlew runServer    # Launch test server
```

---

## Configuration

GoldenTweaks provides a config screen (requires Cloth Config) with categories:

| Category | Content |
|---|---|
| **调试 (Debug)** | GUI debugger, item info logging |
| **机制 (Mechanics)** | Always-edible food, right-click pickup settings |
| **平衡 (Balance)** | Solar panel multiplier, Mekanism Extras stacking |
| **兼容 (Compatibility)** | Thaumcraft JEI aspect page limit, misc compat toggles |

---

## Project Structure

```
src/main/java/net/fodoth/skina/goldentweaks/
├── compat/       # Per-mod compat: items, recipes, blocks, events
├── config/       # NeoForge config spec & screen
├── debug/        # GUI inspector & debug utilities
├── event/        # NeoForge event handlers
├── mixin/
│   ├── balance/  # Numerical/mechanical balance tweaks
│   ├── feature/  # New gameplay features
│   ├── fix/      # Bug & crash fixes (per-mod packages)
│   ├── shut/     # Log/spam suppression
│   ├── optimization/ # General optimization mixins
│   └── renderblender/ # Rendering layer patches
├── network/      # C2S / S2C custom packets
└── util/         # Shared helpers
```

---

## License

GPL-3.0-only License

---

# 中文

适用于 Minecraft 1.21.1 的 NeoForge 优化、兼容修复与模组联动增强模组。

GoldenTweaks 涵盖：
- 50+ 模组的兼容性修复
- 30+ 模组的日志刷屏抑制
- 游戏体验增强（QoL）
- 跨模组联动配方、物品要素与汉化

专为大型整合包与长期生存服务器设计。

---

## 功能

### 兼容性修复 — 50+ 模组

| 分类 | 已修复模组 |
|---|---|
| **科技 & 存储** | AE2、AE2Peat、AE2WTLib、Mekanism、Evolved Mekanism、Create、Create Diesel Generators、Create Enchantment Industry、Create Submarine、Fluid Logistics、Mekanism Weaponry、Advanced Loot Info |
| **魔法** | Thaumcraft、Thaumic Tinkerer、Irons Spellbooks、Spectrum、Forbidden Magic、Apotheosis、Apotheosis Things、Apothic Enchanting、Eidolon Repraised、Alshanex Familiars |
| **渲染** | Flywheel、Geckolib、ModernUI、Veil、Sable、CMPackageCouriers、Copycats |
| **QoL & UI** | EMI、JEI、Tipsmod、Quest Shop、Cloth Config、Config Tracker、Xaero's Minimap、Traveler's Titles、Bountiful、Lootr |
| **其他** | Annus、RRLS、Tritium、CCB Tweaks、Silent Gear、NeoGuanNiao、Touhou Little Maid、Touhou Lost Maid、Maid Beacon、JAOPCA、Exspectriments、Aeronautics、Eccentric Tome、NoApothesisNames、Too Many Recipe Viewers、Pattern Schematics、LCMOS、Modonomicon、Placebo、Registrate |

### 日志抑制 — 30+ 模组

GoldenTweaks 静默处理以下模组的无害刷屏日志：
- Continuity、CIT Resewn、Custom Uniforms、Vistas、Xaero's Minimap
- 各类音效引擎与模型加载告警
- Realms 连接告警（离线环境）
- 配方管理器和标签加载错误
- 更多 — 参见 `mixin/shut/`

### 游戏增强

| 功能 | 说明 |
|---|---|
| **右键拾取** | 右键远处物品即可拾取；背包满时多余物品飞向玩家 |
| **始终可食用** | 无视饥饿度即可食用所有食物；可通过标签按物品配置 |
| **GUI 调试器** | Shift + 鼠标中键将鼠标悬浮/手持物品信息打印到日志 |
| **垃圾站** | 与 Kaleidoscope Cookery 联动 — 村庄建筑，每天向垃圾桶随机填充物品 |

### 跨模组联动

GoldenTweaks 通过自定义内容桥接模组：
- **Thaumcraft（神秘时代）**：JSON 驱动的奥术/坩埚/注魔合成配方；物品要素注册；JEI 要素来源页数限制
- **Alshanex Familiars**：自定义属性系统、反转魔宠法术书、属性共享机制
- **Create（机械动力）**：对称之杖兼容、自定义序列组装物品
- **Kaleidoscope Cookery（森罗万象·厨艺）**：垃圾站地板方块与村庄生成
- **Quest Shop（任务商店）**：金币物品与可配置倍率
- **Cataclysm（灾变）**：辅助联动内容
- **Touhou Little Maid（东方小女仆）**：交互增强

### 平衡性调整

- Evolved Mekanism 太阳能板数值（可配置，默认约 x2700 倍基准）
- Mekanism Extras：回收工厂在绝对等级及以上支持堆叠升级
- 所有平衡更改均可在配置中切换

### 本地化

- 内置 50+ 模组的简体中文（zh_cn）翻译
- 指南书本地化（AE2、Croptopia、PackagedAuto 等）

---

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge |
| Java | 21 |

---

## 安装

1. 安装 Java 21
2. 安装 Minecraft 1.21.1 对应的 NeoForge
3. 将 `GoldenTweaks.jar` 放入 `mods` 文件夹
4. （可选）安装 [Cloth Config](https://www.curseforge.com/minecraft/mc-mods/cloth-config) 以使用游戏内配置界面

---

## 开发

### 克隆仓库

```bash
git clone https://github.com/Fodoth-jinzi89/GoldenTweaks.git
```

### 生成 IDE 运行配置

```bash
./gradlew genIntellijRuns
```

### 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/`。

### 运行

```bash
./gradlew runClient    # 启动测试客户端
./gradlew runServer    # 启动测试服务端
```

---

## 配置

GoldenTweaks 提供游戏内配置界面（需要 Cloth Config），含以下分类：

| 分类 | 内容 |
|---|---|
| **调试** | GUI 调试器、物品信息日志 |
| **机制** | 始终可食用、右键拾取详细设置 |
| **平衡** | 太阳能倍率、Mekanism Extras 堆叠升级 |
| **兼容** | Thaumcraft JEI 要素页数限制、杂项兼容开关 |

---

## 项目结构

```
src/main/java/net/fodoth/skina/goldentweaks/
├── compat/       # 各模组联动：物品、配方、方块、事件
├── config/       # NeoForge 配置规范与界面
├── debug/        # GUI 调试器
├── event/        # NeoForge 事件处理
├── mixin/
│   ├── balance/  # 数值/机制平衡调整
│   ├── feature/  # 新增游戏功能
│   ├── fix/      # Bug 与崩溃修复（按模组分包）
│   ├── shut/     # 日志/刷屏抑制
│   ├── optimization/ # 通用优化 Mixin
│   └── renderblender/ # 渲染层补丁
├── network/      # 自定义网络包（C2S / S2C）
└── util/         # 工具类
```

---

## 许可证

GPL-3.0-only License

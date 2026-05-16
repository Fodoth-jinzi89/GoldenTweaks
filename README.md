# GoldenTweaks

[English](#english) | [中文](#中文)

---

# English

A NeoForge optimization and gameplay tweak mod for Minecraft 1.21.1.

GoldenTweaks focuses on improving:
- Rendering performance
- GPU-side optimizations
- Vanilla interaction fixes
- Small gameplay quality-of-life features
- Compatibility patches for problematic mods

This mod is designed as a practical collection of fixes, experimental optimizations, and gameplay improvements for large modpacks and long-term survival gameplay.

---

## Features

### Rendering & Performance
- OpenGL optimization utilities
- DSA (Direct State Access) support
- Fast math optimizations
- Reduced rendering overhead
- SIMD utility implementations
- GPU-side rendering experiments

**Note:** Rendering optimizations is off when using `Vulkan`, `SuperResolution`, `Veil` for better compatibility.

### Gameplay Tweaks
- Right-click to collect items
- Interaction fixes
- QoL utility mechanics

### Compatibility Fixes
- Modded rendering patches
- Broken mixin compatibility fixes
- Logger suppression
- Third-party mod behavior adjustments

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
3. Put `GoldenTweaks.jar` into your `mods` folder

---

## Development

### Clone Repository

```bash
git clone https://github.com/yourname/GoldenTweaks.git
```

### Generate IDE Runs

```bash
./gradlew genIntellijRuns
```

### Build

```bash
./gradlew build
```

Built jars will be located in:

```text
build/libs/
```

---

## GitHub Actions

This repository includes automated GitHub Actions workflows for:
- Automatic CI builds
- Release artifact upload
- Gradle dependency caching
- GitHub Release publishing

Click [Actions](https://github.com/Fodoth-jinzi89/GoldenTweaks/actions/workflows/gradle-publish.yml) to start an automatic online build.

Workflow files are located in:

```text
.github/workflows/
```

---

## Project Goals

GoldenTweaks is not intended to be a universal optimization mod.

The project focuses on:
- GPU-oriented rendering experiments
- Practical fixes ignored by larger projects
- Technical cleanup for heavily modded environments
- Low-level minecraft behavior research

Some features may be highly experimental.

---

## License

MIT License

Unless otherwise specified.

---

# 中文

一个适用于 Minecraft 1.21.1 的 NeoForge 优化与玩法增强模组。

GoldenTweaks 专注于：
- 渲染性能优化
- GPU 侧优化
- 原版交互修复
- 小型 QoL（生活质量）增强
- 问题模组兼容性补丁

本模组旨在为大型整合包与长期生存环境提供一组实用修复、实验性优化与底层改进。

---

## 功能

### 渲染与性能
- OpenGL 优化工具
- DSA（Direct State Access）支持
- Fast Math 快速数学优化
- 降低渲染开销
- SIMD 工具实现
- GPU 侧渲染实验

**提示：** 使用 `Vulkan`、`SuperResolution`、`Veil` 时，为保证兼容性，会关闭渲染优化。

### 游戏玩法增强
- 右键收集物品
- 交互行为修复
- 实用 QoL 机制

### 兼容性修复
- 模组渲染补丁
- Mixin 兼容性修复
- Logger 日志抑制
- 第三方模组行为调整

---

## 环境要求

| 组件 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge |
| Java | 21 |

---

## 安装方式

1. 安装 Java 21
2. 安装 Minecraft 1.21.1 对应 NeoForge
3. 将 `GoldenTweaks.jar` 放入 `mods` 文件夹

---

## 开发

### 克隆仓库

```bash
git clone https://github.com/yourname/GoldenTweaks.git
```

### 生成 IDE 运行配置

```bash
./gradlew genIntellijRuns
```

### 构建

```bash
./gradlew build
```

构建产物位于：

```text
build/libs/
```

---

## GitHub Actions

仓库内已包含 GitHub Actions 自动化工作流，用于：
- 自动 CI 构建
- 自动上传 Release 构建产物
- Gradle 依赖缓存
- GitHub Release 自动发布

用户可使用自动化工作流在线构建可用版本，点击上方 [Actions](https://github.com/Fodoth-jinzi89/GoldenTweaks/actions/workflows/gradle-publish.yml) 即可。

工作流文件位于：

```text
.github/workflows/
```

---

## 项目目标

GoldenTweaks 并不试图成为一个“万能优化模组”。

该项目更关注：
- GPU 导向渲染实验
- 被大型项目忽略的实际问题修复
- 多模组环境下的底层清理
- Minecraft 底层行为研究

部分功能可能具有较强实验性。

---

## 许可证

MIT License

除非另有说明。

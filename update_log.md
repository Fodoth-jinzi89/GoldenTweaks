# GoldenTweaks Update Log

## 2026.06.10 — v2.2

### Alshanex Familiars
- 添加了一本新的法术书，可以共享魔宠属性给主人
- 添加联动配方

### Apothsiscreate
- 添加了正确的序列组装中间物

### Golden Tweaks
- 添加联动配方

## 2026.06.04 — v2.1

### Evolved Mekanism
- 重新调整了太阳能平衡

### Quest Shop
- 尝试修复重启后任务类型丢失问题

### Aeronautics
- 修复 JEI 插件加载过早的问题

### Create
- 修复线程不安全问题

### Create Enchantment Industry
- 修复带储罐的思索崩溃问题

### Fluid Logistics
- 修复无限流体储罐未正确响应配置的问题

### Geckolib
- 修复找不到光照文件引起字体渲染错误的问题

### Golden Tweaks
- 添加汉化和联动配方

## 2026.05.30 — v2.0

### Spectrum
- 添加联动配方

### Travelers Titles
- 修复传送石碑标题错误发给所有玩家的问题
  - [NeoForgeWaystonesCompatHelperMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/travelerstitles/NeoForgeWaystonesCompatHelperMixin.java)

### Sable
- 修复缓存崩溃
  - [VoxelNeighborhoodStateMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/sable/VoxelNeighborhoodStateMixin.java)

### JAOPCA
- 禁止下载语言文件
  - [LocalizationRepoHandlerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/jaopca/LocalizationRepoHandlerMixin.java)

### AE2
- 修复固定项目的表达式写反的问题
  - [PinnedKeysMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2/PinnedKeysMixin.java)

## 2026.05.29 — v1.9

### Apotheosis Things
- 继续修复分解护符

### Cmpackagecouriers
- 修复渲染崩溃
  - [CardboardPlaneItemRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/cmpackagecouriers/CardboardPlaneItemRendererMixin.java)


## 2026.05.28 — v1.8

### Create Submarine
- 添加汉化

### Mekmm
- 添加联动配方

### T.O Magic n' Extras
- 禁用进入世界时提示
  - [IncompatibilityCheckerEventMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/IncompatibilityCheckerEventMixin.java)

### Touhou Little Maid: Spell
### Ramization
- 禁用导致崩溃的 Mixin
  - [GoldenTweaksMixinPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/GoldenTweaksMixinPlugin.java)

### Too Many Recipe Viewers
- 禁用刷屏日志
  - [TooManyRecipeViewersModMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/TooManyRecipeViewersModMixin.java)

### CCB Tweaks
- 修复 Config 过早加载问题
  - [ReleaseMouseMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ccbtweaks/ReleaseMouseMixin.java)


## 2026.05.25 — v1.7

### Spectrum
- 添加联动配方

### Create
- 修复对称之杖和 Sable 的兼容性问题
  - [SymmetryHandlerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/SymmetryHandlerMixin.java)
## 2026.05.25 — v1.6

### Alshanex Familiars
- 继续修复 GUI

### Evolved Mekanism
- 添加合金炉联动配方

### Croptopia
- 添加汉化及 Mekmm 种植机配方

### Create Diesel Generators
- 修复初始化问题
  - [CDGSpriteShiftsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/cdg/CDGSpriteShiftsMixin.java)

## 2026.05.24 — v1.5

### Alshanex Familiars
- 完善联动物品，写好合成表
- 修好 GUI

## 2026.05.23 — v1.4

### Alshanex Familiars
- 增加联动物品，重新搓了一整套属性系统，解放属性上限
  - [相关文件](src/main/java/net/fodoth/skina/goldentweaks/compat/alshanex_familiars)
  - [相关mixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/alshanex_familiars)
- GUI 还没修好

### Attribute Fix
- 设置 Config 默认值的上下限为近乎无限（DOUBLE.MAX_VALUE）
  - [RangeConfigMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/attributefix/RangeConfigMixin.java)

## 2026.05.21 — v1.3

### Questshop
- 修复了硬编码无法本地化的问题
  - 添加本地化

## 2026.05.20 — v1.2

### Golden Tweaks
- 更改了永久可食用的实现方式
  - 现在能够正确响应配置变化
- GUI 调试器新增物品调试功能
  - Shift + 鼠标中键可在日志打印鼠标悬浮/主手物品信息
- 增加了一众模组的汉化

### Maid Beacon
- 尝试修复在 Immersive Optimizations 等异步模组存在时，退出存档删除持久实体会使游戏崩溃的问题
  - [MaidTrackerMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maid/MaidTrackerMixin.java)

### Too Many Recipe Viewers
- 压制了 Iron's Jewelery 的报错
  - [StackHelperMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/StackHelperMixin.java)

### Additional Attributes
- 修复了卷轴放进 AE 会崩溃的问题
  - [SpellUtilsMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/irons_spellbooks/SpellUtilsMixin.java)

### Evolved Mekanism
- 修复了化学混合机配方问题
  - [ChemixerRecipeMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/evolvedmekanism/ChemixerRecipeMixin.java)
  - [BasicChemixerRecipeMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/evolvedmekanism/BasicChemixerRecipeMixin.java)
  - [EMJEIMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/evolvedmekanism/EMJEIMixin.java)
- 修复了 APT 在客户端未完成加载时就读取内部存储的问题
  - [APTPortEnergySafeMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mek/APTPortEnergySafeMixin.java)
  - [MultiblockDataMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mek/MultiblockDataMixin.java)
  
### Mekanism More Machines
- 添加联动配方

### DnDecor
- 添加汉化

### DnDesires
- 添加汉化

## 2026.05.19 — v1.1

### Evolved Mekanism
- 添加联动配方
- 添加汉化

### Create Nuclear
- 添加联动配方
- 添加汉化


## 2026.05.18 — v1.0

### Golden Tweaks
- 现在背包已满时右键拾取能够使得多余的物品飞向玩家
  - [C2SPickupItemPacket](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPickupItemPacket.java)
  - 可以用这个功能，长按右键，在背包已满的状况下提着物品移动
- 删除了多余的类
  
### Silent Gear
- 修复了材料书打开会使每个玩家打开界面的问题
  - [MaterialBookItemMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/silentgear/MaterialBookItemMixin.java)
  - [S2COpenMaterialBookPacket](src/main/java/net/fodoth/skina/goldentweaks/network/packet/S2COpenMaterialBookPacket.java)

## 2026.05.17 — v0.9

### Golden Tweaks
- 增加了一个 GUI 调试器
  - [GuiInspector](src/main/java/net/fodoth/skina/goldentweaks/debug/GuiInspector.java)
  - 默认关闭，可在 `调试` 分类中开启
  - Shift + 鼠标中键 在 log 中输出调试信息

### Cloth Config
- 现在使用反射调用，该模组从必要前置变为可选前置

### Mekanism Extras
- 现在来自 `Mekanism More Machines` 的回收工厂，在绝对等级及以上支持堆叠升级
  - [ExtraMoreMachineBlockTypesMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/balance/ExtraMoreMachineBlockTypesMixin.java)
  - 默认启用
  - 可在 `平衡` 分类中关闭

### Gameplay
- 增加“所有食物始终可食用”功能
  - [FoodPropertiesMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/PlayerMixin.java)
  - 默认启用
  - 可在 `机制` 分类中关闭

### EMI Compatibility
- 屏蔽 `JECharacters / PinIn` 导致的 EMI 异步搜索日志刷屏
  - [EmiLogMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/EmiLogMixin.java)
  - 不再输出无害的拼音搜索异常
  - 不影响其它 EMI 错误日志

## 2026.05.16 — v0.8

### Minecraft
- 新增“右键拾取远处物品”功能（可在配置中进行详细调节）
- 新增“所有食物在满饱食度下食用”的机制

### NeoForge
- 为未注册 Attributes 的实体添加兼容性兜底

---

### Apothesis Things
- 修复 **Salvage Charm** 无法通过铁砧进行定级的问题
    - 现在：主手持有饰品，副手持有材料，右键即可完成定级
- 目前该物品还有数据包重载时数据丢失问题，会造成玩家背包数据非法，无法进入存档，待修复

---

### GPUBooster
- 内置增强版 `GPUBooster` 修复分支
- 修复与以下模组的兼容性问题：
    - `SuperResolution`
    - `Veil`
    - `ModernUI`

---

### EvolvedMekanism
- 调整太阳能发电数值平衡
    - 默认对齐 Mekanism Advanced Generators（约 x2700 倍基准）

---

### AdvancedLootInfo
- 修复与 `Supplementaries` 的兼容性问题

---

### Annuus
- 修复原版网络类获取错误的问题

---

### NoApothesisNames
- 修复 `Apotheosis` 无法获取生物名称导致的崩溃问题

---

### Flywheel
- 修复 `BakedModelBufferer` 在空模型输入时触发 NPE

---

### Create
- 尝试修复 `JEI` 注册异常问题

---

### TouhouLittleMaid
- 修复部分 LLM API 返回标识符错误进入女仆对话气泡的问题

---

### MekanismWeaponry
- 修复无法正确获取玩家皮肤的问题

---

### Create Enchantment Industry
- 尝试修复经验舱口思索场景的潜在崩溃问题

---

### ModernUI
- 尝试修复文本更新导致 `FixedBuffers` 异常的问题

---

### RRLS
- 修复在 Config 尚未加载完成时访问条目的问题

---

### Tritium
- 修复在 Config 尚未加载完成时访问条目的问题

---

### Veil
- 修复 `VanillaShaderProcessor` 中 GLSL 编译错误

---

### Eccentric Tome
- 移除启动时连接 GitHub 获取物品列表的行为
    - 改为使用本地数据源

---

### Other
- 禁用多个模组的日志刷屏（Log Spam）问题
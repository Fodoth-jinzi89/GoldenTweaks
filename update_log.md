# GoldenTweaks Update Log

## 2026.08.11 - v3.2

### Thaumcraft
- 新增模组联动内容，支持通过 JSON 数据包注册神秘时代的奥术合成、坩埚和注魔合成配方
- 新增物品要素注册系统，支持通过 JSON 数据包为物品/标签添加要素
- 添加坩埚合成配方：六种源质碎片（风、地、水、火、秩序、混沌）及平衡碎片
  - [配方文件](src/main/resources/data/goldentweaks/recipe/thaumcraft/crucible/)
- 添加坩埚合成配方：七宗罪碎片（Forbidden Magic）：嫉妒、暴食、贪婪、色欲、骄傲、懒惰、暴怒，以及腐化碎片
  - [配方文件](src/main/resources/data/goldentweaks/recipe/thaumcraft/crucible/)
- 添加坩埚复制配方：琥珀、水银
- 添加坩埚合成配方：纯净水桶
- 添加奥术合成配方：炼金炉
- 添加注魔合成配方：元始珍珠（来自 Tainted Magic）
  - [primordial_pearl.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/primordial_pearl.json)
- 添加物品要素：世界盐
- 添加神秘时代、神秘工匠、禁忌魔法的完整汉化
- 修复神秘工匠 JEI 客户端初始化问题
  - [TTUtilityItemClientSetupMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/TTUtilityItemClientSetupMixin.java)
- 限制 JEI 要素来源显示页数，可在配置中调节
  - [ThaumcraftJeiPluginMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftJeiPluginMixin.java)
- 添加朱砂转化配方（from/to）
- 添加黑曜石图腾合成配方

### Mekanism More Machines
- 添加神秘时代植物种植配方：火焰草、宏伟之树树苗、纤毛菇、水银花、银树树苗
- 添加禁忌魔法树苗种植配方：腐化树苗、扭曲树苗
- 添加烟火凡人间野生芦苇叶种植配方

### Hostile Networks
- 添加基础敌对生物数据模型掉落：僵尸、末影人、僵尸猪灵

### Silent Gear
- 添加下界之星碎片合成配方

### Apothic Enchanting
- 扩展附魔等级汉化覆盖范围

### Ore Excavation Integration
- 添加矿物统一化补充配置

### GoldenTweaks
- 清理无用 import 及注解，提升代码整洁度

### 烟火凡人心
- 更新适配版本

### Sable
- 修复了烟火凡人心、Create、NeoGuanNiao中的方块实体物理化刷物品问题

## 2026.06.28 - v2.9

### 烟火凡人心
- 更改绞肉机的行为

### 合成扩展
- 更改终极锭的贴图

### Traveloptics
- 增加联动配方

## 2026.06.24 - v2.8

### 烟火凡人心
- 更新版本，修复各种机器

### Bountiful
- 增加联动内容

## 2026.06.23 - v2.7

### Kaleidoscope Cookery
- 修复配方标签写错的问题

### 烟火凡人心
- 添加联动配方

### GoldenTweaks
- 增加了森罗联动内容：垃圾站地板
- 垃圾站地板在村庄中时，每天早上会随机往上面的垃圾桶塞垃圾，不同村庄的垃圾列表不同
- 现在村庄中会生成垃圾站，包含垃圾站地板和垃圾桶

## 2026.06.22

### 女仆
- 添加了烟火凡人心菜品，以及无尽蛋糕的支持

## 2026.06.21 - v2.6

### 烟火凡人心
- 修改部分厨具逻辑，添加了自动化支持
- 添加联动配方
- 现在食物统一会优先使用该模组的食物

### All The Compatibility
- 暂时移除了登入消息

## 2026.06.19 - v2.5

### Alshanex Familiars
- 修复了潘多拉魔盒界面
- 暂时移除了所有灾厄召唤物

### Advanced Loot Info
- 增加汉化

### Touhou Lost Maid
- 现在迷失女仆的掉率是可以配置的
- 修复了被玩家驯服的实体击杀迷失女仆不掉落的问题
- 现在迷失女仆被击杀不会在聊天栏报信息

## 2026.06.17 — v2.4

### Exspectriments
- 使之可以在 Connector 环境下启动
- 修复了与 Spectrum 的兼容性

### AeBetterVillagers
- 增加联动交易

### Ae2peat
- 现在样板编码访问终端可以上传子网络，也可以拼音搜索
- 修复了 EMI 转移配方时的崩溃问题

### Create
- 修复了偶发的进度提前获取物品图标导致崩溃问题

### Create Submarine
- 移除了欢迎界面

### Spectrum
- 修复了合成模板刷物品问题

### Tipsmod
- 增加了tips

### GoldenTweaks
- 增加联动配方

## 2026.06.13 — v2.3

### Alshanex Familiars
- 修复强化丢失问题
- 尝试修复召唤物攻击魔宠问题
- 添加联动法术

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
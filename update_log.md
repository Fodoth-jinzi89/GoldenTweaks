# GoldenTweaks Update Log

## 2026.05.18 — v1.0

### Golden Tweaks
- 现在背包已满时右键拾取能够使得多余的物品飞向玩家
  - [C2SPickupItemPacket](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPickupItemPacket.java)
  - 可以用这个功能，长按右键，在背包已满的状况下提着物品移动
- 删除了多余的类
  
### Silent Gear
- 修复了材料书打开会使每个玩家打开界面的问题
  - [MaterialBookItemMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/MaterialBookItemMixin.java)
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
  - [FoodPropertiesMixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/FoodPropertiesMixin.java)
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
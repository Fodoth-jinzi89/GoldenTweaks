# GoldenTweaks Update Log

## 2026.05.16 — v0.8

### Minecraft
- 新增“右键拾取远处物品”功能（可在配置中进行详细调节）
- 新增“所有食物在满饱食度下食用”的机制

### NeoForge
- 为未注册 `Attributes` 的实体添加兼容性兜底

---

### Apothesis Things
- 修复 **Salvage Charm** 无法通过铁砧进行定级的问题
    - 现在：主手持有饰品，副手持有材料，右键即可完成定级

---

### GPUBooster
- 内置增强版 GPUBooster 修复分支
- 修复与以下模组的兼容性问题：
    - SuperResolution
    - Veil
    - ModernUI

---

### EvolvedMekanism
- 调整太阳能发电数值平衡
    - 默认对齐 Mekanism Advanced Generators（约 x2700 倍基准）

---

### AdvancedLootInfo
- 修复与 Supplementaries 的兼容性问题

---

### Annuus
- 修复原版网络类获取错误的问题

---

### NoApothesisNames
- 修复 Apotheosis 无法获取生物名称导致的崩溃问题

---

### Flywheel
- 修复 `BakedModelBufferer` 在空模型输入时触发 NPE

---

### Create
- 尝试修复 JEI 注册异常问题

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
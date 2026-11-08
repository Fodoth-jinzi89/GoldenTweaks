# AcademyCraft 技能伤害与领地保护汇总

本文档对应 GoldenTweaks 针对 AcademyCraft 的两项改动：

1. 寂静装备（Silent Gear）的「魔法攻击力」（`magic_damage`）按档位提升技能造成的最终伤害；
2. 能破坏方块的技能不得绕过 FTB Chunks 的领地保护。

---

## 一、技能伤害的统一链路

AcademyCraft 中所有**由玩家技能造成的伤害**最终都汇聚到同一个入口：

`com.mohistmc.academy.skill.AcademyDamageHelper`

| 方法 | 用途 | 本次是否加成 |
|---|---|---|
| `hurt(attacker, target, source, amount)` | 玩家技能对实体造成伤害（绝大多数技能） | ✅ 加成 |
| `hurtSelf(attacker, target=attacker, ...)` | 技能对**自己**造成的反噬伤害 | ❌ 不加成 |
| `hurtNonAbility(target, source, amount)` | 与技能无关的伤害（如物品效果，无攻击者） | ❌ 不加成 |

- 多数技能在调用 `hurt` **之前**会先经过 `DynamicSkillRules.damage(skillId, raw)`，按「该技能单独的 damage 系数 × 全局 `skillDamageMultiplier`」预乘一次。
- 少数技能直接把原始伤害传给 `hurt`（没有再乘 `DynamicSkillRules.damage`），例如火山球、等离子球体伤害、熔解射线的伤害等。
- 反射类伤害（矢量反射、定向反射）同样走 `hurt`。

**结论**：把加成放在 `AcademyDamageHelper.hurt` 上，等于在所有技能的最末端做最后一次乘算，覆盖最全面，且与各技能自己的伤害公式无关。

---

## 二、各技能造成伤害的方式一览

### 风系 Aerohand

| 技能 | 伤害方式 |
|---|---|
| 空气刃 `air_blade` | `hurt(player, target, magic, DynamicSkillRules.damage(...) * 保留系数)` |
| 轰炸长枪 `bomber_lance` | `hurt(..., playerAttack, DynamicSkillRules.damage(...))` |
| 风暴核心 `storm_core` | `hurt(owner, ..., DynamicSkillRules.damage("storm_core", power))`，另有第 2 段伤害 |
| 火山球 `volcanic_ball` | `hurt(player, target, playerAttack, damage)`（伤害未再乘 `DynamicSkillRules`） |
| 空气墙 `air_wall` | 命中判定后 `hurt(...)`（阻挡/反弹） |
| 空气分离 `aero_separator` | 对其它实体 `hurt(...drown...)`；对**自己**用 `hurtSelf`（反噬） |
| 风之被动 `AeroPassiveRuntime` | `hurt(...)` |
| 气流 / 喷气 / 飞行 / 进攻护甲 | 不造成伤害（位移、防护） |

### 电击使 Electromaster

| 技能 | 伤害方式 |
|---|---|
| 电磁炮 `railgun` | `hurt(player, target, playerAttack, DynamicSkillRules.damage("railgun", 衰减伤害))`；同时 `destroyBlock` 破坏方块 |
| 雷击之枪 `thunder_bolt` | 走 `ElectromasterDamageHelper.attack` |
| 雷暴 `thunder_clap` | `hurt(..., DynamicSkillRules.damage("thunder_clap", 衰减))` |
| 电弧生成 `arc_gen` | `ElectromasterDamageHelper.attack` + 点燃方块（`setBlockAndUpdate(FIRE)`） |
| 磁力操控 / 磁力移动 / 矿物探测 / 充电 / 身体强化 / LF | 一般不直接造成伤害 |

> `ElectromasterDamageHelper.attack` 只是 `AcademyDamageHelper.hurt` 的一层包装（额外给爬行者附加充能），伤害仍落回同一个入口。

### 原子崩坏 Meltdowner

| 技能 | 伤害方式 |
|---|---|
| 原子崩坏 `meltdowner` | `hurt(..., playerAttack, damage)`（两处）+ `destroyAlongRay` 破坏方块 |
| 采掘射线 `mine_ray_basic/luck/expert` | 不伤实体；持续 `removeBlock` 挖掘方块 |
| 电子炸弹 / 电子导弹 / 射线弹幕 | 各自 `hurt`，或先生成投射物再 `hurt` |
| 散射炸弹 `scatter_bomb` | 对他人 `hurt`；对**自己** `hurtSelf(6.0)`（反噬） |
| 光盾 / 喷气引擎 / 辐射强化 | 不直接造成伤害 |

### 念动力 Telekinesis

| 技能 | 伤害方式 |
|---|---|
| 巡航炸弹 `cruise_bomb` | `hurt(..., playerAttack, damage)` |
| 纸钻 `paper_drill` | `hurt(..., playerAttack, damage)` |
| 精神重击 `psycho_slam` | `hurt(..., DynamicSkillRules.damage("psycho_slam", 15 + 5 * 熟练度))` 等两处 |
| 液态阴影 `liquid_shadow`（被动） | `hurt(..., DynamicSkillRules.damage("liquid_shadow", rawDamage))` |
| 精神投射物（投掷 / 针 / 传送） | 由 `PsychoProjectileEntity` 命中后 `hurt(...)` |
| 完美纸 / 精神硬化 / 绝缘 / 超载思维 | 不直接造成伤害 |

### 空间移动 Teleporter

| 技能 | 伤害方式 |
|---|---|
| 撕裂肉块 `flesh_ripping` | `hurt(...)` |
| 空间波动 / 威胁传送 / 瞬移类 | 伤害型走 `hurt(...)`，其余为位移 |

### 矢量操作 Vecmanip

| 技能 | 伤害方式 |
|---|---|
| 血逆流 `blood_retro` | `hurt(..., DynamicSkillRules.damage(...))` |
| 定向爆破 `dir_blast` | `hurt(..., DynamicSkillRules.damage(...))` + `destroyBlock`（先发 `BreakEvent`） |
| 定向冲击 `dir_shock` | `hurt(...)` |
| 地表冲击 `ground_shock` | `hurt(...)` + `setBlockAndUpdate`（草→圆石/泥土）+ `destroyBlock`（先发 `BreakEvent`） |
| 风暴之翼 `storm_wing` | `hurt` 相关 + `destroyBlock`（先发 `BreakEvent`） |
| 等离子炮 `plasma_cannon` | 生成 `PlasmaOrbEntity`，其 `hurt(player, ..., explosion, damage)` + `explode(Interaction.BLOCK)` |
| 矢量加速 / 偏移 / 反射 | 位移、反射；反射伤害走 `hurt` |

### 通用 / 被动 / 投射物

| 来源 | 伤害方式 |
|---|---|
| 基础系（脑内课程 / 心灵课程） | 只给经验，不造成伤害 |
| 被动技能 `PassiveDamageHelper` | `hurt(player, target, playerAttack, DynamicSkillRules.damage(skillId, damage))` |
| 被动事件（矢量偏移反射火球等） | 反射；大型火球被消解时 `explode(null, ..., MOB)`（**无 source** 的爆炸） |
| 电磁枪 `Electricalibur`（物品） | `hurtNonAbility(target, playerAttack, 30)`（**无攻击者**，故不加成） |
| 磁力钩 `EntityMagHook` | `hurt(player, target, playerAttack, 4.0)` |
| 磁力操控方块 `MagManipBlockEntity` | `hurt(p, target, playerAttack, this.damage)` |
| 等离子球 `PlasmaOrbEntity` | `hurt(player, entity, explosion(player, player), damage)` |

---

## 三、改动 1：魔法攻击力加成技能伤害

**实现位置**

- `compat/academy/GTAcademyMagicDamage` —— 倍率计算
- `compat/academy/GearMagicAttack` —— 读取寂静装备属性
- `mixin/feature/academy/AcademyDamageHelperMixin` —— 注入 `AcademyDamageHelper.hurt`

**规则**

1. 取**主手与副手**寂静装备 `magic_damage`（魔法攻击力）的**最大值**；
2. `最终伤害倍率 = 1 + 魔法攻击力 ÷ 每档点数 × 每档增伤`；
3. 默认 **每 5 点 +10%**（5 → 110%，10 → 120%，25 → 150%）。

**覆盖范围**：所有走 `hurt` 的玩家技能伤害。

**暂不覆盖**：技能对自身的反噬伤害（`hurtSelf`）、无攻击者的物品伤害（`hurtNonAbility`）。如需要可以再开。

**配置项**（`config/goldentweaks-common.toml` → `[balance.academy]`）

| 键 | 默认 | 说明 |
|---|---|---|
| `magicAttackDamage` | 开 | 总开关 |
| `magicAttackPerStep` | 5 | 每多少点魔法攻击力算一档 |
| `magicAttackBonusPerStep` | 0.10 | 每档增伤比例 |

---

## 四、改动 2：技能破坏方块不得绕过 FTB Chunks 领地保护

**现状**

AcademyCraft 那些会破坏方块的技能**不走原版破坏流程**，而是自行构造并发布 NeoForge 的 `BlockEvent.BreakEvent`：

- 电磁炮 `railgun`、采掘射线 `mine_ray_*`、熔解射线 `meltdowner`、定向爆破 `dir_blast`、
  地表冲击 `ground_shock`、风暴之翼 `storm_wing`、精神投射物 `PsychoProjectileEntity`、磁力操控 `MagManip`。

另有通过爆炸破坏方块的路径（等离子炮、矢量偏移反射火球）。

FTB Chunks 在 NeoForge 上通过 Architectury 监听 `BlockEvent.BreakEvent` 并取消，理论上应当拦截上述自行发布的事件。

**本次改动（加固）**

新增 `compat/ftbchunks/GTClaimProtection`，直接注册到 NeoForge 事件总线（`EventPriority.LOWEST`），对每个 `BreakEvent` **再按 FTB Chunks 自己的规则复核一次**：

```
ClaimedChunkManagerImpl.getInstance()
    .shouldPreventInteraction(player, MAIN_HAND, pos, FTBChunksExpected.getBlockBreakProtection(), null)
```

判定为「禁止」即取消事件。这样即便技能自行发布事件、或 FTB 的桥接在某个环境下未生效，技能也无法在他人领地内破坏受保护方块；对普通玩家挖方块没有任何行为改变（FTB 原本就会取消，这里只是复核，且排在最后执行）。

**配置项**：`[balance.academy] skillBlockClaimProtection`（默认开）。

**备注**：技能是否破坏方块本身仍受 AcademyCraft 自己的 `destroyBlocks` / 世界白名单 / 每技能 `destroy_blocks` 控制，本次改动只在其之上叠加领地保护。

---

## 五、验证

- `gradlew.bat compileJava --offline` 通过。
- 建议实测：
  1. 在他人领地内用电磁炮 / 采掘射线轰击 —— 方块不应被破坏；
  2. 手持带魔法攻击力的寂静装备释放技能 —— 伤害应随魔法攻击力提升（每 5 点 +10%）。

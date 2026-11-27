# 化学反应配方开发指南

本文面向要**编写反应配方**或**新增配方类型**的开发者（含 agent）。
配套文档：`multiblock-host-gui.md`（主机）、`multiblock-port-gui.md`（端口）。

化学反应这套配方系统不是原版的 `Recipe`——它实现 AMI 的 `IMultiblockRecipe`，
匹配、消耗、产物投递全在机器自己的 tick 里做。理解这条自建管线，比记住 JSON 字段更重要。

---

## 0. 一句话结论

写一条配方只需一个 JSON；但要让它**跑起来**，得理解这四件事：

1. **能源是按 tick 扣的**，不是开工时一次性收取。总成本 = `processing_time × energy_per_tick`。
2. **能量预算在开工前就检查了**，超预算的配方根本不会成为候选，不会"先扣料再跳闸"。
3. **输入在开工瞬间就离开端口**，不是完成时扣。玩家中途偷不走原料。
4. **`mode` 决定配方能被哪一组端口服务**，写错 mode 会让配方永远不会被选中。

---

## 1. 最小可用配方

`data/<namespace>/recipe/chemical_reaction/<name>.json`：

```json
{
  "type": "goldentweaks:chemical_reaction",
  "mode": 1,
  "inputs": [
    { "item": "minecraft:iron_ingot", "count": 4 },
    { "fluid": "minecraft:water", "amount": 1000 }
  ],
  "outputs": [
    { "item": "minecraft:gold_ingot", "count": 1 },
    { "fluid": "minecraft:lava", "amount": 500 }
  ],
  "processing_time": 16,
  "energy_per_tick": 32
}
```

字段全部说明：

| 字段 | 必填 | 默认 | 说明 |
|---|---|---|---|
| `type` | 是 | — | 恒为 `goldentweaks:chemical_reaction` |
| `inputs` | 是 | — | 输入栈列表，可为空列表但没太大意义 |
| `outputs` | 否 | `[]` | 输出栈列表 |
| `processing_time` | 否 | `40` | 运行刻数 |
| `energy_per_tick` | 否 | `0` | 每刻扣的 FE |
| `mode` | 否 | `0` | 归属模式，`0` = 任意模式 |

`processing_time` 与 `energy_per_tick` 都是**可选的**，但两者共同决定总能耗，
建议总是显式写出，避免默认值带来的意外。

---

## 2. 栈的写法

`GTReactionStack` 的 codec 按**字段名**区分类型，所以一条栈只能有其中一个 kind 字段。

### 2.1 物品

```json
{ "item": "minecraft:iron_ingot", "count": 4 }
```

`count` 默认 **1**。

### 2.2 流体

```json
{ "fluid": "minecraft:water", "amount": 1000 }
```

`amount` 默认 **1000**（1 桶），单位 mB。

### 2.3 Mekanism 化学品

```json
{ "chemical": "mekanism:oxygen", "amount": 500 }
```

`amount` 默认 **1000**。需要装 Mekanism 才有效。

### 2.4 Thaumcraft 要素

```json
{ "essentia": "thaumcraft:ignis", "amount": 8 }
```

`amount` 默认 **1000**。需要装 Thaumcraft 才有效。

补一句设计说明：要素的量纲被刻意对齐到**物品数**——
层级 `essentiaTankCapacity()` 就是 `slotLimit()`（`64 × level`），
所以"要 3 个 ignis"读起来和"要 3 个锭"一样自然。

### 2.5 AE 键

```json
{ "ae": { "key": { ... }, "amount": 8 } }
```

`key` 里是 AE 自己的 NBT 结构，`amount` 默认 1000。
这条路径的 id 用占位符 `ae2:generic`，真实身份在 tag 里。

### 2.6 重要：kind 的 ordinal 不能重排

```java
public enum Kind {
    ITEM, FLUID, CHEMICAL,
    /** Appended rather than inserted: the ordinal is what the wire codec writes. */
    ESSENTIA,
    AE
}
```

网络包用 **ordinal** 编码。中间插入一个 kind，老客户端会把每个条目读成错误类型。
**只能往末尾追加。**

---

## 3. 执行语义（写配方前必须知道）

### 3.1 能源：按 tick 扣，开工前查预算

```java
// 完工后总能耗
public long totalEnergy() {
    return (long) this.processingTime * (long) this.energyPerTick;
}
```

但实际扣费是**每 tick 一次**，在 `tickRecipe` 里：

```java
// Billed at the rate pinned when the craft started, which is what the overclock bought.
int cost = activePowerDraw();
if (cost > 0 && !consumeEnergy(cost)) {
    tripBreaker();
    return;
}
this.progress++;
```

注释明确写了顺序：**"pay, then advance"** ——先付费再推进，付不起就跳闸。

**开工前的预算检查**（`withinEnergyBudget`）：

```java
public boolean withinEnergyBudget(int baseCostPerTick) {
    return baseCostPerTick <= 0 || baseCostPerTick <= maxRecipeCost();
}
```

关键细节，源码注释原话：

> 比的是**基础值**，不是超频后的值。超频是玩家拿电力换速度的选择，
> 不该决定哪些配方对这台机器"存在"——按加成后的成本判断，
> 会让一台刻意超频的机器在预算顶端什么也开不了。

所以：**写配方时 `energy_per_tick` 要和你的能源端口等级匹配**。
超出端口供应能力的配方会**静默不被选中**（见 3.5 的状态提示）。

### 3.2 输入在开工瞬间扣除

```java
// Inputs leave the ports as the craft starts, so what is in the machine always reflects
// what is still owed. Charging at completion let a player pull the ingredients back out
// mid-craft and still receive the products.
consumeInputs(batched);
```

这条对配方设计有实际影响：**输入是一次性全扣的，产物是完工一次性给的**。
不存在"流体每 tick 流入"这种逐步消耗。做连续生产时要注意缓冲区够不够。

### 3.3 跳闸是破坏性的

```java
/**
 * 掉电或结构破碎时丢弃当前配方。
 *
 * 按机器契约这是破坏性的：进度归零，已消耗的输入 <em>不</em> 返还，不产出任何东西。
 * 机器还会自己关掉，不会自行重启，玩家必须来看一眼。
 */
private void tripBreaker() { ... }
```

所以 `energy_per_tick` 写得太贴近端口上限很危险：一旦某 tick 供能不足，
整批原料蒸发。留余量。

### 3.4 关闭机器不会中止运行中的批次

```java
/**
 * 开关只限制 <em>开始</em>新工作：已在运行的配方允许做完，
 * 因为中途停止会静默吞掉输入。
 */
private boolean mayStartRecipe() {
    return this.running;
}
```

### 3.5 被预算拒绝时会有状态提示

配方因成本被跳过时**不是无声的**，机器会记录：

```java
if (!withinEnergyBudget(recipe.getEnergyPerTick())) {
    this.refusedForCost = true;
    // 零表示"还没记录"，所以第一个被拒的配额优先，之后取最小值
    this.refusedCost = this.refusedCost == 0
            ? recipe.getEnergyPerTick()
            : Math.min(this.refusedCost, recipe.getEnergyPerTick());
    continue;
}
```

界面状态行因此能说出"配方需要 X FE/t，超出供应"。
**但这个记录是按搜索顺序覆盖的**——只有第一个（最便宜的那个）会被记住。

---

## 4. `mode` 到底做什么

### 4.1 三层筛选

一次配方查找经过三层过滤（`findLockedRecipe`）：

```
for (每个通道组 channel : channelGroups())        ← 第一层：通道
    for (每个模式 mode : modeOrder())              ← 第二层：模式（按端口重要性排序）
        matchOneGroup()                            ← 第三层：配方匹配
```

**通道**是端口的 `channel` 字段（见端口文档），决定哪些端口算"同一条产线"。
**模式**是端口在结构里的排序结果。

### 4.2 两遍扫描：精确模式优先

```java
/**
 * 两遍，不是一遍。通配配方在任何模式下都合法，单遍会让一个通配候选仅仅因为
 * 在配方列表里排得靠前就胜出——模式"检查过"却从未真正被偏爱。
 * 因此按具体程度排序：为本模式写的配方先试，通配配方只在那些全失败后才轮到。
 */
Integer mode = this.selectedMode;
if (mode != null && mode != GTPortChannels.WILDCARD) {
    MatchedMultiblockRecipe exact = matchRecipesOfMode(level, input, mode);
    if (exact != null) return exact;
}
return matchRecipesOfMode(level, input, null);
```

**这对配方作者的直接影响**：如果你写了一条 `mode: 0` 的通配配方，
它永远会输给同条件下 `mode` 精确匹配的配方。想让某条配方在特定产线上优先，写确切 mode。

### 4.3 通配的两个方向

```java
private static boolean modeAccepts(@Nullable Integer mode, int recipeMode) {
    if (mode == null || mode == GTPortChannels.WILDCARD) {
        return true;
    }
    return recipeMode == GTPortChannels.WILDCARD || recipeMode == mode;
}
```

两个通配在这里相遇：

- **配方写 `mode: 0`** → 在任何模式下都能跑。
- **当前模式是 `0`** → 接受任何配方。
- `null` 模式 → 完全不过滤（锁定配方的路径会用到）。

### 4.4 锁定配方仍然受限

```java
// A locked recipe still has to belong to the mode being tried: the lock names a recipe,
// not a permission to ignore the ports that make it.
if (!modeAccepts(this.selectedMode, recipe.mode())) return null;
// The lock names a recipe, not permission to drain a budget the ports no longer have —
// a player who removes an energy hatch must not keep running the old, costlier recipe.
if (!withinEnergyBudget(recipe.getEnergyPerTick())) return null;
```

玩家锁定配方**不等于**获得了忽略端口配置的权限。拆掉能源舱后，锁定的贵配方也跑不了。

---

## 5. 自定义 kind 的原料检查

AMI 的匹配器只懂物品和流体。要素和化学品必须自己查（`typedInputsAvailable`）：

```java
/**
 * 端口里是否有配方要的要素和化学品。
 * AMI 的匹配器只认识物品和流体。这两种得在这里查，
 * 否则机器会欢快地开始一个要素/化学品根本不在端口里的合成，
 * 然后什么也不产出——而它本该消耗那些原料。
 */
private boolean typedInputsAvailable(GTChemicalReactionRecipe reaction, int multiplier) {
    for (GTReactionStack input : reaction.inputs()) {
        if (input.kind() != Kind.ESSENTIA && input.kind() != Kind.CHEMICAL) continue;
        long needed = input.amount() * multiplier;
        long held = 0L;
        for (GTPortBlockEntity stored : typedPorts(true)) {
            if (input.kind() == Kind.ESSENTIA && stored.kind() == GTPortKind.ESSENTIA) {
                held += GTPortStorages.essentiaAmount(stored.essentiaStorage(), input.id());
            } else if (input.kind() == Kind.CHEMICAL && stored.kind() == GTPortKind.CHEMICAL) {
                held += GTPortStorages.chemicalAmount(stored.chemicalStorage(), input.id());
            }
        }
        if (held < needed) return false;
    }
    return true;
}
```

**新增配方 kind 时必须同步改这里**，否则那条配方会"匹配成功但什么都产不出"。

---

## 6. 批次与超频

两个会改变实际运行参数、但不改配方的机制。写配方时要意识到它们的存在。

### 6.1 批次：短配方被放大

```java
/**
 * 把短反应放大到至少跑够配置的最短刻数。
 * 倍率是达到下限的最小整数，所以一批"刚好够长"而非无谓地长。
 * 然后按机器实际能力往下降：在手输入和空闲产出空间都会限制一次能折多少份。
 */
private MatchedMultiblockRecipe batchRecipe(MatchedMultiblockRecipe recipe) {
    if (!this.batchMode) return recipe;
    int floorTicks = GoldenTweaksCommonConfig.CHEMICAL_REACTOR_BATCH_MIN_TICKS.get();
    if (baseTime >= floorTicks) return recipe;
    int wanted = (int) Math.ceil(floorTicks / (double) baseTime);
    ...
}
```

- 受 `CHEMICAL_REACTOR_BATCH_MIN_TICKS` 配置控制。
- **合并而非只是缩放**：一批产出四组同样的物品，列表里显示为一条 400 的条目，
  而不是四条抢槽位的重复行。
- 倍率为 1 时原样返回，长配方完全不受影响。

**写配方的影响**：`processing_time` 很短（比如 1~5 tick）的配方在开启批次时，
实际是按倍率放大的。设计产出率时按放大后的结果算。

### 6.2 超频：时间换电力

```java
/**
 * 三个上限同时生效，取最小。机器等级上限说这类机器能超到哪；
 * 玩家设置说他们想超到哪；供应上限说舱室实际付得起多少——每级把账单翻四倍。
 */
public int overclockSteps(int baseCostPerTick) { ... }
```

- 每级：**时间减半，每 tick 成本 ×4**。
- 时长有下限（至少 1 tick），所以"多超频只会更贵"这个规则成立。
- **预算检查用基础成本，不扣超频后的**（见 3.1）。

**写配方的影响**：`energy_per_tick` 决定了这条配方能被超多少次。
写得很高的配方可能一步都超不了。

---

## 7. 新增配方类型

基础釜和进阶釜**共用** `GTChemicalReactionRecipe` 类，但各有自己的 type 和 serializer。
这是关键设计：

```java
/**
 * 本实例所属的配方类型。携带而不是假设。
 * 进阶釜复用这个类，固定类型会把每条进阶配方归到基础类型下——
 * 结果是进阶页面空的，而基础页面列出它跑不了的配方。
 */
private final RecipeType<?> type;

/** 构建本实例的序列化器。理由同上。 */
private final RecipeSerializer<?> serializer;
```

如果要加**第三种**机器类型，照抄注册骨架：

```java
public final class GTFooRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, GoldenTweaks.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, GoldenTweaks.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GTChemicalReactionRecipe>> TYPE =
            TYPES.register("foo_reaction", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID, "foo_reaction")));

    // 声明在 TYPE 之后：序列化器要引用它，字段初始化式不能前向引用。
    public static final DeferredHolder<RecipeSerializer<?>,
            GTChemicalReactionRecipe.Serializer> SERIALIZER =
            SERIALIZERS.register("foo_reaction",
                    () -> new GTChemicalReactionRecipe.Serializer(TYPE.get()));
}
```

机器侧要覆盖 `recipeTypes()` 返回自己的 type，否则搜不到配方。

### 7.1 网络编码为什么不带 type

```java
// The wire carries no type: a recipe only ever travels to a client that already has the
// same type registered, and the instance that encoded it knows which one that is.
```

序列化器实例自己持有 type，`decode` 时用它重建。所以**同一份 JSON 的 `type` 字段
必须和注册的 type id 一致**，否则客户端解出来的实例归属于错误的 type。

---

## 8. 配方与配方查看器

`GTChemicalReactionRecipe` 对 JEI/EMI 暴露的是**扁平化视图**：

```java
/** Flattened ingredients, so JEI/EMI and AMI's own bookkeeping can enumerate the item inputs. */
@Override
public @NotNull NonNullList<Ingredient> getIngredients() {
    ...
    // A stack stands for itself; repeating it would inflate the ingredient count
    // without changing what the recipe accepts.
    ingredients.add(Ingredient.of(item.getItem()));
}
```

注意两点：

- 只暴露 **ITEM** 类型的输入，流体/化学品/要素/要素不在里面。
  查看器里想看全貌，走 `inputs()` / `outputs()`。
- `Ingredient.of(item.getItem())` **丢弃了 count**。这是刻意的——原版 `Ingredient`
  不含数量语义，重复添加只会虚增 ingredient 数量。

`getItemInputs()` / `getFluidInputs()` 等是给 AMI 自己看的，同样只返回对应子集：

> AMI's own interface only speaks items and fluids — `getItemInputs()` and friends
> therefore expose the matching subset, and the machine's own tick path reads the full lists.

**机器运行读的是 `inputs()` / `outputs()` 全量列表**，不是 `getItemInputs()`。
这条区别决定了自定义 kind 能否真正生效。

---

## 9. 排查清单

| 症状 | 最可能原因 | 检查点 |
|---|---|---|
| 配方完全不触发 | `mode` 与端口模式不匹配 | mode 必须等于某个端口组的模式，或写 `0` |
| 有提示"电力不足"但端口满电 | `energy_per_tick` 超过端口供应能力 | 降低 `energy_per_tick`，或换更高等级的能源端口 |
| 开工即跳闸、原料蒸发 | `energy_per_tick` 贴着端口上限 | 留余量；超频会把账单再翻四倍 |
| 配方匹配了但没产出 | 自定义 kind 没进 `typedInputsAvailable` | 新增 kind 时必须同步改那个方法 |
| 查看器里看不到流体 | 预期行为 | JEI/EMI 视图只有物品，见第 8 节 |
| 进阶釜页面是空的 | 配方 type 写错，或机器没覆盖 `recipeTypes()` | JSON 的 `type` 必须与注册 id 一致 |
| 解析配方时报错 | kind 字段拼错 / 同时给了两个 kind 字段 | 一条栈只能有 `item`/`fluid`/`chemical`/`essentia`/`ae` 之一 |
| 配方被"通配版"抢了 | 这是两遍扫描的预期行为 | 见 4.2，精确 mode 优先 |
| 短配方产出比预期多 | 批次放大 | 见 6.1，受 `CHEMICAL_REACTOR_BATCH_MIN_TICKS` 影响 |

### 快速自检

把机器开到**不接任何能源端口**，然后放上原料：

- 状态行显示"电力不足"并且能报出所需 FE/t → 配方**被匹配到了**，
  只是预算不够。链路正常，去加能源端口。
- 状态行一动不动、什么也不说 → 配方**根本没被匹配**，问题在 `mode` 或原料种类。

这个区分比看日志快：匹配失败和预算失败在日志里都不留痕迹。

---

## 10. 完整示例

一条混合三种 kind、带多产物的配方（改编自 `test_mixed_mode3.json`）：

```json
{
  "type": "goldentweaks:chemical_reaction",
  "mode": 3,
  "inputs": [
    { "item": "minecraft:coal", "count": 3 },
    { "fluid": "minecraft:water", "amount": 1000 },
    { "essentia": "thaumcraft:ignis", "amount": 8 },
    { "chemical": "mekanism:oxygen", "amount": 500 }
  ],
  "outputs": [
    { "item": "minecraft:shulker_shell", "count": 3 },
    { "fluid": "create:honey", "amount": 1000 }
  ],
  "processing_time": 64,
  "energy_per_tick": 128
}
```

这条配方要求：mode 3 的端口组、Thaumcraft 的要素端口、Mekanism 的化学品端口、
至少能供 128 FE/t 的能源端口。缺任何一样都不会被选中。

---

## 11. 参考实现索引

| 关注点 | 文件 |
|---|---|
| 配方类 / codec / 网络编码 | `compat/avaritia_more_items/GTChemicalReactionRecipe.java` |
| type 与 serializer 注册 | `compat/avaritia_more_items/GTChemicalReactionRecipes.java` |
| 栈的 JSON 格式 | `compat/avaritia_more_items/GTReactionStack.java` |
| 栈到真实对象的转换 | `compat/avaritia_more_items/GTReactionStacks.java` |
| 匹配 / 消耗 / 批次 / 超频 | `compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java` |
| 进阶釜独立 type | `compat/avaritia_more_items/GTAdvancedChemicalReactionRecipes.java` |
| 示例配方 | `data/goldentweaks/recipe/chemical_reaction/*.json` |

`GTChemicalReactionRecipe` 的类注释已经把整体设计讲完了，值得先读那一段：

> AMI ships recipe types for its own stations (compressor, roller press, …) but none for a
> generic reaction, so the kettle declares its own. Everything that makes a recipe usable by
> an AMI multiblock — `matches`, `assemble`, `getResultItem` — already has a working default
> on `IMultiblockRecipe`; only the data and the timing are supplied here.

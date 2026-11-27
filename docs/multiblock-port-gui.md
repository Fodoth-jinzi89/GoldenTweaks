# 端口 GUI 与行为开发指南

本文面向要在 GoldenTweaks 里**新增端口**或**接入端口体系**的开发者（含 agent）。
上一份 `multiblock-host-gui.md` 讲主机，这一份讲端口——两者是**完全不同的两套机制**，
照主机那套写法抄端口会一地坑，原因在下面第 1 节。

结论来自现有实现：`GTPortKind` / `GTPortTier` / `GTPortBlock` / `GTPortBlockEntity` /
`GTPortMenu` / `GTPortScreen` / `GTPortChannels` / `GTPortStorages`。

---

## 0. 一句话结论

端口的三个关键设计，和主机都不一样：

1. **GUI 是数据中心的，不是方块中心的**。开窗包里传 `位置 + kind + 槽位数`，
   菜单在客户端**重新解析**方块实体，动态数值全部走 `ContainerData` 同步。
2. **层级是独立的方块**，不是 blockstate。25 个层级 × 2 模式 × N 种 = 大量注册项，
   这样做是为了物品名能带层级（`I ULV input bus`）。
3. **主动传输只发生在 `mode == OUTPUT`**，且每个层级有自己的节拍和吞吐量。

---

## 1. 与主机 GUI 的根本差异

| | 主机 | 端口 |
|---|---|---|
| 开窗包内容 | 仅 BlockPos | BlockPos + kind 枚举 + 槽位数 |
| 菜单如何拿方块实体 | 从 BlockPos 重新查 | 同样重查，但**kind 以包内为准** |
| 动态数值来源 | 直接读方块实体 | `ContainerData`（`SyncedData`） |
| 客户端是否有实体 | 是 | 是（`getUpdateTag`/`getUpdatePacket` 已实现） |
| 菜单类型 | 每台机器一个 | **全部端口共用一个** `gt_port` |

端口共用一个菜单类型，靠包里的 `kind` 和 `slots` 区分。这是刻意的：
五种端口、25 个层级、2 个模式，正常做法要注册 250 个菜单类型，
而它们除了图标和容量之外没有区别。

---

## 2. 开窗链路

### 2.1 服务端：写三个值

`GTPortBlock#useWithoutItem`（第 180 行起）：

```java
if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof GTPortBlockEntity port
        && player instanceof ServerPlayer serverPlayer) {
    serverPlayer.openMenu(port, buffer -> {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(port.kind());
        buffer.writeVarInt(port.itemHandler() == null
                ? 0 : Objects.requireNonNull(port.itemHandler()).getSlots());
    });
}
return InteractionResult.sidedSuccess(level.isClientSide);
```

**为什么 kind 要从服务端写而不是客户端自己读**（源码注释原话）：

> 客户端读不可靠：它只能读自己那份方块实体副本，而那份副本只被告知物品堆，
> 从不被告知布局。

这一点很关键。客户端的方块实体有数据（因为实现了 `getUpdateTag`），
但**它的槽位数、kind 派生关系不一定完整**，必须在开窗时由服务端权威地告诉它。

### 2.2 客户端：按同样顺序读回

`GTPortMenu#MENU`（第 43 行）：

```java
public static final DeferredHolder<MenuType<?>, MenuType<GTPortMenu>> MENU =
        MENU_TYPES.register("gt_port", () -> IMenuTypeExtension.create(
                (id, inventory, data) -> new GTPortMenu(id, inventory,
                        data.readBlockPos(), data.readEnum(GTPortKind.class),
                        data.readVarInt())));
```

**读写顺序必须严格一致**：BlockPos → enum → varInt。`writeEnum`/`readEnum` 用
`GTPortKind` 的 ordinal 编码，所以**枚举常量的顺序不能随便重排**——重排会让旧存档/旧包
解析成别的 kind。

### 2.3 菜单构造

```java
public GTPortMenu(int id, Inventory inventory, BlockPos pos, GTPortKind kind, int slots) {
    super(MENU.get(), id);
    this.port = resolve(inventory, pos);
    this.clientSide = inventory.player.level().isClientSide;
    this.kind = this.port != null ? this.port.kind() : kind;   // ← 实体优先，包内值兜底
    this.data = new SyncedData();
    addDataSlots(this.data);

    addSlot(new SlotItemHandler(ownHandler(this.port, true), 0, TOOL_X, TOOL_Y));
    addSlot(new SlotItemHandler(ownHandler(this.port, false), 0, FILTER_X, FILTER_Y));
    // …玩家背包 3×9 + 快捷栏
}
```

注意 `this.kind` 的取值顺序：**能查到实体就用实体的 kind**，查不到才用包里传来的。
这样即便包里的值和实体不一致（比如重排过枚举），也能自愈。

---

## 3. 动态数值：`SyncedData`

端口最重要的一段设计。`GTPortMenu` 第 149 行起：

```java
private final class SyncedData implements ContainerData {
    private final int[] cache = new int[DATA_COUNT];

    @Override
    public int get(int index) {
        if (!GTPortMenu.this.clientSide) {
            return switch (index) {          // 服务端：现算
                case DATA_AMOUNT -> GTPortMenu.this.filled();
                case DATA_CAPACITY -> GTPortMenu.this.capacity();
                case DATA_RATE -> GTPortMenu.this.inputRate();
                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;
                case DATA_ACTIVE -> GTPortMenu.this.isActivePort() ? 1 : 0;
                default -> 0;
            };
        }
        return this.cache[index];            // 客户端：读缓存
    }

    @Override
    public void set(int index, int value) {
        this.cache[index] = value;           // 原版网络层定期调用
    }
}
```

同步的五个值：

| 常量 | 含义 |
|---|---|
| `DATA_AMOUNT` | 已存（物品=总和，流体=mB，能量=FE） |
| `DATA_CAPACITY` | 容量，单位同上 |
| `DATA_RATE` | 输入速率（FE/tick） |
| `DATA_SINGLE_CELL` | 每个条目占一格（0/1） |
| `DATA_ACTIVE` | 是否穿面主动传输（0/1） |

**为什么不直接读客户端实体**：源码注释说客户端那份副本
「只被告知物品堆和字段文本，从不是流体或能量的数量」。所以数值必须靠 `ContainerData`。

新增一个要显示的数值，改 `DATA_COUNT`、加常量、在 `get` 里加一个 case、
在屏幕里调 `menu.syncedXxx()`。**不要**试图在客户端读实体算。

---

## 4. 槽位设计

只有三类真实槽位，**端口内容本身不占槽位**：

```java
public static final int TOOL_INDEX = 0;      // 工具槽（扳手之类）
public static final int FILTER_INDEX = 1;    // 过滤槽
private static final int OWN_SLOTS = 2;
```

`TOOL_INDEX`/`FILTER_INDEX` 恒定在前两位，**与玩家背包槽数无关**。这一点很重要：
`quickMoveStack` 靠这两个下标区分「哪个是自己的槽」。

端口内容（仓库里的物品、罐里的流体）**不进槽位系统**：

> A port's own contents are not slotted: the window lists them and reads them from the block
> entity on both sides, so nothing about them travels through the slot sync.

它们在屏幕上是一块 3×3 的**展示区**（`STORAGE_CELLS = 9`），
按页翻阅端口实际持有的条目。所以不要试图用槽位表达端口内容。

客户端没有实体时，`SlotItemHandler` 仍需要一个落脚点：

```java
private static final ItemStackHandler CLIENT_TOOL = new ItemStackHandler(1);
private static final ItemStackHandler CLIENT_FILTER = new ItemStackHandler(1);
```

两个独立实例，避免工具槽和过滤槽互相覆盖。

---

## 5. 层级系统

### 5.1 为什么是独立方块

`GTPortTier` 有 25 个值（ULV → MAX）。每个 tier 是**独立的方块和物品**，不是 blockstate：

> Each tier is its own block and its own item rather than a block state value. That is what
> lets the inventory name a part "I ULV input bus" instead of a generic "input bus".

物品不携带 blockstate，所以层级放进状态就「在手上看不见了」。

注册名规则（`GTMultiblockBlocks#declarePorts`）：

```
gt_port_<kind>_<mode>_<tier>
例：gt_port_item_input_ulv, gt_port_fluid_output_max
```

25 × 2 × 5 = 250 个方块，全部绑到**同一个** `BlockEntityType`（`gt_port`）。

### 5.2 与 AMI 的层级桥接

`GTPortTier#ami()`：

```java
public MultiblockPortTier ami() {
    MultiblockPortTier[] all = MultiblockPortTier.values();
    return all[Math.min(all.length - 1, ordinal())];   // 钳到 AMI 的最高级
}
```

AMI 自己的梯子只到 IX。我们多出来的 UHV → MAX **钳到 IX**，
因为它们作为结构零件仍然有效，拒绝组装没有意义。

**这意味着：结构扫描看到的是 IX，不是 UHV。** 如果下游要按层级做结构判定，
必须知道这个钳位。

### 5.3 容量与吞吐

全部由 `level()` 派生，不要另建表：

```java
itemSlots()      = level²            // ULV=1, MAX=625
slotLimit()      = 64 × level
fluidTanks()     = level
tankCapacity()   = 8000 << (level-1) // 几何增长：8000 … 2,048,000
chemicalTanks()  = level
chemicalTankCap()= tankCapacity/10
essentiaTanks()  = itemSlots()
essentiaTankCap()= slotLimit()       // 与物品槽一致：三个要素 ≈ 三个锭
reachInterval()  = ULV:80, LV:20, 其余:1
reachItems()     = 1,1,4,16,64,64,64,256,1024,… MAX:Integer.MAX_VALUE
reachFluid()     = 1000,1000,4000,16000,64000,…
```

注意 `reachFluid` **不是** `reachItems × 1000`，是单独的表。

---

## 6. 行为：主动传输

### 6.1 唯一的 tick 入口

`GTPortBlockEntity#serverTick()`（第 735 行）：

```java
public void serverTick() {
    if (!this.active || !this.enabled || this.level == null || this.level.isClientSide) {
        return;
    }
    boolean stepDue = --this.reachCountdown <= 0;
    if (stepDue) {
        this.reachCountdown = portTier().reachInterval();
    }

    Direction front = front();
    BlockPos target = this.worldPosition.relative(front);
    if (!this.level.isLoaded(target)) {
        return;
    }

    switch (kind()) {
        case ITEM -> { if (stepDue) reachItems(target, front); }
        case FLUID -> { if (stepDue) reachFluids(target, front); }
        case ENERGY -> reachEnergy(target, front);          // 每 tick，不受节拍限制
        case ESSENTIA -> { if (stepDue) reachEssentia(target, front); }
        case CHEMICAL -> { if (stepDue) reachChemicals(target, front); }
    }
}
```

要点：

- **`active` 默认只在 `mode == OUTPUT` 时为真**：

  ```java
  this.singleCell = mode() == PortMode.INPUT;
  this.active     = mode() == PortMode.OUTPUT;
  ```

  源码解释：INPUT 是「被机器伸手进来取」，OUTPUT 是「自己往前面推」。
  但这个默认值**可以被玩家开关改掉**（`toggleActive`）。

- **关闭的端口什么都不做，包括穿面传输**。注释明确指出这是刻意的：
  在 tick 里尊重开关而在穿面传输里不尊重，会让「已禁用」的端口继续搬东西。

- **能量不受节拍限制**。因为它的速率本身就是 FE/tick，再叠一个倒计时会重复限速。

### 6.2 传输方向由 mode 决定

```java
boolean input = mode() == PortMode.INPUT;
IItemHandler from = input ? theirs : ours;
IItemHandler to   = input ? ours   : theirs;
```

同样规则适用于流体、要素、化学品。新增一种 kind 时照这个模式写。

### 6.3 通道

`GTPortChannels` 是**纯函数解析器**，不存第二份状态：

```java
public static final int WILDCARD = 0;

// "0"      = 通配（加入所有组）
// "1,3"    = 只加入 1 和 3
// "0,3"    → 归一化为 "0"（通配 + 具体值 = 通配）
// 空/非法  → 视为通配（绝不静默排除）
public static String normalise(String raw);
public static List<Integer> effective(String raw);
public static boolean joins(@Nullable String raw, int channel);
```

**失败即通配**是刻意的：

> An empty result means the text is unparseable — callers treat that as the wildcard rather than
> as "no channels", so a broken field can never silently exclude a port from everything.

注意 `"1,,2"` 会被判为非法（返回空 → 通配），而不是宽容地当成 `"1,2"`：

> An empty segment is a malformed list, not a missing entry: "1,,2" is a typo and treating it as
> "1,2" would hide that from the player.

### 6.4 通道锁

`channelLocked` 是**机器在运行中设置**的，不是玩家设置：

```java
public boolean isChannelLocked()
public void setChannelLocked(boolean locked)
```

锁住时 `setText(Field.CHANNEL, ...)` 直接忽略写入，屏幕上该输入框变灰、不可编辑：

```java
boolean locked = port != null && port.isChannelLocked();
this.channelField.setEditable(!locked);
this.channelField.setTextColor(locked ? INACTIVE_TEXT_COLOUR : TEXT_COLOUR);
```

主机在开始合成时锁住参与端口，结束时解锁；**打破主机方块也必须解锁**
（`GTChemicalReactorMachineBlockEntity#onBlockRemoved()`），
否则端口会永久保留一个没人来解的锁。

### 6.5 源质端口：三条必须同时成立的链路

源质和其他四种 kind 不一样：它的接口由**两个模组分别定义**，而且两边的判定都**不走标准
NeoForge 能力查询**。少接一条，表现就是「要素推不进来」，并且**没有任何报错**。

#### 6.5.1 三条链路各管什么

| 链路 | 谁在问 | 要求 |
|---|---|---|
| `EssentiaCapabilities.TRANSPORT` 能力 | 神秘时代管道、`EssentiaEndpoints.transport` | 端口注册了 `IEssentiaTransport` |
| `IAspectContainer` 接口（在**方块实体**上） | TE 的样板供应器、存储总线 | 实体本身实现接口，不只是内部的 storage |
| `IEssentiaTransport.simulateAddEssentia` | 存储总线 / 样板供应器的 SIMULATE 阶段 | 返回真实余量，**不能依赖接口默认实现** |

#### 6.5.2 第一坑：`IAspectContainer` 必须在实体上

实现在 `GTPortEssentiaStorage` 上是不够的——TE 检查的是**方块实体**：

```java
// ThEEssentiaPushTarget.thE$typeAccepts，accessSide == null 时
blockEntity instanceof IAspectContainer
    && TCReflection.isEssentiaStore(blockEntity)
    && container.doesContainerAccept(aspect)      // 三条全中才认
```

`ProviderEssentiaTargets.wrap` 也先看 `blockEntity instanceof IAspectContainer`。

所以 `fix.thaumcraft.PortAspectContainerMixin` 把接口混入 `GTPortBlockEntity`，
九个方法全部转发给内部 storage。**用 mixin 而不是直接 `implements`**，
是因为 `IAspectContainer` 来自神秘时代，而端口实体在没有该模组的环境里也必须能加载。

> 注意：`GTPortBlockEntity` 本身不引用任何 Thaumcraft 类型，这个约束必须保持。

#### 6.5.3 第二坑（最容易漏）：`simulateAddEssentia` 的默认实现是 `return 0`

`IEssentiaTransport` 给了两个 default 方法，它们的字节码**只有两条指令**：

```
simulateAddEssentia   0: iconst_0   1: ireturn
simulateTakeEssentia  0: iconst_0   1: ireturn
```

而存储总线往目标写入时（`EssentiaContainerStorage.insert`）：

```java
if (actionable == SIMULATE) {
    if (transport == null || side == null) return 0;
    return Math.min(amount, transport.simulateAddEssentia(aspect, amount, side));
}
```

**SIMULATE 返回 0 就等于「目标收不了」**，于是根本不会发起真正的写入。不覆写这两个方法，
任何经 `IEssentiaTransport` 与端口交互的装置都会认为端口是空的：

- 存储总线贴着端口 → 要素停在网络里不动
- 样板供应器推要素 → 要素卡在供应器里（以通用堆栈的形式）

覆写时必须**用和真正写入相同的判据**（`admit()`：单格模式、同要素续加），
否则会出现「探测说能收、真写却失败」。

```java
@Override
public int simulateAddEssentia(Aspect aspect, int amount, Direction side) {
    Integer tank = admit(aspect);              // 与 add() 同一套规则
    if (tank == null) return 0;
    return Math.max(0, Math.min(amount, this.tankCapacity - this.amounts[tank]));
}
```

`simulateTakeEssentia` 同样要覆写，否则从端口抽要素的装置（存储总线、导出总线）
会认为端口是空的。

#### 6.5.4 第三坑：TE 的端点扩展点

`IEssentiaEndpointProvider` 是 TE 的公开 API，注册后 `EssentiaEndpoints.insert` **优先**走它，
而且**完全不看 `accessSide`**：

```java
// EssentiaEndpoints.insert 的优先级
IEssentiaEndpointProvider provider = EssentiaEndpointProviders.find(blockEntity);
if (provider != null) return provider.insert(...);   // ← 先走这里
// 否则回落到 IEssentiaTransport.addEssentia
```

`GTPortEssentiaEndpointProvider` 实现了它，注册在 `FMLCommonSetupEvent` 上
（那时 TE 的端点表已初始化完毕）。

这条不经过 SIMULATE 判定，是第二道保险；但**只靠它不够**——样板供应器的目标解析
（`EssentiaContainerStorage`）仍然走 transport 路径，也就是 6.5.3 那个 `return 0` 的坑。
两者都要。

#### 6.5.5 排查：一个反直觉的现象

如果在 ME 子网络里塞一个能存要素的存储元件（ami 全类型元件等），要素就「正常」了，
拔掉那个元件又不工作——**这不是元件解决了问题**。

元件只是让要素在网络内部被消化掉，绕开了「往端口推」这条判定。真正的推送到端口从未成功。
排查时不要被这个现象误导，直接检查 `simulateAddEssentia` 是否被覆写。

一个快速验证：临时在 `simulateAddEssentia` 里打一行日志，成功收到要素时应该能看到。
看不到就说明判定仍在返回 0。

#### 6.5.6 机器侧的对应要求

端口收下了要素，机器还得**认得它是必需原料**——否则会出现「要素不在端口，配方照样跑完」。
见 `multiblock-recipe.md` 的「执行语义」一节：`typedInputsAvailable` 必须在配方搜索
路径里也被调用，不能只在锁定配方那条路上。

---

## 7. 屏幕端

### 7.1 数据来源分三类

```java
// 1. 直接读菜单的同步值（客户端安全）
this.menu.syncedAmount(), syncedCapacity(), syncedRate()

// 2. 读菜单上的文本字段（内部已处理 null 兜底）
this.menu.modeText(), channelText(), priorityText()

// 3. 需要方块实体的（客户端也拿得到，但要判 null）
GTPortBlockEntity port = this.menu.port();
if (port == null) return;
```

`GTPortMenu` 对那些文本访问器都写了兜底：

```java
public String modeText() {
    return this.port == null ? GTPortBlockEntity.DEFAULT_TEXT : this.port.modeText();
}
```

**新加文本字段时也要照此写兜底**，否则客户端实体还没同步到时会 NPE。

### 7.2 编辑框的发送时机

输入框不是每次按键都发包，而是 `send(...)` 里比对后发送：

```java
private void send(GTPortBlockEntity.Field field, String value) {
    GTPortBlockEntity port = this.menu.port();
    if (port == null) return;
    String current = switch (field) {
        case NAME -> port.getDisplayName().getString();
        case MODE -> port.modeText();
        case CHANNEL -> port.channelText();
        case PRIORITY -> port.priorityText();
    };
    if (!current.equals(value)) {
        // 与「在途值」也比一次：输入被服务端拒绝后，框里的值和端口不一致，
        // 只跟端口比会把玩家的更正当成「没变」而什么都不发。
        String baseline = this.pendingFields.getOrDefault(field, current);
        if (baseline.equals(value)) return;
        this.pendingFields.put(field,
                field == GTPortBlockEntity.Field.PRIORITY ? displayedPriority(value) : value);
        PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
    }
}
```

两个坑，注释里都写了：

1. **必须与在途值比较**。服务端可能拒绝写入（比如通道被锁），
   此时框和端口不一致；只跟端口比，玩家的改正是「没变」，于是永远发不出去。
2. **`pendingFields` 存的是显示形式**，不是线上形式。优先级会显示成 `1,600` 这样的分组，
   若存裸整数，`echo` 比对时永远匹配不上，字段会永久钉在 pending 值。

### 7.3 回显

`echo` 在服务端值回来时更新框内容，但**跳过正在编辑的框**：

```java
if (this.channelField == null || this.channelField.isFocused()) return;
echo(this.modeField, Field.MODE, port.modeText());
echo(this.channelField, Field.CHANNEL, port.channelText());
```

不跳过的话，玩家打字打到一半会被回包覆盖。

---

## 8. 新增一种端口 kind

以新增 `GAS`（某种气体）为例。

### 8.1 加枚举值

`GTPortKind`：

```java
GAS("gas");

@Override
public boolean isExtended() {
    return this == CHEMICAL || this == ESSENTIA || this == GAS;
}

@Override
public @NotNull ResourceLocation icon() {
    if (isExtended()) {
        return ResourceLocation.fromNamespaceAndPath("goldentweaks",
                "block/multiblock/port/input_" + this.id());
    }
    return ResourceLocation.fromNamespaceAndPath("avaritia_more_items",
            "block/multiblock_port/input/" + this.id());
}
```

**加上新值必须放在枚举末尾**（它是 `writeEnum`/`readEnum` 的 ordinal 编码，
中间插入会改变后续所有值的序号）。

### 8.2 加容量

`GTPortTier` 里加对应方法，比照 `chemicalTanks()` / `chemicalTankCapacity()`。

### 8.3 加能力

`GTPortCapabilities` 里为该 kind 注册能力，`GTPortStorages` 里实现具体搬运
（比照 `reachChemical`）。

**特别注意**：如果目标模组的接口有 default 方法返回「空/零」，一定要覆写它们
（`simulateAddEssentia` 就是这么踩的坑），否则对端会认为端口不接受任何东西。

### 8.4 菜单与屏幕

- `GTPortMenu#totalCells()` 的 `switch` 加一个 case。
- `GTPortMenu#filled()` 若该 kind 是单一总量（像能量），在那里特判；
  否则走默认的 `contents()` 求和。
- `GTPortBlockEntity#serverTick()` 的 `switch` 加一个分支。
- 如新增显示数值，按第 3 节扩 `ContainerData`。

### 8.5 资源

- 图标：`assets/goldentweaks/textures/block/multiblock/port/input_gas.png`
- 方块模型 / blockstate：250 个命名规律见第 5.1 节
- 语言键：`block.goldentweaks.gt_port_gas_input_ulv` 等，需带层级代码

---

## 9. 排查清单

| 症状 | 最可能原因 | 检查点 |
|---|---|---|
| 开界面即崩 `BufferUnderflow` | 读写顺序不一致 | `writeBlockPos/writeEnum/writeVarInt` ↔ `readBlockPos/readEnum/readVarInt` |
| kind 显示错乱 | 枚举常量被重排过 | `writeEnum` 按 ordinal 编码，**只能在末尾追加** |
| 数值永远是 0 | 屏幕直读了实体字段 | 必须走 `menu.syncedXxx()`（`ContainerData`） |
| 端口不主动搬东西 | `active` 为假，或 `enabled` 为假 | 默认只有 `OUTPUT` 才 active；玩家可关 |
| 能量搬得比预期慢 | 误以为它受节拍限制 | 能量**不受** `reachInterval` 限制 |
| 通道输入怎么改都没反应 | 通道被机器锁了 | `isChannelLocked()`；输入框应变灰 |
| 通道写了 `1,,2` 却变成通配 | 这是**刻意**的 | 空段视为非法，非法→通配 |
| 工具/过滤槽串位 | `TOOL_INDEX`/`FILTER_INDEX` 顺序被动过 | 它们必须恒为 0 和 1 |
| 结构扫描不认 UHV 以上端口 | AMI 钳位 | `ami()` 钳到 IX，这是预期行为 |
| 打破主机后端口通道锁死 | 没走 `onBlockRemoved()` | 主机 `onRemove` 必须通知机器解锁 |
| **源质推不进来** | `simulateAddEssentia` 未覆写 | 见 6.5.3，默认实现是 `return 0` |
| 要素推不进来，但有存储元件时正常 | 同上，元件只是绕开了判定 | 见 6.5.5，别被这个现象误导 |

### 快速自检

打开端口界面，看右下角的 `cells` 读数：

- 显示的是**层级对应的槽数**（ULV=1、LV=4、MV=9…）→ `totalCells()` 走的是实体分支，链路正常。
- 显示 **0** → `port == null`，客户端没解析到方块实体，问题在开窗包或实体同步。

注意 `totalCells()` 报的是**层级槽数，不是展示区的 9 格**：

> Not the size of the visible band: the band is a fixed 3 by 3 window that pages through
> whatever the port holds, so reporting its nine would say "9" for every tier and hide the very
> thing the readout is for.

---

## 10. 数据持久化

`getUpdateTag` / `getUpdatePacket` 已实现，所以客户端有实体副本。但记住：

- **客户端副本可靠的内容**：物品堆、文本字段（mode/channel/priority/name）。
- **不可靠的内容**：流体量、能量值。这些必须走 `ContainerData`。
- `saveAdditional` / `loadAdditional` 覆盖了存档读写；
  新增状态字段记得两边都加，且 `loadAdditional` 里要为缺失字段提供默认值（旧存档兼容）。

---

## 11. 参考实现索引

| 关注点 | 文件 |
|---|---|
| kind 枚举与图标 | `content/multiblock/GTPortKind.java` |
| 层级与容量表 | `content/multiblock/GTPortTier.java` |
| 方块 / 开窗 / 掉落 | `content/multiblock/GTPortBlock.java` |
| 行为 / tick / 通道锁 | `content/multiblock/GTPortBlockEntity.java` |
| 菜单 / SyncedData / 槽位 | `content/multiblock/GTPortMenu.java` |
| 屏幕 / 输入框 | `content/multiblock/GTPortScreen.java` |
| 通道解析 | `content/multiblock/GTPortChannels.java` |
| 搬运实现 | `content/multiblock/GTPortStorages.java` |
| 能力注册 | `content/multiblock/GTPortCapabilities.java` |
| 注册（250 方块） | `content/multiblock/GTMultiblockBlocks.java#declarePorts` |
| 源质容器接口混入 | `mixin/fix/thaumcraft/PortAspectContainerMixin.java` |
| 源质端点扩展 | `compat/thaumicenergistics/GTPortEssentiaEndpointProvider.java` |
| 源质存储与探测方法 | `compat/thaumcraft/GTPortEssentiaStorage.java` |

屏幕与菜单的 UI 坐标全部取自 `gt_port_io.png`，注释明确说
「every coordinate here is taken from that sheet rather than computed」。
改布局要先改图，不要改常量。

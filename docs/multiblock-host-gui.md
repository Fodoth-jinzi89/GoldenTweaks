# 多方块主机 GUI 接入指南

本文面向要在 GoldenTweaks 里**新增一台多方块机器**的开发者（含 agent）。重点讲清主机界面这条链路：
一次右键到屏幕弹出来之间，到底有哪些环节必须自己接上，缺哪一环会以什么方式失败。

所有结论都来自现有两台机器的实现：基础反应釜（`GTChemicalReactorMultiblock`）和进阶反应釜
（`GTAdvancedChemicalReactorMultiblock`）。照抄这两台的骨架通常比重新设计更快。

---

## 0. 一句话结论

GUI 打不开，九成是这三件事之一：

1. **`createMenu` 返回的菜单用了别的机器的 `MenuType`** —— 客户端按 type 找屏幕，找不到就静默不弹。
2. **开窗包里没写 BlockPos，或菜单工厂里没读回来** —— 客户端拿不到位置，解析不出方块实体。
3. **`RegisterMenuScreensEvent` 里没注册屏幕** —— 服务端开窗成功，客户端仍然一片空白。

下面按链路顺序展开。

---

## 1. 涉及的文件与职责

设新机器叫 `foo_controller`。

| 环节 | 文件 | 职责 |
|---|---|---|
| 方块 | `GTFooMachineBlock`（通常直接复用 `GTChemicalReactorMachineBlock`） | 右键开窗、朝向、合成判定 |
| 方块实体 | `GTFooMachineBlockEntity extends GTChemicalReactorMachineBlockEntity` | `getMachineId()` / `menuType()` |
| 菜单 | `GTChemicalReactorMenu` 或其子类 | 槽位、`ContainerData`、状态读取 |
| 屏幕 | `GTFooScreen extends GTChemicalReactorScreen` | 绘制、按钮 |
| 注册 | `GTFooMultiblock` + `GTMultiblockBlocks.register` | 方块/物品/方块实体/菜单类型 |
| 客户端注册 | `GTChemicalReactorClientEvents` | `RegisterMenuScreensEvent` 绑定屏幕 |

---

## 2. 关键：一次右键到 GUI 的完整链路

```
玩家右键
  └─ GTChemicalReactorMachineBlock#useWithoutItem            [服务端]
       serverPlayer.openMenu(tile, buffer -> buffer.writeBlockPos(pos))
         └─ 写入的是「位置」，不是方块实体本身
  ───────────────────────────────────────────── 网络 ───
       IMenuTypeExtension.create((id, inv, data) -> ...)     [客户端]
         └─ data.readBlockPos()  ← 必须与服务端的写入顺序一致
              └─ GTChemicalReactorMenu#create(type, id, inv, pos)
                   └─ resolveTile(inv, pos)  ← 从客户端世界里再查一次方块实体
  └─ 客户端按 MenuType 查已注册的 Screen
       RegisterMenuScreensEvent 里必须登记过这个 type
```

### 2.1 服务端：开窗

`GTChemicalReactorMachineBlock#useWithoutItem`（第 326 行起）：

```java
@Override
protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                           Player player, BlockHitResult hitResult) {
    if (level.isClientSide) {
        return InteractionResult.SUCCESS;
    }
    if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof GTChemicalReactorMachineBlockEntity tile) {
        serverPlayer.openMenu(tile, buffer -> buffer.writeBlockPos(pos));
    }
    return InteractionResult.CONSUME;
}
```

要点：

- **故意不调用 `super`**。AMI 的实现会在结构未成形时往聊天栏刷 `multiblock.unformed_hint`，
  而反应釜有自己的界面能显示状态，所以直接开窗。
- 这里传的 `tile` 提供 `MenuProvider`，`buffer` 承载附加数据。**附加数据只有一个 BlockPos**，
  不要塞别的东西——菜单工厂那边必须逐个按同样顺序读回。
- 客户端侧 `level.isClientSide` 提前返回 `SUCCESS`，避免重复开窗。

### 2.2 客户端：菜单工厂

`GTChemicalReactorMultiblock`（第 44 行）：

```java
public static final DeferredHolder<MenuType<?>, MenuType<GTChemicalReactorMenu>> MENU =
        MENU_TYPES.register("chemical_reactor", () -> IMenuTypeExtension.create(
                (containerId, inventory, data) ->
                        new GTChemicalReactorMenu(containerId, inventory, data.readBlockPos())));
```

`data.readBlockPos()` 必须和 2.1 的 `writeBlockPos` 一一对应。写成 `readBlockPos` 但服务端没写，
会在客户端抛 `BufferUnderflow`，表现是打开即崩或界面永远不出现。

### 2.3 客户端：从位置找回方块实体

`GTChemicalReactorMenu`（第 173 行起）：

```java
private static Container resolveContainer(Inventory inventory, BlockPos pos) {
    GTChemicalReactorMachineBlockEntity tile = resolveTile(inventory, pos);
    return tile != null ? tile : new SimpleContainer(1);
}

@Nullable
private static GTChemicalReactorMachineBlockEntity resolveTile(Inventory inventory, BlockPos pos) {
    return inventory.player.level().getBlockEntity(pos)
            instanceof GTChemicalReactorMachineBlockEntity tile ? tile : null;
}
```

这里用的是 **`inventory.player.level()`**，即客户端世界。因为客户端世界里那个位置本来就有
（同步过来的）方块实体，所以查得到。解析失败时退回一个空的 `SimpleContainer(1)`，
保证槽位数量不匹配时不会立刻炸——但界面会是空的，可以据此判断是不是这一步出了问题。

---

## 3. 最容易出错的一步：`menuType()`

服务端开窗走的是方块实体的 `createMenu`：

`GTChemicalReactorMachineBlockEntity`（第 1132 行起）：

```java
@Override
public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
        sendProductsTo(serverPlayer);   // 把进行中的配方产物补给玩家
    }
    return GTChemicalReactorMenu.create(menuType(), containerId, inventory, this);
}

protected MenuType<?> menuType() {
    return GTChemicalReactorMultiblock.MENU.get();
}
```

**`menuType()` 是为子类留的钩子。** 新机器必须覆盖它，返回**自己**的菜单类型。

如果不覆盖，用的就是基础反应釜的 `MENU`。此时：

- 服务端开窗成功（用的是那个已注册的 type，所以不报错）；
- 客户端按 type 找屏幕——找到的是**基础反应釜的屏幕**；
- 屏幕上画的是基础釜的布局，槽位/按钮全对不上；
- 如果那个 type 的屏幕恰好没注册（或已被别的机器占用），就是**什么都不弹**。

这个失败的隐蔽之处：**服务端一切正常，日志里也没有异常**，只是界面不对或不出。

同理，`getMachineId()` 决定结构文件名，也必须覆盖：

```java
public String getMachineId() {
    return "foo";   // → data/<ns>/multiblock/foo.json
}
```

---

## 4. 注册模板

### 4.1 机械侧（服务端也加载）

```java
public final class GTFooMultiblock {
    public static final String ID = "foo_controller";

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GoldenTweaks.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(GoldenTweaks.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoldenTweaks.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GoldenTweaks.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GTChemicalReactorMenu>> MENU =
            MENU_TYPES.register("foo_machine", () -> IMenuTypeExtension.create(
                    (containerId, inventory, data) ->
                            GTChemicalReactorMenu.create(menuType(), containerId, inventory,
                                    data.readBlockPos())));

    // 自引用绕行：工厂存在 MENU 里，不能在它自己的初始化式里写 MENU.get()
    private static MenuType<?> menuType() {
        return MENU.get();
    }

    public static final DeferredBlock<GTChemicalReactorMachineBlock> MACHINE =
            BLOCKS.register(ID, () -> new GTChemicalReactorMachineBlock(
                    GTMultiblockBlocks.properties(),
                    "foo",                              // 结构文件
                    GTFooBlockEntity::new,              // 方块实体工厂
                    GTMultiblockBlocks.SHELL_PP));      // 机壳

    public static final DeferredItem<BlockItem> MACHINE_ITEM =
            ITEMS.register(ID, () -> new GTChemicalReactorItem(MACHINE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GTFooBlockEntity>>
            MACHINE_ENTITY = BLOCK_ENTITIES.register(ID, () -> BlockEntityType.Builder.of(
                    GTFooBlockEntity::new, MACHINE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENU_TYPES.register(eventBus);
    }
}
```

注意 `MENU_TYPES.register(...)` 用 `IMenuTypeExtension.create(...)`（NeoForge 提供），
不是原版 `new MenuType<>(...)`。前者带 `FriendlyByteBuf`，才能传 BlockPos。

### 4.2 客户端：绑定屏幕

`GTChemicalReactorClientEvents`（第 21 行）：

```java
@SubscribeEvent
public static void registerScreens(RegisterMenuScreensEvent event) {
    if (!ModList.get().isLoaded("avaritia_more_items")) {
        return;
    }
    event.register(GTChemicalReactorMultiblock.MENU.get(), GTChemicalReactorScreen::new);
    event.register(GTAdvancedChemicalReactorMultiblock.MENU.get(),
            GTAdvancedChemicalReactorScreen::new);
    // ← 新机器在这里追加一行
}
```

`event.register(菜单类型, 屏幕构造器)`。**菜单类型一旦注册了却忘了绑屏幕，就是打不开**。

---

## 5. 方块实体的最小实现

```java
public class GTFooBlockEntity extends GTChemicalReactorMachineBlockEntity {

    public GTFooBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);   // 注意：若父类签名不同，以实际为准
    }

    @Override
    public String getMachineId() {
        return "foo";
    }

    @Override
    protected MenuType<?> menuType() {
        return GTFooMultiblock.MENU.get();   // ← 最常漏的一行
    }
}
```

只要覆盖这两个方法，其余（结构扫描、端口、配方、同步、产物广播）全部继承。

---

## 6. 挂接注册

`GTMultiblockBlocks#register`（第 342 行）：

```java
if (ModList.get().isLoaded("avaritia_more_items")) {
    declarePorts();
    BLOCK_ENTITIES.register(eventBus);
    GTPortMenu.MENU_TYPES.register(eventBus);
    GTChemicalReactorMultiblock.register(eventBus);
    GTAdvancedChemicalReactorMultiblock.register(eventBus);
    GTFooMultiblock.register(eventBus);        // ← 新机器在这里追加
    GTChemicalReactionRecipes.register(eventBus);
} else {
    StandaloneReactor.declare();
}
```

**必须在 AMI 存在时注册。** 主机类继承 AMI 的 `AbstractMultiblockBlock`，
没有 AMI 时触碰它会 `NoClassDefFoundError`，所以这段代码整体挂在 `ModList` 判断里。

另外：`chemical_plant_controller` 这个 id 是「有 AMI 就注册主机、没有就注册普通方块」的二选一。
**新机器请用全新的 id**，不要复用，否则会和这个条件注册撞车。

---

## 7. 排查清单

GUI 打不开时按顺序查：

| 症状 | 最可能原因 | 检查点 |
|---|---|---|
| 右键完全没反应 | `useWithoutItem` 没被调用，或服务端提前 return | 方块是否重写了该方法；`level.isClientSide` 分支是否写反 |
| 服务端正常、客户端空白 | 屏幕没注册 | `RegisterMenuScreensEvent` 里有没有这个 type |
| 打开后崩 `BufferUnderflow` | 读写 BlockPos 不对等 | `writeBlockPos` ↔ `readBlockPos` 是否成对 |
| 界面能开但内容全空 | `resolveTile` 返回 null | 客户端世界那个位置是否真有方块实体；`Tile` 的泛型是否写成了父类导致 `instanceof` 不匹配 |
| 内容和机器对不上（画的是别的釜） | `menuType()` 没覆盖 | 子类是否返回了自己的 `MENU` |
| 结构永远不成形 | `getMachineId()` 与结构文件名不符 | `data/<ns>/multiblock/<id>.json` 是否存在 |

### 一个快速自检

打开界面后看 `GTChemicalReactorMenu#getDisplayName()` 的返回值：

```java
public Component getDisplayName() {
    return this.tile != null
            ? this.tile.getDisplayName()
            : Component.translatable("block.goldentweaks.chemical_plant_controller");
}
```

- 显示的是**方块的自定义名** → `resolveTile` 成功，链路正常。
- 显示的是**固定的 fallback 文本** → `tile == null`，2.3 那一步失败了。

这条线索比看日志快得多，因为整条链路在服务端不会留下任何异常。

---

## 8. 与本题无关但会咬人的地方

- **`FORMED` 必须复用 AMI 的属性实例**（`AbstractMultiblockBlock.FORMED`）。
  AMI 的 `AbstractConnectBlock.updateFormation` 按**引用同一性**读写该属性，
  自己 `BooleanProperty.create("formed")` 出来的新实例查找会失败。
- **不要调用 `super.onPlace`**。AMI 的泛洪填充只认自己的方块家族，
  机壳和端口是普通方块，会被误判成「已成形」的 1×1×1。
  现有实现把 `onPlace` 整个留空，由方块实体自己扫描结构。
- **破方块要走 `machine.onBlockRemoved()`**，否则端口会保留运行中加的通道锁。
- `GTChemicalReactorItem` 的第三个构造参数是提示用的机壳语言键
  （如 `"block.goldentweaks.shell_pp"`），和真实机壳保持一致。

---

## 9. 参考实现

| 机器 | 路径 |
|---|---|
| 基础反应釜 | `compat/avaritia_more_items/GTChemicalReactorMultiblock.java` |
| 进阶反应釜 | `compat/avaritia_more_items/GTAdvancedChemicalReactorMultiblock.java` |
| 主机方块 | `compat/avaritia_more_items/GTChemicalReactorMachineBlock.java` |
| 方块实体 | `compat/avaritia_more_items/GTChemicalReactorMachineBlockEntity.java` |
| 菜单 | `compat/avaritia_more_items/GTChemicalReactorMenu.java` |
| 屏幕 | `compat/avaritia_more_items/client/GTChemicalReactorScreen.java` |
| 客户端注册 | `compat/avaritia_more_items/client/GTChemicalReactorClientEvents.java` |
| 无 AMI 兜底 | `content/multiblock/StandaloneReactor.java` |

进阶釜是把「共用方块类、共用菜单基类、只换注册」做到底的例子：
它的方块类**就是**基础釜的类，差异全在 `GTAdvancedChemicalReactorBlockEntity` 的
`getMachineId()` 与 `menuType()` 两个方法里。新机器优先照这个思路做。

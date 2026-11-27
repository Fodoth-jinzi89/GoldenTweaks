# 要素在 EMI 里无法按要素查配方：排查结论

## 结论

**这是神秘时代内置的 JEI 集成的限制，不是 GoldenTweaks 桥接的问题。**
要素可以作为 *输出* 反查用途，但永远不能作为 *输入* 反查配方 —— 因为
`AspectSourceRecipeManagerPlugin.itemUid` 只接受物品。

同样的行为在旧的独立模组 `scex_aspect_jei` 里也存在，所以这不是迁移引入的回归。

## 证据链

### 1. 查询入口只认物品

`thaumcraft.integration.jei.AspectSourceRecipeManagerPlugin`：

```java
// getRecipesForInput(ITypedIngredient) —— 44 字节
Object uid = itemUid(typedIngredient);
if (uid == null) return List.of();              // ← 要素在这里就出局了
return List.of(catalog.get().byInput().getOrDefault(uid, List.of()));

// itemUid(ITypedIngredient) —— 55 字节
Optional<ItemStack> stack = typedIngredient.getIngredient(VanillaTypes.ITEM_STACK);
if (stack.isEmpty() || stack.get().isEmpty()) return null;   // ← 只认物品
return helper.getUidForStack(stack.get(), UidContext.Ingredient);
```

传入要素（`JeiAspect`）时 `getIngredient(ITEM_STACK)` 返回 `empty`，`itemUid` 返回 `null`，
`getRecipesForInput` 立刻返回空列表。`isHandledInput` 又是拿 `getRecipesForInput` 判空的，
所以对要素一律返回 `false`。

### 2. 输出方向是支持的

`getRecipesForOutput`（110 字节）先试要素类型：

```java
Optional<JeiAspect> aspect = typedIngredient.getIngredient(JeiAspect.TYPE);
if (aspect.isPresent()) {
    Object recipe = catalog.get().byAspect().get(aspect.get().aspect());
    return recipe == null ? List.of() : List.of(recipe);
}
// 否则退回物品路径
Object uid = itemUid(typedIngredient);
```

`JeiAspectSources$Catalog` 也确实维护了三个索引：

| 字段 | 键 |
|---|---|
| `byInput` | 物品 UID |
| `byPhial` | 要素安瓿 |
| `byAspect` | `Aspect` 实例 |

所以「要素 → 用途」理论上通，「要素 → 配方」永远不通。

### 3. 旧独立模组的行为完全相同

`dev.scex.aspectjei.SourceRecipeManagerPlugin#getRecipesForInput` 的字节码里，
第一条指令同样是 `getstatic VanillaTypes.ITEM_STACK`，同样只用 `byInput()` 查表。
**两者在这里没有差别。**

### 4. ingredient 注册也完全等价

两个版本的注册调用逐字节相同：

```java
// 旧：dev.scex.aspectjei.AspectIntegration#ingredients
// 新：thaumcraft.integration.jei.ThaumcraftJeiPlugin#registerIngredients
mgr.register(JeiAspect.TYPE,
             Aspect.ordered().stream().map(JeiAspect::new).toList(),
             new JeiAspectHelper(),
             new JeiAspectRenderer(),
             JeiAspect.CODEC);
```

`getUid` 都是 `Aspect.getTag()`，`getResourceLocation` 都是 `thaumcraft:<tag>`，
所以 EMI 侧的 UID 映射一致。

## GoldenTweaks 侧已核对干净

`GTScexAspectBridge` 的反射链全部可解析（`script/debug/check_aspect_bridge.py` 可复跑）：

```
SCEX_INGREDIENT -> thaumcraft.integration.jei.JeiAspect
SCEX_HELPER     -> thaumcraft.integration.jei.JeiAspectHelper    (构造器 access=0x0000，需 getDeclaredConstructor)
SCEX_RENDERER   -> thaumcraft.integration.jei.JeiAspectRenderer  (同上)
JeiAspect.TYPE / JeiAspect(Aspect) / aspect()           OK
TMRVStack.ingredient / create(...)                      OK
```

已知的两个陷阱（都已处理）：

- `JeiAspectHelper` / `JeiAspectRenderer` 的构造器是**包内可见**的，
  `getConstructor()` 会抛 `NoSuchMethodException`，必须 `getDeclaredConstructor()` + `setAccessible(true)`。
- `TMRVStack.render` 直接 `getfield renderer` 然后 `invokeinterface getWidth()`，
  **不判空**，renderer 传 `null` 会在第一帧绘制时 NPE。

## 如果要把「要素 → 配方」补上

上游需要改 `AspectSourceRecipeManagerPlugin`：在 `itemUid` 之前先试 `JeiAspect.TYPE`，
用 `byAspect()` 反查。参照它自己 `getRecipesForOutput` 的写法即可。

在 GoldenTweaks 里补需要新增一个 `ISimpleRecipeManagerPlugin` 并
`addTypedRecipeManagerPlugin(SOURCE_TYPE, plugin)`，但那样会和上游插件对同一批配方
重复注册（EMI 里出现两份来源页），所以不建议在整合包侧硬补。

## 复现与验证

```bash
# 桥接反射链是否可解析
python script/debug/check_aspect_bridge.py

# 检查上游的两个方法（需先把类从 jar 里提取到 script/debug/out/）
python script/debug/disasm_full.py <AspectSourceRecipeManagerPlugin.class> getRecipesForInput
python script/debug/disasm_full.py <AspectSourceRecipeManagerPlugin.class> itemUid
```

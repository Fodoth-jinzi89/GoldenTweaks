# Thaumcraft4Tweaks 功能总结（参考用）

> 分析对象：`libs/reference/Thaumcraft4Tweaks-1.5.39.jar`
> 作者：glee8e ｜ 版本：1.5.39 ｜ 环境：Minecraft 1.7.10 / Forge（FML CoreMod）
> 实现方式：ASM 字节码注入（`net.glease.tc4tweak.asm.*`）+ 事件监听 + 自定义网络包 + NEI 集成 + 公开 API

---

## 一、性能优化（Lag Fix）

| 功能 | 说明 | 相关类 |
|---|---|---|
| 奥术工作台卡顿修复 | 工作台 GUI 的合成结果刷新间隔可配置（`updateInterval`，单位 tick）；默认每 tick 刷新改为按配置刷新 | `WorkbenchLagFix`、`ContainerArcaneWorkbenchVisitor`、`GuiResearchTable` 注入 |
| 奥术合成缓存 | 缓存奥术合成结果（`arcaneCraftingHistorySize`，0~256），避免 shift 批量合成反复全量搜索配方 | `modules/findRecipes/`（`FindRecipes`、`ArcaneCraftingHistory`） |
| 要素映射线程降优先级 | 调整 TC4 的 MappingThread 线程优先级（0=自动 / 1=强制 / 2=关闭） | `MappingThreadLowPriority`、`MappingThreadVisitor` |
| 物品要素标签缓存 | 缓存 `ThaumcraftApi.getObjectTags` 查询结果，避免反复遍历全物品表 | `ObjectTagsLagFix`、`modules/objectTag/`（`GetObjectTags`、`ObjectTagsCache`、`ObjectTagsMutation`） |
| 物品哈希计算优化 | 缓存 `generateItemHash` 结果（替代原版每 tick 全物品扫描的哈希实现） | `ItemHashLagFix`、`modules/generateItemHash/`（`GenerateItemHash`、`CustomItemStacks`、`RangedObjectTags`） |
| 坩埚配方查找缓存 | 按物品哈希缓存坩埚配方查询结果 | `modules/findCrucibleRecipe/`（`FindCrucibleRecipe`、`CrucibleRecipeByHash`） |
| 研究查找缓存 | 按 key 缓存 `ResearchItem` 查询 | `modules/getResearch/`（`GetResearch`、`ResearchItemCache`） |
| 粒子引擎修复 | 客户端 `ParticleEngine` 线程安全/泄漏问题，进服务器与读档时清理粒子 | `modules/particleEngine/ParticleEngineFix` |

## 二、客户端 GUI 增强

| 功能 | 说明 | 相关类 |
|---|---|---|
| 神秘学之书 GUI 缩放 | Thaumonomicon 界面可手动缩放（1.0~4.0）或按窗口尺寸自动推断（含上下限与是否考虑搜索区） | `BiggerResearchBrowser`、`GuiResearchBrowserVisitor` |
| 研究搜索框 | 手册右上角搜索框（取自 WitchingGadgets 并修正其被放大后的布局；开启后禁用 WG 自带搜索） | `modules/researchBrowser/ThaumonomiconIndexSearcher` |
| 搜索限定当前标签页 | 可选只搜索当前标签页 | `limitBookSearchToCategory` |
| 研究标签页翻页 | 标签页过多时出现上一页/下一页按钮，配套公开 API | `modules/researchBrowser/BrowserPaging`、`api/BrowserPagingAPI` |
| 研究完成度计数器 | 标签页显示"已学 X / 共 Y"，三种样式：关闭 / 当前 / 全部 | `modules/researchBrowser/DrawResearchCompletionCounter` |
| 研究标签排序 | 自定义研究标签页排列顺序（GUI 配置 + NEI dump 工具辅助） | `categoryOrder`、`ResearchCategoriesVisitor` |
| 清除通知按钮 | 存在 TC4 通知且打开聊天时显示清除按钮 | `modules/hudNotif/HUDNotification`、`addClearButton` |
| 研究台元素列表滚轮 | 研究台上要素列表可用鼠标滚轮滚动（可反转方向） | `ScrollFix`、`AddHandleMouseInputVisitor`、`inverted` |
| 节点渲染尺寸上限 | 限制超大灵气节点的渲染大小（纯视觉，不影响实际大小） | `NodeRenderUpperLimit`、`ItemNodeRendererVisitor`、`NodeLikeRendererVisitor`（兼容 ThaumicHorizons、Gadomancy 渲染器） |

## 三、玩法/机制调整（通用）

| 功能 | 说明 | 相关类 |
|---|---|---|
| 奥术工作台禁用原版合成 | 可选让奥术工作台不再充当普通工作台 | `checkWorkbenchRecipes`、`ContainerArcaneWorkbenchVisitor` |
| 小型罐子碰撞箱 | 罐类方块（要素罐、脑罐等）碰撞箱与方块轮廓一致（原版为 1×1×1） | `smallerJars`、`BlockJarVisitor` |
| 药瓶放置优化 | 使用药瓶时优先补满部分堆叠与当前槽位 | `alternativeAddStack`、`BetterAddItemStackToInventoryVisitor` |
| 更随机化的战利品袋 | 附魔书附魔随机、vis stone 存储量随机（无需重启服务器） | `moreRandomizedLoot`、`UtilsVisitor$GenerateLootVisitor` |
| 发射器发射原始箭 | 发射器中的 primal arrow 直接射出而非掉落 | `dispenserShootPrimalArrow`、`ItemEssenceVisitor` |
| 地之冲击伤害模式 | 震击法术可伤害实体类型：仅生物 / 除物品经验外全部 / 全部 | `earthShockHarmMode`、`EntityShockOrdVisitor` |
| 倾倒傀儡队列上限 | 限制 decant 傀儡的最大液体队列方块数，防服务器卡顿 | `decantMaxBlocks`、`AILiquidGatherVisitor` |
| 注魔配方增强 | 矿物词典替代模式（默认/严格/宽松/关闭）+ 输入 NBT 标签继承（附魔、自定义名、符文护盾等；可限定仅盔甲工具、白名单） | `InfusionEnhance`、`InfusionRecipeVisitor`、`modules/infusionRecipe/`（`InfusionOreDictMode`、`InfusionRecipeGetOutput`） |
| 冠军生物属性调整 | 配置 Champion 怪物的属性增益（多人时伤害加成可叠加） | `ChampionModConfig`、`ConfigurationAttributeModifier` |
| 灵气中继器链接持久化 | 将 vis relay 的父节点链接写入存档，读档时恢复，防重启掉链 | `VisNetPersist`、`TileVisRelayVisitor`、`modules/visrelay/SavedLinkHandler` |
| 补充服务器状态同步 | 对部分 tile entity 尽力补充发送服务端状态，修复罕见不同步（带宽/耗时略有增加） | `sendSupplementaryS35`、`AddOnDataPacketMarkBlockForRenderUpdateVisitor`、`TileHoleVisitor`、`TileHoleSyncPacket` |

## 四、Bug 修复（ASM 注入点）

- `ChunkCoordinates` / `WorldCoordinates` / `BlockCoordinates`：修复 hashCode 与 equals 不一致导致的 HashMap 查找错误
- `AspectList` 注入（`AspectListVisitor`）
- `TESRGetBlockTypeNullSafetyVisitor`：渲染器 getBlockType 空指针安全（多个 tile 渲染器）
- `TileMagicWorkbench` / `ContainerArcaneWorkbench`：奥术工作台容器/合成矩阵逻辑
- `TileInfusionMatrix`：注入矩阵 Math.abs 计算与合成开始逻辑
- `ItemWandCastingVisitor`：法杖物品 NBT 加载空指针检查
- `TileChestHungryVisitor`：饿鬼箱 decrStackSize
- `EntityGolemBaseVisitor` / `AIItemPickupVisitor`：傀儡搬运/拾取 AI
- `ScanManagerVisitor`、`PacketAspectCombinationToServerVisitor`、`PacketPlayerCompleteToServerVisitor`（网络包访问器）
- `FXBeamPowerVisitor` / `FXSonic`：光束渲染混合修复
- `TileAlchemyFurnaceAdvancedRendererVisitor` / `TileThaumatoriumRendererVisitor`：渲染修复
- `MazeHandlerVisitor` / `CellLoc`：异界迷宫（dim）保存/脏标记修复
- `BlockAiry` / `BlockFluxGas` / `BlockMagicalLeaves` / `BlockMagicalLog` / `BlockMetalDevice`：方块行为修复
- `EntityUtilsVisitor` / `UtilsVisitor`（setBiomeAt）/ `UtilsFXVisitor`：工具类修复
- `ReadMarkerNoCastVisitor`：研究标记相关修复
- `TileTube` / `TileJarFillable` / `TileVisNode` / `TileVisRelay`（drawEffect）等方块实体修复

## 五、漏洞利用修复（ExploitFix）

- `ThaumcraftVisitor$AddFakePlayerGuardVisitor`：添加假玩家防护（防刷物品类漏洞）
- 注魔/研究等相关注入点亦带防利用性质

## 六、NEI 集成

- `nei/NEIConfig`（IConfigureNEI）：工具菜单"TC4Tweaks"下可 dump：
  - 已注册 ItemStack 要素标签（`DumpObjectTags`）
  - 已注册研究（`DumpResearch`）
  - 已注册研究标签页（`DumpResearchCategories`，支持完整/仅名称两种模式，供配置 `categoryOrder` 使用）

## 七、公开 API（供其他模组/MineTweaker 调用）

- `api/TC4TweaksAPI`：总入口
- `api/BrowserPagingAPI`：手册翻页 API
- `api/InfusionExtAPI` + `api/infusionrecipe/`：增强注魔配方 API
  - `EnhancedInfusionRecipe`、`InfusionRecipeExt`（含 NBT 行为枚举 `RecipeNBTBehavior`）
  - `RecipeIngredient`（含或配方 `RecipeIngredientOr`、延迟 `RecipeIngredientDefer`）、`Utility`
- MineTweaker（ZenScript）兼容：`MTCompat`、`MTCompatForInfusionExt`
- 通过 `META-INF/services/` 注册 API 实现

## 八、网络与配置同步

- 自定义网络通道 `network/MyNet`（基于 FML 简单实现）
- 服务端→客户端同步关键配置（`NetworkedConfiguration` / `MessageSendConfiguration(V2)`）：`checkWorkbenchRecipes`、`smallerJars`、`infusionOreDictMode`（保证客户端行为与服务端一致）
- 客户端/服务端配置一致性检测（`detectAndSendConfigChanges`、`checkConnection`）
- `TileHoleSyncPacket`：洞方块（TileHole）状态同步

## 九、其他

- 完整 FML 配置 GUI（`GuiFactory` / `GuiModConfig`），配置分"客户端"与"通用"两大类，含语言文件（en_US / ru_RU）
- 模组 JAR 指纹校验（`KNOWN_SIGNATURE`、`getFingerprintDescriptions`）
- 兼容多个 TC4 系附属模组（ThaumicHorizons、Gadomancy、WitchingGadgets、MineTweaker 等）

---

## 备注（移植参考）

以上功能大多依赖 1.7.10 TC4 内部类与 ASM 注入，若要在本仓库（NeoForge 1.21.1 的 GoldenTweaks）中移植，需要：
1. 用 Mixin 替代 ASM 注入点（`@Inject` cancellable 优先）；
2. 用 NeoForge 事件与网络 API（`PayloadRegistrar` 等）替代 FML 网络；
3. 部分"修复类"功能需先确认 1.21.1 对应 TC 移植版（如 Thaumcraft 6/7）是否仍存在同类问题。

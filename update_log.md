# GoldenTweaks Update Log

## 2026.10.09 - v4.7.4

### 精致存储 × 机械动力
- 新增精致存储（Sophisticated Storage）在机械动力装置（contraption）上的完整支持：为全部存储方块注册自有的挂载存储类型 `sophisticated_storage`，以离世界的「影子」`StorageBlockEntity` 承载方块 NBT，使物品、升级与设置在移动中依旧可用；服务端作为 `renderInfo` 的唯一写入方，经新增的 `S2CSsStorageRenderPacket` 推送给客户端并让装置结构缓存失效，修掉物品显示闪烁；`SsContraptionTickHandler` 还在每个容器的实时世界坐标处运行精致存储的服务端 tick（漏斗 / 拾取 / 磁铁升级）
  - [SophisticatedStorageMountedStorage.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SophisticatedStorageMountedStorage.java)、[SophisticatedStorageMountedStorageType.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SophisticatedStorageMountedStorageType.java)、[SSCreateCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SSCreateCompat.java)、[SsContraptionTickHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsContraptionTickHandler.java)、[S2CSsStorageRenderPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/S2CSsStorageRenderPacket.java)
- 装置上的容器可用原生外观的存储 / 设置界面（升级栏、设置页、限定桶 1~4 格布局）；「返回存储」改走自有的 `C2SOpenSsContraptionStoragePacket` 重新打开挂载存储菜单，不再用精致存储的世界坐标载荷
  - [SsStorageMenu.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsStorageMenu.java)、[SsStorageMenus.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsStorageMenus.java)、[SsStorageSettingsMenu.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsStorageSettingsMenu.java)、[SsStorageScreen.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/client/SsStorageScreen.java)、[SsStorageSettingsScreen.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/client/SsStorageSettingsScreen.java)、[C2SOpenSsContraptionStoragePacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SOpenSsContraptionStoragePacket.java)
- 装置上右键沿用精致存储方块语义（安装升级、等级升级、向限定桶存入并做射线命中槽位计算），左键可从限定桶取出（机械动力本身缺失的路径），走新增的 `C2SSsContraptionAttackPacket`
  - [SsContraptionInteraction.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsContraptionInteraction.java)、[SsContraptionAttackHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/client/SsContraptionAttackHandler.java)、[C2SSsContraptionAttackPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SSsContraptionAttackPacket.java)
- 装置上应用精致存储等级升级物品：定位目标方块、迁移 wrapper NBT、调整容量、替换装置方块并重建挂载存储
  - [SsTierUpgrade.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/SsTierUpgrade.java)、[TierUpgradeAccessor.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/compat/sophisticatedstorage/TierUpgradeAccessor.java)、[TierUpgradeDefinitionAccessor.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/compat/sophisticatedstorage/TierUpgradeDefinitionAccessor.java)
- 配套修复：接线盒护目镜对木质类型物品显示字面 `%s`，改用 `ItemStack.getHoverName()`；连续分离歧管 `reset()` 后 `axis`/`normalAxis` 的空指针崩服；装置通用容器菜单把非 9 倍数槽位补齐到 9 才能打开（如限定桶）；精致存储界面纳入 EMI 排除区域，避免侧栏 / 搜索 / 配方视图遮挡升级栏与设置页
  - [ChuteGoggleItemNameMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/ChuteGoggleItemNameMixin.java)、[ContinuousSeparationManifoldMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/ContinuousSeparationManifoldMixin.java)、[MountedStorageMenusMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/MountedStorageMenusMixin.java)、[MountedStorageManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/MountedStorageManagerMixin.java)、[AbstractContraptionEntityInteractionMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/AbstractContraptionEntityInteractionMixin.java)、[BackToStorageTabMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/compat/sophisticatedstorage/BackToStorageTabMixin.java)、[SsStorageEmiPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/compat/sophisticatedstorage/client/SsStorageEmiPlugin.java)

### AE2 / EMI 合成计划界面
- 让 EMI 在 AE2 的 `CraftConfirmScreen` 与 neoecoae 的 `ECOCraftConfirmScreen` 上可用：`CraftConfirmMenu` 没有槽位，EMI 原本识别不到屏幕；补上通用屏幕边界与 stack provider，恢复侧栏 / 搜索与悬停计划项的 R/U/A
  - [GTAe2CraftConfirmEmiPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2/GTAe2CraftConfirmEmiPlugin.java)、[GTAe2EmiStacks.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2/GTAe2EmiStacks.java)
- 「收藏缺失」按钮改用共用实现：把 `missingAmount > 0` 的计划项加入 EMI 收藏并重建侧栏，替代 AEQC / neoecoae 原先调用本包缺失的 JEI 内部类
  - [GTAe2CraftConfirmBookmark.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2/GTAe2CraftConfirmBookmark.java)、[BookmarkMissingButtonMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/aeqc/BookmarkMissingButtonMixin.java)、[ECOCraftConfirmScreenBookmarkMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/neoecoae/ECOCraftConfirmScreenBookmarkMixin.java)

### Polymorph 奥术配方
- 把 Thaumcraft 的 `IArcaneRecipe`（不在 `RecipeManager` 中）包装成原版 `Recipe` 交给 Polymorph 列出 / 选择，合成 id 形如 `goldentweaks:arcane/<index>`，按研究解锁与网格匹配过滤；奥术工作台与神秘能源（Thaumic Energistics）奥术合成终端都接入选择组件，选择结果经自有的 `C2SSelectArcaneRecipePacket` 回传服务端（Polymorph 自己的包无法解析非 `RecipeManager` 的 id）
  - [PolymorphArcaneCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/PolymorphArcaneCompat.java)、[GTArcaneRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/GTArcaneRecipe.java)、[GTArcaneRecipes.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/GTArcaneRecipes.java)、[PolymorphArcaneImpl.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/PolymorphArcaneImpl.java)、[GTArcaneRecipesWidget.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/client/GTArcaneRecipesWidget.java)、[GTWorkbenchPolymorphClient.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/client/GTWorkbenchPolymorphClient.java)、[GTTerminalPolymorphClient.java](src/main/java/net/fodoth/skina/goldentweaks/compat/polymorph/client/GTTerminalPolymorphClient.java)、[ArcaneWorkbenchMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ArcaneWorkbenchMenuMixin.java)、[ArcaneCraftingTerminalMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumicenergistics/ArcaneCraftingTerminalMixin.java)、[C2SSelectArcaneRecipePacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SSelectArcaneRecipePacket.java)

### 性能优化
- EMI 搜索索引第二阶段（工具提示 gram 索引）改为后台守护线程延迟构建，进世界不再卡主线程；`$` 类工具提示搜索在构建完成前暂不完整。另接入 JustEnoughCharacters 外部索引识别与 REMI 展示管线桥接，并保留跳过原生索引的路径；新增 `emiDeferSearchIndex`（默认开）
  - [GTEmiSearchEngine.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiSearchEngine.java)
- EMI 创造模式标签内容构建由一次性主线程（约 2.65s 卡顿）拆分为逐标签的后台构建，保留原内容与顺序（模组变体、神秘使法杖仍正确索引）；新增 `emiDeferCreativeGroups`（默认开）
  - [GTEmiCreativeRegroup.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiCreativeRegroup.java)、[EmiStackListMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiStackListMixin.java)
- JEI 要素扫描跳过无自定义组件的默认 `ItemStack` 的 `getUidForStack`（每个约 350µs，整轮约 22s），并去掉一次被丢弃的探测调用；新增 `jeiAspectScanSkipUid`（默认开）
  - [GTJeiAspectScan.java](src/main/java/net/fodoth/skina/goldentweaks/compat/jei/GTJeiAspectScan.java)、[ThaumcraftJeiPluginFastScanMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftJeiPluginFastScanMixin.java)、[AspectCatalogMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/scexaspect/AspectCatalogMixin.java)
- Mekanical Create 两组模拟室配方目录（约 11s）改为进入世界即后台预热，EMI 重载与之重叠；未就绪时短暂等待再回退行内计算，不会显示空配方；新增 `mekanicalcreateDeferRecipes`（默认开）
  - [GTMekanicalDeferredRecipes.java](src/main/java/net/fodoth/skina/goldentweaks/compat/mekanicalcreate/GTMekanicalDeferredRecipes.java)、[EmiReloadPrewarmMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanicalcreate/EmiReloadPrewarmMixin.java)、[MekanicalCreateDeferredRecipesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanicalcreate/MekanicalCreateDeferredRecipesMixin.java)

### 字体与渲染
- 语义换行改用 `StringSplitter` 量宽（与原生 `split` 同一条路径），不再走 `Font.width(String)`：后者会被 AcceleratedRendering 写入非线程安全的 `Object2IntOpenHashMap`，而 TMRV 会在工作线程上构造 EMI 信息配方并调用 `split`，跨线程写入导致 rehash 越界崩溃
  - [SemanticFontWrapMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/vanilla/SemanticFontWrapMixin.java)
- ModernUI：向不可变定长缓冲 map 写入抛 `UnsupportedOperationException`，改为吞掉，首次 SDF 类型创建不再报错
  - [ModernUITextRenderTypeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/modernui/ModernUITextRenderTypeMixin.java)
- RenderBlender：第一人称延迟 cosmic 层改用无深度渲染类型（修 Iris z 压缩遮挡），着色器未初始化时跳过绘制
  - [CosmicRenderLayerDepthMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/CosmicRenderLayerDepthMixin.java)
- Sable：单方块子层渲染调用 `BakedModel.getModelData`，让逐方块模型数据（如精致存储的显示物品）生效
  - [SingleBlockSubLevelModelDataMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/sable/SingleBlockSubLevelModelDataMixin.java)

### 模组兼容
- ae2cs：输入端口与侧配置 wrapper 的模拟分支未遵循 `SidePolicy.allowExtract()`，与机械动力溜槽组合会被无限取出刷物品；改为抽取恒为空 / 按策略判定
  - [AECsInputPortExtractMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2cs/AECsInputPortExtractMixin.java)、[AECsSideConfigExtractMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2cs/AECsSideConfigExtractMixin.java)
- 生物群系改造（BOP）异常方块：开启「优化错误方块」后只在原版模型方块间变化，且每 200 tick（10s）才切换一次，而非每 tick
  - [AnomalyBlockEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/biomesoplenty/AnomalyBlockEntityMixin.java)
- CoFH Core：工具提示同级列表 `removeIf` 遇到不可变列表（如 AE2 样板界面）抛 `UnsupportedOperationException` 崩渲染线程，改为 try/catch 兜底
  - [CoreClientEventsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/cofh_core/CoreClientEventsMixin.java)
- EMI：抑制常驻的红色「reload worry」警告行；`EmiTags.getRawValues`/`getIngredient` 无世界时补空值保护；`TooltipQuery#getText` 空值安全，修「钻石」等搜索触发的 NPE；特定输出槽物品（神秘要素源、scex 要素、学园都市熔炉）上移左移 1px 对齐
  - [EmiReloadWorryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiReloadWorryMixin.java)、[EmiTagsRawValuesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiTagsRawValuesMixin.java)、[TooltipQueryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/TooltipQueryMixin.java)、[SlotWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/SlotWidgetMixin.java)
- EMI Ores：`BiomeEmiStack` 在离开世界后搜索索引烘焙期间 `level == null` 抛 NPE，返回占位 id
  - [BiomeEmiStackNullLevelMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi_ores/BiomeEmiStackNullLevelMixin.java)
- FTB 任务书：强制关闭 FTB 界面加速渲染，回到原版路径，修方块光照丢失、物品溢出与编辑框 / 文字毛边
  - [FtbAccelerationDisableMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ftbquests/FtbAccelerationDisableMixin.java)
- FTB Ultimine：加入 ThaumicBases 的 `ThaumicCropBlock`，并排除 Supplementaries 亚麻上半格
  - [VanillaCropLikeHandlerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ftbultimine/VanillaCropLikeHandlerMixin.java)
- 通用机械（Mekanism）：物流管道单方块在途物品数设上限，超出即拒收，避免失控寻路 / OOM；流转物品预测集合同样设限
  - [LogisticalTransporterTransitCapMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanism/LogisticalTransporterTransitCapMixin.java)、[TransporterManagerFlowingCapMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanism/TransporterManagerFlowingCapMixin.java)
- PackagedAuto：子类型数据列表中允许空数据组件（裸 `packagedauto:package`），修 JEI UID 生成时的 NPE
  - [PackageItemSubtypeInterpreterMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/packagedauto/PackageItemSubtypeInterpreterMixin.java)
- Ponder for Kaleidoscope Cookery：把旧的三参 `RecipeItem.RecipeRecord` 构造重定向到四参构造（KC 1.5.1），修 `NoSuchMethodError`
  - [RecipeItemScenesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ponderforkc/RecipeItemScenesMixin.java)
- REMI：酿造台界面打开时隐藏 REMI 额外的 `WORKSTATION` 侧栏，避免遮挡原版 GUI
  - [RemiBrewingWorkstationSidebarMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/remi/RemiBrewingWorkstationSidebarMixin.java)
- Silent Gear：单个无法解码的材料 / 部件 / 特性改为记日志跳过，不再中断资源重载导致世界加载失败
  - [SilentGearDecodeGuardMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/silentgear/SilentGearDecodeGuardMixin.java)
- 机械动力：蒸汽动力（Steam Powered）齿轮方块改用自己的烘焙模型渲染，恢复方块形态下缺失的轮子
  - [SteampoweredCogwheelVisualMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/steampowered/SteampoweredCogwheelVisualMixin.java)
- ThaumicBases：蘑菇作物要素球改以底面中心为原点，不再贴在方块角
  - [AshroomCropOrbMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumicbases/AshroomCropOrbMixin.java)
- 神秘能源（Thaumic Energistics）：单元升级处理器实现 `IItemHandlerModifiable`，修 `ClassCastException` 掉线；元素单元工作台客户端从同步的槽 0 单元重建升级栏，使升级可见
  - [EssentiaCellUpgradeHandlerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumicenergistics/EssentiaCellUpgradeHandlerMixin.java)、[EssentiaCellWorkbenchMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumicenergistics/EssentiaCellWorkbenchMenuMixin.java)
- Thaumic Horizons：注魔罐可损坏材料（剪刀）按物品而非严格默认组件匹配，正确搭建的罐注魔能够启动
  - [LegacyInfusionIngredientsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumichorizons/LegacyInfusionIngredientsMixin.java)
- Advanced Infusion：开启 `disableAdvancedInfusionItems` 时一并剥离重复研究条目、断开父级链接并移除对应的奥术 / 注魔配方
  - [AdvancedInfusionResearchMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/advanced_infusion/AdvancedInfusionResearchMixin.java)
- Thermal：交换流体进度箭头的背景 / 填充贴图，使流体配方与普通配方显示一致
  - [DrawablesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thermal/DrawablesMixin.java)
- TMRV：同步注册 ingredient 并强制 `thaumcraft` 插件为 `NORMAL`，使其分类真正加载（原本被一个无关的 `thaumcraft:outer_lands` EMI 分类判为命名空间重复）；让 `setPlugin` 幂等，星辉重复的光流明配方不再被判为损坏而丢弃
  - [JEIPluginManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/JEIPluginManagerMixin.java)、[TmrvRecipeSetPluginMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/TmrvRecipeSetPluginMixin.java)
- LDLib2：专用服务器上抹掉 `ItemSlot` 的客户端专用方法（名字以 `xei` 开头或引用 `net/minecraft/client/*`），修 `Renderable` 的 `NoClassDefFoundError` 及连带的模组类加载失败
  - [Ldlib2ASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ldlib2/Ldlib2ASM.java)、[ItemSlotDummyMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ldlib2/ItemSlotDummyMixin.java)
- 心契誓约 / ApricityUI：LLM 连接关闭时不再发起网络请求、不进入思考核心、不再刷「思考核心处理失败」聊天，改为提示去 GoldenTweaks 设置开启（`enableMaidLlm`，默认关）；K/V 控制台热键在 GUI 打开时忽略；GUI 打开时屏蔽女仆热键（`blockMaidHotkeysInGui`，默认开）；心契誓约 / ApricityUI 日志按开关抑制（`suppressMaidLogs`，默认开）
  - [MaidLogSuppressor.java](src/main/java/net/fodoth/skina/goldentweaks/compat/maidsoulcore/MaidLogSuppressor.java)、[OpenAiCompatibleClientMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maidsoulcore/OpenAiCompatibleClientMixin.java)、[MaidSoulOwnerChatPacketMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maidsoulcore/MaidSoulOwnerChatPacketMixin.java)、[MaidSoulRuntimeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maidsoulcore/MaidSoulRuntimeMixin.java)、[AuiConsoleKeyHandlerClientInputMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maidsoulcore/AuiConsoleKeyHandlerClientInputMixin.java)、[KeyMappingMaidHotkeyScreenGuardMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maid/KeyMappingMaidHotkeyScreenGuardMixin.java)
- 东方小女仆：恢复失落的女仆时清除 `IS_LOST_MAID`、标记可驯服并重置任务；生成前修复缺失 / 损坏的 Owner UUID，避免放置时 NPE；精致存储方块实体被识别为可开的箱子
  - [AbstractStoreMaidItemUnLostMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/touhoulostmaid/AbstractStoreMaidItemUnLostMixin.java)、[AbstractStoreMaidItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/touhoulittlemaid/AbstractStoreMaidItemMixin.java)、[SophisticatedStorageChestType.java](src/main/java/net/fodoth/skina/goldentweaks/compat/touhoulittlemaid/SophisticatedStorageChestType.java)

### Thaumcraft
- 注魔拦截器重做：绑定最近的符文矩阵（y+3..10）与基座（y+1..8），从缓存的 13×13×3 源扫描取要素，一次性快进剩余基座材料、注入 20 稳定度，并支持 Thaumic Horizons 注魔罐；所有者离线或超出注魔罐 8 格范围时，用携带所有者研究 NBT 的 `FakePlayer` 启动注魔，实现无人值守 / 离线注魔
  - [GTInfusionIntercepterBlockEntity.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTInfusionIntercepterBlockEntity.java)
- 奥术工作台菜单配置化禁用原版合成、跳过空网格查询并接入 Polymorph 选择；`findMatchingArcaneRecipeEntry` 加奥术配方缓存与选择（命中缓存优先，未命中再全量扫描并回填）
  - [ArcaneWorkbenchMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ArcaneWorkbenchMenuMixin.java)、[ThaumcraftCraftingManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftCraftingManagerMixin.java)

### 联动数据
- OEI 矿物统一用 `script/oei/ore_unification.py` 重生成（224 条，24 种金属 × 矿 / 深层矿 / 粗矿 / 粗矿块 / 锭 / 粒 / 粉 / 板 / 杆 / 齿轮 / 储块 + 萤石），规范提供方默认 `alltheores`，并对钒、铅 / 锡 / 铀 / 锇 / 钢、铜 / 铁 / 金 / 黄铜板材、钨、秘银分别改指原版 / `mekanism` / `create` / `neoecoae` / `irons_spellbooks`·`hazennstuff`；另补 38 条手写条目
  - [ore_unification.json](src/main/resources/data/oei/replacements/ore_unification.json)、[ore_unification_addition.json](src/main/resources/data/oei/replacements/ore_unification_addition.json)、[ore_unification.py](script/oei/ore_unification.py)
- 食物统一扩充到约 264 条，规范提供方以 `flavor_immersed_daily`、`croptopia`、`kaleidoscope_cookery`、`farmersdelight` 为主，盐统一到 `alltheores:salt`
  - [food_unification.json](src/main/resources/data/oei/replacements/food_unification.json)
- 新增 `script/oei/gen_food_compat.py`：扫描模组 jar 中引用统一物品的配方，生成 `_unified` 配方与 `alltheores` 缺失时的原版回退配方，并把缺失的统一产物补进相关物品标签
- 新增 `script/mekmm/sync_planting_insolator.py`：把 MekMM 种植站（`mekmm:planting`）配方与热力 `te_insolator` 双向同步，重生成约 296 个 `te_insolator` 目录（横跨约 20 个命名空间，如 spectrum / croptopia / biomesoplenty / northstar 等）
- 新增大量配方：northstar（约 251，Create 粉碎 / 熔炼 / 鼓风 / 冲洗 / 压片 / 序列组装等）、thermal（约 110，储块 / 粉尘 / 齿轮 / earth_charge·fire_charge）、silentgems（约 59）、silentgear、mekmm（约 344，种植 / 辊压 / 车削 / 回收等）、powergrid、ratatouille、thaumcraft，均带 `_unified` 变体
- 新增标签 `c:obsidian`、`c:sand`、ratatouille 堆肥标签与 Thaumcraft 作物采集标签
- 新增 `script/worldfix/remove_runaway_transporters.py`：一次性地区文件修复脚本，移除超大精致存储桶与失控的通用机械物流管道列（默认 dry-run，`--apply` 写 `.bak`）
- 新增 `script/ssh/` 免密 SSH 部署脚本

### 汉化
- 新增 Astral Sorcery 完整简体中文翻译（约 877 条：物品 / 方块 / 实体、JEI 分类、星芒宝典正文、星座与星能力、附魔与药水等级等）
  - [zh_cn.json](src/main/resources/assets/astralsorcery/lang/zh_cn.json)
- 新增 Thermal 中文补充（约 39 条：流体、实体、文案与音效字幕）与 pipez_optimizer 中文（约 18 条）
- 补充 GoldenTweaks 新增配置项的中英文文案（EMI 索引延迟、JEI 要素扫描、Mekanical Create、女仆日志 / 热键 / LLM 等）

### 依赖
- 元数据新增可选依赖：`thermal` 系列（foundation / expansion / dynamics / innovation / locomotion / cultivation）、`astralsorcery`、`pipez_optimizer`
  - [neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml)

### 其它
- 注册与门控随新增内容更新：`GoldenTweaks.java` 按模组检测注册新增内容，Mixin 插件补齐全部新 Mixin 的模组门控并接入 ldlib2 的 ASM，`goldentweaks.mixins.json` 登记新增条目（`mixins` 266 条 / `client` 159 条）
  - [GoldenTweaks.java](src/main/java/net/fodoth/skina/goldentweaks/GoldenTweaks.java)、[GoldenTweaksMixinPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/GoldenTweaksMixinPlugin.java)、[goldentweaks.mixins.json](src/main/resources/goldentweaks.mixins.json)
- 移除 fidworkblock 的示例 / 砧板 Mixin 与旧的 `ThaumcraftJeiPluginMixin`（由新的快速扫描 Mixin 取代）

## 2026.10.04 - v4.7.3

### 右键拾取
- 右键拾取改为「客户端配置 + 逐玩家同步」：拾取距离与各项许可（站立 / 潜行 / 满包 / 经验球拾取、延迟阈值，以及 Lootr 快速拾取的组数与长按间隔）改由客户端按各自配置算出，经新增的 `C2SUpdatePickupSettingsPacket` 同步给服务端，服务端只按该玩家裁剪后的设定执行；未同步过设定的玩家沿用 `PickupSettings.DEFAULT`（拾取关闭），行为与旧版默认一致。整块拾取配置从通用配置迁移到客户端配置
  - [PickupSettings.java](src/main/java/net/fodoth/skina/goldentweaks/network/PickupSettings.java)、[PickupSettingsManager.java](src/main/java/net/fodoth/skina/goldentweaks/network/PickupSettingsManager.java)、[C2SUpdatePickupSettingsPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SUpdatePickupSettingsPacket.java)、[C2SPickupItemPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPickupItemPacket.java)、[ClientClickHandler.java](src/main/java/net/fodoth/skina/goldentweaks/network/handler/ClientClickHandler.java)、[GoldenTweaksClientConfig.java](src/main/java/net/fodoth/skina/goldentweaks/config/GoldenTweaksClientConfig.java)
- 满包判定改为只统计主背包 36 格，不再把盔甲与副手算进去；非创造模式满包时把多余物品飞向玩家，创造模式满包则不再拦下
  - [ItemPickupUtil.java](src/main/java/net/fodoth/skina/goldentweaks/util/ItemPickupUtil.java)
- Lootr 快速拾取改用该玩家同步来的设定，不再读全局配置
  - [LootrQuickLootEvent.java](src/main/java/net/fodoth/skina/goldentweaks/compat/lootr/LootrQuickLootEvent.java)

### 模组兼容
- Astral Sorcery：修复降星点光柱在 Sodium / Iris / Veil 等环境下画不出像素的问题——新增用原版闪电渲染自绘的光柱叠加层（`astralsorceryFocalColumn`，默认开启），并抬高光束 alpha、把 FOV alpha 改成恒等映射、给星盘角度可见度加下限；去掉发现降星点的 ±6° 角度限制；新生成区块登记后立即激活节点；`getNodes` 返回快照修区块卸载期的 `ConcurrentModificationException` 崩服；专用服务器取消 `@OnlyIn(CLIENT)` 的创意标签构建，修 `NoSuchMethodError` 崩服
  - [GTFocalPointColumnRenderer.java](src/main/java/net/fodoth/skina/goldentweaks/compat/astralsorcery/GTFocalPointColumnRenderer.java)、[FocalBeamAlphaBoostMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/FocalBeamAlphaBoostMixin.java)、[FocalLightBeamFovAlphaMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/FocalLightBeamFovAlphaMixin.java)、[AstrolabeAngleComponentVisibilityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/AstrolabeAngleComponentVisibilityMixin.java)、[FocalPointManagerDiscoveryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/FocalPointManagerDiscoveryMixin.java)、[FocalPointManagerAddNodeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/FocalPointManagerAddNodeMixin.java)、[FocalPointSyncDataGetNodesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/FocalPointSyncDataGetNodesMixin.java)、[CommonProxyCreativeTabMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/CommonProxyCreativeTabMixin.java)
- AcademyCraft 技能/机器循环音效在音效事件尚未绑定 Holder 时 `tick` 抛异常崩溃，改为该帧整体跳过
  - [AcademySoundGuard.java](src/main/java/net/fodoth/skina/goldentweaks/compat/academy/AcademySoundGuard.java)、[AbilityLoopSoundManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/academy/AbilityLoopSoundManagerMixin.java)、[MachineLoopSoundManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/academy/MachineLoopSoundManagerMixin.java)
- AppliedFlux / ae2helpers 结果导入卡：自行实现其导入逻辑（方块与面板版、按方向配置、指数退避、预期产物跟踪、掉落处理），把升级卡并入标准升级侧栏、移除单独绘制的面板，并把 ExtendedAE+ 样板供应器升级槽各加 2 格容纳感应 / 虚拟完成 / 频道 / 结果导入卡；样板供应器界面新增翻页按钮与页码输入框，右侧按钮栏对齐到升级侧栏下方
  - [AppliedFluxCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/appflux/AppliedFluxCompat.java)、[IGTImportUpgradeHost.java](src/main/java/net/fodoth/skina/goldentweaks/compat/appflux/IGTImportUpgradeHost.java)、[BlockPatternProviderImportMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2helpersblock/BlockPatternProviderImportMixin.java)、[UpgradeSlotCompatMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/extendedaeplusslots/UpgradeSlotCompatMixin.java)、[PatternProviderMenuMergeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/appflux/PatternProviderMenuMergeMixin.java)、[PatternProviderScreenMergeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/appflux/PatternProviderScreenMergeMixin.java)、[PatternProviderScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/extendedae_plus/PatternProviderScreenMixin.java)
- Mekanism 工厂槽位不再被默认堆叠上限卡死：超频及以上等级的输入/输出槽按等级放大容量（`8 << (step-1)`），与 Mekanism Extras 行为对齐，并给槽位接口加上限倍率
  - [IGTFactorySlotLimit.java](src/main/java/net/fodoth/skina/goldentweaks/compat/mekanism/IGTFactorySlotLimit.java)、[BasicInventorySlotLimitMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/balance/mekanism/BasicInventorySlotLimitMixin.java)、[TileEntityFactorySlotLimitMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/balance/mekanism/TileEntityFactorySlotLimitMixin.java)
- FTB Quests 任务书章节图片重做：贴图路径可反查成具体物品/流体并以真实 `ItemStack` 渲染（修「方块变灰」与非等比缩放），图片可悬停、可 R/U 查配方，图片编辑界面新增「配方 id / 用途 id」两项并随存盘与网络同步；未解锁图片不绘制、解锁即时出现；「产线搭建」章节组根任务打钩后自动完成整章
  - [GTItemTextureIndex.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/client/GTItemTextureIndex.java)、[GTQuestImageItems.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/client/GTQuestImageItems.java)、[GTQuestImageHover.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/client/GTQuestImageHover.java)、[GTQuestImageRecipeBinding.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/client/GTQuestImageRecipeBinding.java)、[GTFtbQuestEmiPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/client/GTFtbQuestEmiPlugin.java)、[QuestBookRecipeKeyHandler.java](src/main/java/net/fodoth/skina/goldentweaks/event/QuestBookRecipeKeyHandler.java)、[ChapterImageRecipeBindingMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/ftbquests/ChapterImageRecipeBindingMixin.java)、[ChapterImageRecipeBindingConfigMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/ftbquests/ChapterImageRecipeBindingConfigMixin.java)、[ChapterImageButtonItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/ftbquests/ChapterImageButtonItemMixin.java)、[QuestPanelImageWidgetsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/ftbquests/QuestPanelImageWidgetsMixin.java)、[QuestDependantAutoCompleteMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/ftbquests/QuestDependantAutoCompleteMixin.java)
- applied_kjs：其服务端状态用无参 `HashMap`/`HashSet`，异步区块加载线程与服务器 tick 并发访问会抛 `ConcurrentModificationException`，改用 `ConcurrentHashMap` 与并发集合
  - [AppliedKjsASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/appliedkjs/AppliedKjsASM.java)、[AppliedKjsServerStateDummyMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/appliedkjs/AppliedKjsServerStateDummyMixin.java)
- Thermal JEI 界面适配资源包：机器 JEI 部件改用 JEI 标准绘制器（箭头 / 火焰 / 格子），配方背景换成本模组贴图（去掉灰色面板、保留格子边框），新增 17 张 Thermal 分类背景贴图
  - [GTBlankDrawable.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thermal/client/GTBlankDrawable.java)、[DrawablesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thermal/DrawablesMixin.java)、[ThermalRecipeCategoryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thermal/ThermalRecipeCategoryMixin.java)
- Traveloptics：召唤物不再伤害或锁定召唤者及其生物，无主召唤物进世界即清除，并剥离灾变命名空间的血量/攻击修正器数值缩放
  - [TravelOpticsSummonProtection.java](src/main/java/net/fodoth/skina/goldentweaks/compat/traveloptics/TravelOpticsSummonProtection.java)、[SummonedOwnerCaptureMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/traveloptics/SummonedOwnerCaptureMixin.java)
- AE2CS 熵变反应室加速卡过多会「卡住不产出」，改为把加速倍率钳制到配置上限 `balance.ae2cs.maxSpeedMultiplier`（默认 2，范围 1~16，调到 16 等于关闭限制）
  - [EntropyReactionChamberSpeedMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2cs/EntropyReactionChamberSpeedMixin.java)
- Create 盆配方在容器不足 3×3 时的 `CraftingInput` 转换错误，改用真实 `CraftingInput.of` 构建
  - [BasinRecipeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/create/BasinRecipeMixin.java)
- EMI 配方页的装饰按钮（如 wcwt「填充物品」）默认被关闭，现强制开启 `showRecipeDecorators`；并修正 wcwt 锤子按钮落到 EMI 右侧按钮列顶部而非另起一列
  - [EmiRecipeDecoratorsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiRecipeDecoratorsMixin.java)、[WcwtEmiPullButtonPositionMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/wcwt/WcwtEmiPullButtonPositionMixin.java)
- 女仆核心首次进世界的自动教程弹窗可关闭（`disableMaidSoulTutorialPopup`，默认开启该修复）；把 ApricityUI 控制台界面加入 IMBlocker 白名单，使其中文输入法可用（`fixMaidSoulAuiIme`，默认开启）
  - [NoviceTutorialAutoPopupMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/maidsoulcore/NoviceTutorialAutoPopupMixin.java)、[IMBlockerConfigMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/imblocker/IMBlockerConfigMixin.java)
- Patchouli：进度同步触发重载时注册表缺少 `minecraft:item`，`deserializeStack` 抛异常导致整本书加载失败，改为返回空气
  - [ItemStackUtilMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/patchouli/ItemStackUtilMixin.java)
- Polymorph：绕开 TMRV 的 JEI 19.x stub 缺 `IRecipeTransferListener` 导致的插件崩溃，并补回「转移配方后选中该配方」的行为
  - [PolymorphJeiPluginMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/polymorph/PolymorphJeiPluginMixin.java)、[RecipeTransferUtilMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/polymorph/RecipeTransferUtilMixin.java)
- 烟火凡人心 / fidworkblock：补注册茶壶盖物品（含模型、贴图与合成配方）；砧板方块实体读取旧存档时把不足 3 格的物品栏补足，修 `Slot 1 not in valid range` 崩溃
  - [FidAdditionalItems.java](src/main/java/net/fodoth/skina/goldentweaks/compat/flavorimmerseddaily/FidAdditionalItems.java)、[ChoppingboardBlockEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/fidworkblock/ChoppingboardBlockEntityMixin.java)

### Thaumcraft
- 研究笔记「挖空」校验改为拒绝会把目标要素拆成多个连通分量的挖空，修某些研究（如「贸易」）因笔记无解而无法完成
  - [ResearchNoteDataIsolationMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ResearchNoteDataIsolationMixin.java)
- 源质罐子的 cosmic 叠加渲染整体加兜底：渲染器内抛出的任何异常都会经 `BlockEntityRenderDispatcher` 包装成 `ReportedException` 直接崩游戏，现捕获后回到原版渲染并只记一次日志
  - [JarBlockEntityRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/JarBlockEntityRendererMixin.java)

### 联动数据
- OEI 物品统一扩充：矿物统一改用 `script/oei/ore_unification.py` 重生成（24 种金属 × 矿/深层/粗矿/锭/粒/粉/板/杆/齿轮/块等，规范提供方 `alltheores`，并对铅/锡/铀/锇/钢改指 `mekanism`、铜/铁/金/黄铜板材改指 `create`），另补 38 条手工条目（peridot/sapphire/ruby、thaumium/thauminite 块、neoecoae↔northstar 钨、以及一批 thermal 粉末与方块）；食物统一补约 200 条，规范提供方以 `flavor_immersed_daily`、`croptopia`、`kaleidoscope_cookery`、`farmersdelight` 为主，盐统一到 `alltheores:salt`
  - [ore_unification.json](src/main/resources/data/oei/replacements/ore_unification.json)、[ore_unification_addition.json](src/main/resources/data/oei/replacements/ore_unification_addition.json)、[food_unification.json](src/main/resources/data/oei/replacements/food_unification.json)、[ore_unification.py](script/oei/ore_unification.py)
- 新增热力系列（Thermal）、AppliedFlux、pipez、useless_mod 的物品要素（分别约 627 / 44 / 12 / 136 条）与 Thermal 的 23 条实体要素；新增 appflux 的 3 个 Silent Gear 材料
- 新增标签 `c:obsidian`、`c:sand`，northstar 的 2 个 Create 混合配方，pipez_optimizer 的 8 个升级回退配方与 4 个物品的汉化、贴图
- Silent Gear 材料生成器扩展（木头 90 / 岩石 143 / 纤维 10 / 宝石 123 / 金属 28 / 有机物 328），要素生成脚本新增 appflux、pipez、thermal、useless_mod 命名空间规则与 thermal 实体要素，资源位置提取新增 8 个命名空间的名称转储
  - [generate_materials.py](script/silentgear/generate_materials.py)、[generate_aspect_batch.py](script/resource_locations/generate_aspect_batch.py)

### 汉化
- 新增 Astral Sorcery 完整简体中文翻译（物品 / 方块 / 实体、JEI 分类、星芒宝典正文、星座与星能力、附魔与药水等级等）
  - [zh_cn.json](src/main/resources/assets/astralsorcery/lang/zh_cn.json)
- 新增 Thermal 中文补充（流体、实体、文案与音效字幕）与 pipez_optimizer 中文
- 补充 GoldenTweaks 新增配置项的中英文文案（星辉光柱、女仆核心、AE2CS、拾取迁移等）

### 依赖
- 元数据新增可选依赖：`thermal` 系列（foundation / expansion / dynamics / innovation / locomotion / cultivation）、`astralsorcery`、`pipez_optimizer`
  - [neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml)

### 其它
- 重写 README（中英双语）：补充设计原则、功能概览、配置、实现方式、项目结构、数据与脚本、已知限制等章节，并更新依赖与代码规模统计
- 注册与门控随新增内容更新：`GoldenTweaks.java` 按模组检测注册新增内容（烟火凡人心茶壶盖、AppliedFlux 兼容、星辉光柱渲染器），Mixin 插件补齐全部新 Mixin 的模组门控并接入 applied_kjs 的 ASM，`goldentweaks.mixins.json` 登记新增条目
  - [GoldenTweaks.java](src/main/java/net/fodoth/skina/goldentweaks/GoldenTweaks.java)、[GoldenTweaksMixinPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/GoldenTweaksMixinPlugin.java)、[goldentweaks.mixins.json](src/main/resources/goldentweaks.mixins.json)

## 2026.09.30 - v4.7.2

### EMI 搜索
- 重写 EMI 搜索内核，取代 v4.6 引入的「输入去抖 + 后台节流」方案：原版每次搜索要做全量后缀数组排序（倍增算法，大整合包下秒级），新方案把索引换成 1~3 gram 倒排表、只与字符总数成正比且不排序，查询编译成位集表达式后用位运算求集合，不再物化候选列表、也不再对全表逐项扫描；正则项排在收窄后的候选集上最后执行
  - 长度 ≤3 的 token 直接零误报命中，更长的按 posting 升序求交后 `String.contains` 校验（与 JEI 19+ 的 `BakedSubstringIndex` 同构）
  - `EmiSearch.CompiledQuery` 构造时即编译出命中位集，`test` 改为按下标查位；索引外的 stack 返回 0 交回 EMI 原判定，且刻意不取消 `search`、不改 `stacks`，避免破坏 REMI 等挂钩方
  - 常驻 daemon 线程 + 代次号协作取消取代原先按键触发的计时器，索引在求值途中被重建即放弃该次结果；构建失败记 ERROR 并回退原生搜索
  - 装了 JustEnoughCharacters 时改用它注入 EMI 的拼音 Trie 解析 token，拼音搜索行为保持不变
  - [GTEmiSearchEngine.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiSearchEngine.java)、[GTEmiSubstringIndex.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTEmiSubstringIndex.java)、[GTBitSetHolder.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/GTBitSetHolder.java)、[EmiSearchMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiSearchMixin.java)、[EmiSearchCompiledQueryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiSearchCompiledQueryMixin.java)
- 移除旧的去抖实现与配置：删除 `EmiSearchDebounce`、`EmiSearchTickHandler`、`EmiSearchWidgetMixin`，配置 `searchStartDelay`、`searchSpreadDuration` 一并删除；新增 `emiFastSearch`（默认开启）与 `emiSkipVanillaSearchIndex`（默认开启，跳过原版后缀数组的存储与排序）
  - [GoldenTweaksClientConfig.java](src/main/java/net/fodoth/skina/goldentweaks/config/GoldenTweaksClientConfig.java)
- 修复 EMI 隐藏物品时 `GearItem` 未生效的问题：EMI 独立于创造栏索引全部物品，在 `isHiddenFromRecipeViewers` 中一并隐藏 Silent Gear 的预制工具
  - [SilentGearEmiHiderMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/silentgear/SilentGearEmiHiderMixin.java)

### Thaumcraft
- 新增「傀儡简化」模式（`simplifiedGolems`，默认开启）：开启后傀儡不再寻路，原地完成收获/伐木/钓鱼——`moveToward` 直接返回「已就位」、收获的「够得着」判定恒真、伐木不再要求站立点；回家不再寻路，改由 `customServerAiStep` 在傀儡停下后被推离时瞬移回原点；搜索间隔统一为 20 tick。用于规避移植版傀儡走不到目标卡死、聚集傀儡拿物品走不回原点放不进容器的问题
  - [ThaumcraftGolemEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/ThaumcraftGolemEntityMixin.java)、[GolemHarvestGoalMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/GolemHarvestGoalMixin.java)、[GolemReturnHomeGoalMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/GolemReturnHomeGoalMixin.java)、[GolemConfigRuntimeMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/GolemConfigRuntimeMixin.java)、[GTGolemSimplify.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTGolemSimplify.java)
- 新增「禁止腐化蔓延」（`disableTaintSpread`，默认开启）：运行期拦截把整列转成腐化之地的 `setColumn`，但腐化灵气节点自身的转化需要放行，故用旁路标记区分，世界生成用的另一重载不受影响；节点生物群系转化另加三个独立开关 `disablePureNodeBiomeConversion`（默认关）、`disableSinisterNodeBiomeConversion`（默认开）、`disableTaintedNodeBiomeConversion`（默认开）
  - [ThaumcraftBiomeMutationMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftBiomeMutationMixin.java)、[AuraNodeBiomeConversionMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/AuraNodeBiomeConversionMixin.java)、[GTTaintConversionGuard.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTTaintConversionGuard.java)
- 修复收获傀儡把瓜类藤蔓当作物摧毁、反而漏掉果实的问题：瓜藤（原版 `StemBlock`/`AttachedStemBlock` 及 id 以 `_stem` 结尾者）恒不可收获，果实恒可收获（新增方块标签 `goldentweaks:melon_fruits`），神秘基础学红莲只把 `ATTACHMENT` 状态算作果实
  - [CropUtilsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/CropUtilsMixin.java)、[melon_fruits.json](src/main/resources/data/goldentweaks/tags/block/melon_fruits.json)
- 修复物品要素标签缓存在客户端每个 tick 被清空的问题：失效入口由只挂 `bindRecipeManager` 改为同时挂 `refreshRecipeManager` 与 `clearServerRuntimeCaches`，且仅在配方管理器实例真正更换时清空
  - [ItemAspectRegistryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ItemAspectRegistryMixin.java)
- 稀有种族渲染器模型去重：`TCClientSetup` 为每种实体各注册一次通用渲染器（共 29 个实例），每个实例都会烘焙 28 个 `EntityModel`（约 9 MB/实例，合计约 260 MB），现让首个实例的模型存入静态数组供后续实例共享；TAINT_SHEEP 仍走 `SafeSheepModel` 以支持 OptiFine CEM/ETF
  - [LegacyThaumcraftMobRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/LegacyThaumcraftMobRendererMixin.java)
- 修复奥术工作台 EMI 一键转移的取材槽位假设错误：原按固定菜单槽区间筛选（假定充能槽总存在），但充能槽是条件添加的——普通台背包从槽 11 起、充能台从槽 20 起，硬编码会漏取材或越界；改为按槽位归属动态筛选，并给槽位区间加边界裁剪
  - [ArcaneWorkbenchEmiRecipeHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/emi/ArcaneWorkbenchEmiRecipeHandler.java)
- 神秘基础学（Thaumic Bases）进阶炼金炉补齐 `Capabilities.ItemHandler.BLOCK`：此前只有神秘时代本体炼金炉注册了物品能力，进阶炼金炉继承了容器逻辑却没有能力，管道/漏斗无法交互；新增面行为对齐 `WorldlyContainer` 的 `IItemHandler` 视图（底=燃料、侧=输入、顶=无），并新增配置 `furnaceFuelFirst`（默认开启）把燃料槽排到前面，让煤等可燃物落入燃料槽
  - [GTThaumicBasesFurnaceCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumicbases/GTThaumicBasesFurnaceCompat.java)、[GTAlchemicalFurnaceItemHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumicbases/GTAlchemicalFurnaceItemHandler.java)

### 模组兼容
- Mekanism / Compact Machines：紧凑空间维度内没有天空与昼夜，太阳能、风力、中子活化器都无法工作；新增在紧凑空间内恒判太阳可见、亮度恒为 1，并按配置比例缩放输出，覆盖 Mekanism、MekMM、Mekanism Advanced Generators 三套机组；新增配置 `compactSpacePowerPercent`（默认 25，0~最大值）
  - [GTMekanismCompactSpace.java](src/main/java/net/fodoth/skina/goldentweaks/compat/mekanism/GTMekanismCompactSpace.java)、[TileEntitySolarGeneratorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/TileEntitySolarGeneratorMixin.java)、[SolarCheckMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/SolarCheckMixin.java)、[TileEntitySolarNeutronActivatorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/TileEntitySolarNeutronActivatorMixin.java)、[MekMMSolarCheckMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/MekMMSolarCheckMixin.java)、[MekMMLargeSolarNeutronActivatorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/MekMMLargeSolarNeutronActivatorMixin.java)、[TileEntityWindGeneratorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/TileEntityWindGeneratorMixin.java)、[SolarPanelsSolarCheckMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/SolarPanelsSolarCheckMixin.java)、[SolarPanelsSolarGeneratorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/mekanism/SolarPanelsSolarGeneratorMixin.java)
- AE2 终端搜索优化：匹配时每次都重算 `AEKey#getDisplayName().getString().toLowerCase()`（重建可翻译组件、解析翻译、拼串、小写），改为弱键缓存；只重定向 `getDisplayName()`、保留原 `contains(term)` 方法体，不破坏 JustEnoughCharacters 注入的拼音匹配。同时移除旧的终端搜索去抖补丁 `MEStorageScreenSearchMixin`
  - [GTAe2NameCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2/GTAe2NameCache.java)、[NameSearchPredicateMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2/NameSearchPredicateMixin.java)
- emiLink 磁盘缓存加固：其缓存只在格式版本变化时失效，不感知模组/物品注册表变化，增删模组后物品 id 错位，甚至解压出超大数组导致 OOM；改为客户端启动时按「全部 modId@version 排序后的 SHA-1」指纹比对，不一致或缓存超过 16 MB 即删除 `emilink/cache.bin`
  - [GTEmiLinkCacheGuard.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emilink/GTEmiLinkCacheGuard.java)、[ClientSetupEvent.java](src/main/java/net/fodoth/skina/goldentweaks/event/ClientSetupEvent.java)
- AcademyCraft 技能绕过领地保护：其技能自行构造并发布 `BlockEvent.BreakEvent`，不走原版破坏流程，可在他人领地破坏方块；新增以最低优先级复核并取消的 FTB Chunks 保护，并登记 `ftbchunks` 可选依赖；新增配置 `skillBlockClaimProtection`（默认开启）
  - [GTClaimProtection.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbchunks/GTClaimProtection.java)、[neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml)
- AcademyCraft 技能增伤：取主/副手 Silent Gear 的最高 `magic_damage`，按档位放大技能最终伤害（用 `ThreadLocal` 防递归放大）；新增配置 `magicAttackDamage`（默认开启）、`magicAttackPerStep`（默认 5.0）、`magicAttackBonusPerStep`（默认 0.10）
  - [GTAcademyMagicDamage.java](src/main/java/net/fodoth/skina/goldentweaks/compat/academy/GTAcademyMagicDamage.java)、[GearMagicAttack.java](src/main/java/net/fodoth/skina/goldentweaks/compat/academy/GearMagicAttack.java)、[AcademyDamageHelperMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/academy/AcademyDamageHelperMixin.java)
- mcwifipnp + zstdnet 端口标签重叠：zstdnet 会把端口框贴到含 `sharetolan` 的界面（含 mcwifipnp 的联机界面），与「监听端口号」输入框重叠且不随标签页切换；改为只在第一个标签页、移到设置项下方，并把端口标签绘制与 mcwifipnp 对齐。同时取消进入单人世界后聊天栏的两条 zstd 端口提示
  - [McWifiPnPZstdPortLayout.java](src/main/java/net/fodoth/skina/goldentweaks/compat/mcwifipnp/McWifiPnPZstdPortLayout.java)、[ZstdShareToLanPortLabelMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/zstdnet/ZstdShareToLanPortLabelMixin.java)、[ZstdWorldJoinHintMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/zstdnet/ZstdWorldJoinHintMixin.java)
- Silent Gear 预制工具不再出现在创造物品栏：清空 `GearItemSets#getIterator`，使（16 材料 × 35 套）预制工具不再被列出——装备应由部件合成
  - [GearItemSetsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/silentgear/GearItemSetsMixin.java)
- Alshanex Familiars API 迁移：随 familiarslib 新版把服务端 `PlayerFamiliarData` + attachment 改为 `FamiliarRoster.of(player)`，客户端选择界面改用 `ClientFamiliarData.get()`
  - [FamiliarManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/alshanex_familiars/FamiliarManagerMixin.java)、[FamiliarSelectionScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/alshanex_familiars/FamiliarSelectionScreenMixin.java)、[MultiSelectionCurioScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/alshanex_familiars/MultiSelectionCurioScreenMixin.java)
- Mekanical Create 候选配方缓存无上限：其 lambda 内层 `HashMap` 会累积数万条候选目录（堆转储约 95 MB），替换为 access-ordered、上限 128 的有界 LRU，保留记忆化收益同时限制常驻堆
  - [BoundedCandidateMap.java](src/main/java/net/fodoth/skina/goldentweaks/compat/mekanicalcreate/BoundedCandidateMap.java)、[MekanicalCreateCandidateCacheMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanicalcreate/MekanicalCreateCandidateCacheMixin.java)
- Astral Sorcery 专用服务器崩服修复：2.0.1 把 `CreativeTabItem#fillCreativeTab` 标成 `@OnlyIn(CLIENT)`，专用服务器上该方法会被 RuntimeDistCleaner 整个删除，但其 `CommonProxy#onBuildCreativeTabContents` 没有做 dist 守卫，而 `BuildCreativeModeTabContentsEvent` 在服务端也会触发（数据包重载、ComputerCraft 的 `onServerStarted`），遍历物品时抛 `NoSuchMethodError` 冲出事件导致崩服；专用服务器上取消该处理器，客户端保持原样（创意标签内容本就由客户端各自构建，服务端这份不参与同步）
  - [CommonProxyCreativeTabMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/astralsorcery/CommonProxyCreativeTabMixin.java)
- 其它单项修复：
  - AE2 All Pattern 聚合样板的两张缓存上限由 256 降为 64，限制常驻堆
    - [AeAllPatternCacheCapMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/aeallpattern/AeAllPatternCacheCapMixin.java)
  - Bakeries 发酵罐在物品统一后只认本模组全麦面粉、无法投料，改为同时接受统一后的物品
    - [FermentationTankBlockMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/bakeries/FermentationTankBlockMixin.java)
  - create_connected 运行期刷新物品可见性走 JEI API，被 TMRV（以 EMI 实现）提前锁定而抛异常刷屏，装 TMRV 时取消该刷新
    - [CreateConnectedJEIMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/createconnected/CreateConnectedJEIMixin.java)
  - Kaleidoscope 汤底重复注册会抛异常中断同步队列，改为幂等跳过并保留先注册者
    - [SoupBaseManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/kaleidoscope/SoupBaseManagerMixin.java)
  - Language Reload 在 `separateTranslations` 未初始化时 `Entity.toString` 于非客户端线程（区块生成）抛 NPE，改为捕获后返回 `unknown`
    - [EntityToStringMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/languagereload/EntityToStringMixin.java)
  - Trade Cycling 切职业按钮「闪一下即永久消失」（`visible=false` 后不再渲染、也就无法重算可见性），改为每帧由界面重算
    - [MerchantScreenTradeCyclingMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tradecycling/MerchantScreenTradeCyclingMixin.java)

### 屏蔽
- c2me 启动时的「实验性版本」警告屏改为自动关闭（在 `init` 尾部把自身 `onClose` 排到客户端线程执行）
  - [C2meExperimentalWarningScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/C2meExperimentalWarningScreenMixin.java)
- c2me OpenCL 编译时屏幕底部的「Compiling…」进度文本屏蔽：吞掉对全局 `PROGRESS_TEXT` 的写入使其保持 null（渲染早退，编译本身不受影响）
  - [C2meOpenClProgressOverlayMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/C2meOpenClProgressOverlayMixin.java)

### 窗口
- 修复启动器 `--width/--height` 被忽略的问题：NeoForge 的早期窗口提供方（如 Drippy）先按 `fml.toml` 建窗再移交，启动器尺寸不生效；改为客户端首个 tick 读取进程命令行并纠正一次窗口尺寸（非全屏时）。新增配置 `windowFollowsLauncherSize`（默认开启）
  - [ClientWindowSizeGuard.java](src/main/java/net/fodoth/skina/goldentweaks/event/ClientWindowSizeGuard.java)

### 联动数据
- 新增 52 个命名空间的 946 条物品要素与 8 个命名空间的 31 条实体要素（共 977 个 JSON），覆盖 Astral Sorcery、Spectrum、NeoECOAE、Power Grid、Flavor Immersed Daily、Supplementaries、Bakeries、Iron's Spellbooks、AcademyCraft 等
- Silent Gear 新增 3 个联动命名空间共 29 个材料（ae2lt 21、academy 5、astralsorcery 3），并按生成器重做全部材料数值：硬金属移除魔法/远程/护甲韧性等属性、附魔与充能 ×0.2、挖掘等级统一为钻石级；软金属移除非核心属性、耐久 ×6、附魔与充能 ×1.8；宝石耐久 ×0.5，另含颜色与特性调整
  - [generate_materials.py](script/silentgear/generate_materials.py)
- Evolved Mekanism 熔炼/固化配方命名空间统一：输出/输入流体由 `avaritia_integration:molten_*`、`evolvedmekanism:molten_*` 改为 `avaritia_more_items:*`，加载条件相应改为 `avaritia_more_items` + `avaritia`
- 新增标签与配方：`c:fruits/grapes`、`c:seeds`、`goldentweaks:melon_fruits`、`thaumcraft:harvest_*_crops`，并把约 38 个 thaumicbases / thaumcraftcelestial 方块加入 `minecraft:mineable/*`；新增 createpropulsion、Create 填充、ratatouille 等配方

### 汉化
- 新增 FTB Chunks 完整简体中文翻译
  - [zh_cn.json](src/main/resources/assets/ftbchunks/lang/zh_cn.json)
- 补充 GoldenTweaks 新增配置项的中英文文案，并同步 silentgear、spectral_decorations、tipsmod 的 zh_cn

### 依赖
- 更新一批编译依赖：AcademyCraft 0.0.28、Thaumcraft port.253、Thaumic Bases port.21、Thaumic Tinkerer port.64、Forbidden Magic / Tainted Magic port.33、Apotheosis 8.9.0、Spectrum 1.12.7、Supplementaries 3.9.9、NeoECOAE 21.2.0、Geckolib 4.9.3、Extended Crafting 7.0.9、Create Big Cannons 5.11.7、JAOPCA 5.0.14.23、Moonlight 3.7.0、Thaumic Horizons scex-dev.41 等，并新增 Trade Cycling、ItemPhysic Lite 等
  - [libs/README.md](libs/README.md)

### 文档与脚本
- 新增《内存问题排查报告》《帧率与卡顿问题排查报告》《AcademyCraft 技能伤害汇总》三份分析文档
- 新增方块逻辑树状图 `script/block/方块.html`，资源位置提取脚本新增 22 个模组的名称转储

### 其它
- 移除 Aeronautics 在缺少 JEI API 时的兼容绕过补丁（`AeroNeoForgeCommonEvents` 系列及 accessor），Mixin 插件中对应的门控一并删除
- `.gitignore` 新增忽略 spark 堆快照（`script/spark/`、`*.sparkheap`）

## 2026.09.22 - v4.7.1

### Thaumcraft
- 修复 JSON 坩埚配方在专用服务器上全部加载失败、并连带让引用它们的研究无法注册的问题：`GTCrucibleRecipe` 曾把「研究 key」（大写，如 `GT_SHARD_ALCHEMY`）拿去拼 `ResourceLocation` 只当日志标签用，而 `ResourceLocation` 不允许大写字符，异常直接把每个坩埚配方的加载整个抛掉。表现是 18 个坩埚配方全部 `skipped`、研究页里引用的 `goldentweaks:air_shard` 等解析失败、研究随之注册不上：客户端手册能显示但点击解锁报「已拒绝未知研究请求」，服务端 `/tc research` 也查不到，挂在同一研究下的配方全部不可用
  - 改用配方文件的真实 `location` 作为解析/日志上下文（与 `GTArcaneRecipe`、`GTInfusionRecipe`、`GTRiftRecipe` 一致）
  - [GTCrucibleRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTCrucibleRecipe.java)
- 修复奥术工作台的 EMI 一键转移取材范围错误：`getInputSources` 用的是菜单槽 `11~46`，既漏掉快捷栏（`47~55`），又把隐藏的充能槽（`11~19`）算了进去，材料放在快捷栏里的配方无法一键转移进 3x3 合成格
  - [ArcaneWorkbenchEmiRecipeHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/emi/ArcaneWorkbenchEmiRecipeHandler.java)
- 优化奥术工作台首次打开时的卡顿：结果刷新会先做一次 `RecipeManager#getRecipeFor` 的全量原版配方扫描，而打开界面时（以及每次取走成品后）合成格是空的、不可能匹配任何配方；现在空网格直接跳过这次扫描，语义不变
  - [ArcaneWorkbenchMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ArcaneWorkbenchMenuMixin.java)

## 2026.09.21 - v4.7

### 模组兼容
- 修复 OEI（One Enough Item）物品统一规则在服务端全部失效的问题：OELib 在数据包 reload 期间校验物品标签，而原版要等所有 reload listener 跑完才把 `tags/item` 绑定进注册表，于是全部 `#c:xxx` 规则被判定为 `deferred`；deferred 数据既不会重试、`getAllData()` 也读不到，表现为重复的矿物锭、配方补全异常
  - 新增原始标签回退解析器：注册表未就绪时直接读取数据包原始 `tags/item` 并展开（含嵌套标签与 `replace` 语义），注册表可用时不介入
  - [OeiRawTagResolver.java](src/main/java/net/fodoth/skina/goldentweaks/compat/oei/OeiRawTagResolver.java)
  - 新增 `ResourceManager` 捕获 Mixin，在 reload prepare 阶段保存当前数据包资源管理器供回退解析使用
  - [RawTagCaptureMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/oneenoughitem/RawTagCaptureMixin.java)
  - `Utils#isTagExists` 与 `Utils#getItemsOfTag` 在注册表查询失败时回退到原始数据包，注册表可用时行为完全不变
  - [UtilsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/oneenoughitem/UtilsMixin.java)
  - [GoldenTweaksMixinPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/GoldenTweaksMixinPlugin.java)
- 新增物品统一自检与自愈：服务端启动后检查统一规则是否真正生效（无规则则跳过、有规则但运行时映射为空则报错），必要时自动触发一次资源重载让规则重新加载
  - [OreUnificationGuard.java](src/main/java/net/fodoth/skina/goldentweaks/compat/oei/OreUnificationGuard.java)
  - 新增配置 `oei.oreUnificationGuard`（启动自检，默认开启）与 `oei.oreUnificationSelfHeal`（自愈重载，默认开启）
  - [GoldenTweaksCommonConfig.java](src/main/java/net/fodoth/skina/goldentweaks/config/GoldenTweaksCommonConfig.java)
- OEI 日志从完全静音改为转发 + 节流：DEBUG/TRACE 丢弃、ERROR/FATAL 始终保留、INFO/WARN 去重并限配额，避免为压制刷屏而丢失有价值的告警
  - [ThrottledForwardingLogger.java](src/main/java/net/fodoth/skina/goldentweaks/util/ThrottledForwardingLogger.java)
  - [OneenoughitemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/oei/OneenoughitemMixin.java)

### Thaumcraft
- 修复专用服务器上客户端要素注册表少于服务端、研究台界面要素栏为空且点击要素命中错误要素的问题：客户端 `ResourceManager` 只暴露 `assets/`，不会加载 `data/<namespace>/thaumcraft/aspects/`，而研究台菜单按注册表索引同步要素池、加成与发现，索引错位即导致两侧要素栏清空
  - 新增客户端要素注册：登录时用客户端自身数据包的 `SERVER_DATA` 视图重新加载，`TreeMap` 列举顺序与条件检查同服务端加载器完全一致，且注册时机（各模组构造完成之后）也与服务端相同
  - [GTAspectClientRegistration.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAspectClientRegistration.java)
  - [GoldenTweaks.java](src/main/java/net/fodoth/skina/goldentweaks/GoldenTweaks.java)
  - [GTAspectEntry.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTAspectEntry.java)

### 修复
- 修复 `RawTagCaptureMixin` 注入描述符不匹配导致模组加载直接崩溃的问题（`InvalidInjectionException: Invalid descriptor`）：目标方法有返回值，handler 必须接收 `CallbackInfoReturnable`
  - [RawTagCaptureMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/oneenoughitem/RawTagCaptureMixin.java)

### 发布
- 新增 CurseForge / Modrinth 上传任务与凭证加载逻辑，支持一条命令同时发布两个平台
  - [publish-platforms.gradle](gradle/publish-platforms.gradle)
- 补充 OEI 兼容相关的中英文配置文案
  - [zh_cn.json](src/main/resources/assets/goldentweaks/lang/zh_cn.json)
  - [en_us.json](src/main/resources/assets/goldentweaks/lang/en_us.json)

## 2026.09.19 - v4.6

### Thaumcraft
- 新增「饕餮节点破坏方块」平衡开关（`hungryNodeBreaksBlocks`，默认关闭）：关闭时饥饿灵气节点只吸收灵气、保留音效与粒子，不再挖掘地形
  - [AuraNodeHungryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/AuraNodeHungryMixin.java)
  - [GoldenTweaksCommonConfig.java](src/main/java/net/fodoth/skina/goldentweaks/config/GoldenTweaksCommonConfig.java)
- 新增「异界漩涡破坏方块」平衡开关（`horizonsVortexBreaksBlocks`，默认关闭）：关闭时漩涡只保留合成与视觉效果，不再吞噬地形
  - [PlanarHazardsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumichorizons/PlanarHazardsMixin.java)
- 异界漩涡的合成产物现在会自动掉落在漩涡下方一格，无需再用法杖右键取件；同时登记到 `GTVortexOutputs`，避免刚产出的成品被漩涡自身的饥饿场再次吞回
  - [VortexBlockEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumichorizons/VortexBlockEntityMixin.java)
  - [GTVortexOutputs.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTVortexOutputs.java)
- 修复灵魂筛在 CentiVis 网络返回越界值（不在 `[0, 请求量]`）时抛异常并因重入标志无法清除而永久卡死的问题：返回值改为夹取到合法区间
  - [SoulSieveBoostMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumichorizons/SoulSieveBoostMixin.java)
- 修复灵魂筛上方没有灵魂收集器（灵魂收集器 / 脑罐）时每 tick 进度预算被置零、只吃灵魂沙却永不产出的问题
  - [SoulSieveReceiverMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumichorizons/SoulSieveReceiverMixin.java)
- 收容缸 / 治愈缸内的生物预览重写：固定朝向与插值字段消除抽搐，按目标高度自适应缩放（不再固定 0.25 导致大生物戳出缸外、小生物看不见），每游戏 tick 仅推进一次动画钟以保持待机动画
  - [GTCreaturePreview.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/client/GTCreaturePreview.java)
  - [SoulJarRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumichorizons/client/SoulJarRendererMixin.java)
  - [VatCreatureRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumichorizons/client/VatCreatureRendererMixin.java)
- 新增神秘视界「奥术针筒」源质效果权重汇总文档
  - [奥术针筒效果.txt](script/thaumcraft/奥术针筒效果.txt)

### 模组兼容
- 新增 EMI 搜索节流：停止输入 `searchStartDelay`（默认 20 tick）后才开始搜索、两次提交至少间隔 `searchSpreadDuration`（默认 20 tick），搜索本体在后台 daemon 线程执行
  - [EmiSearchDebounce.java](src/main/java/net/fodoth/skina/goldentweaks/compat/emi/EmiSearchDebounce.java)
  - [EmiSearchMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiSearchMixin.java)
  - [EmiSearchWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiSearchWidgetMixin.java)
  - [EmiSearchTickHandler.java](src/main/java/net/fodoth/skina/goldentweaks/event/EmiSearchTickHandler.java)
- AE2 终端搜索与 EMI 共用同一组节流配置：`searchTriggerThreshold` 重命名为 `searchStartDelay`（默认由 10 改为 20），并新增 `searchSpreadDuration`（默认 20）
  - [GoldenTweaksClientConfig.java](src/main/java/net/fodoth/skina/goldentweaks/config/GoldenTweaksClientConfig.java)
  - [MEStorageScreenSearchMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2/MEStorageScreenSearchMixin.java)
- 修复 EMI 在没有世界时烘焙搜索索引刷屏 NPE（曾一次重载刷出上万条异常、日志涨到上百 MB）：无客户端世界时返回空 tooltip 与空标签列表
  - [ItemEmiStackTooltipMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/ItemEmiStackTooltipMixin.java)
  - [EmiTagsRawValuesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiTagsRawValuesMixin.java)
- 修复 AE2 自动样板上传在专用服务器注册网络包时因链接客户端 `Screen` 导致整包注册失败的问题（ASM）
  - [Ae2ApuASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2autopatternupload/Ae2ApuASM.java)
  - [ProvidersListS2CPacketDummyMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2autopatternupload/ProvidersListS2CPacketDummyMixin.java)
- 修复 linearbearing 在专用服务器因注册客户端监听器抛 `BootstrapMethodError` 导致整模组加载失败、服务器起不来的问题（ASM）
  - [LinearbearingASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/linearbearing/LinearbearingASM.java)
  - [LinearBearingDummyMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/linearbearing/LinearBearingDummyMixin.java)
- 新增工具类，识别 NeoForge 在专用服务器上会剥离的包/类引用，供上述两个 ASM 判断使用
  - [ClientDistRefs.java](src/main/java/net/fodoth/skina/goldentweaks/util/ClientDistRefs.java)
- 修复 AI-Improvements 的 `ModifierLayer.handle` 未做 null 检查、目标集合里有 null 时服务端崩服的问题
  - [ModifierLayerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/compat/aiimprovements/ModifierLayerMixin.java)
- Carry On 抱持生物改用与收容缸同款的渲染方式（固定姿态 + 按持物框缩放 + 推进动画钟），消除搬运时的抽搐与大小错配，并保留 EMF/ETF 兼容
  - [CarriedObjectRenderMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/carryon/client/CarriedObjectRenderMixin.java)
  - [BirdAnimationBridge.java](src/main/java/net/fodoth/skina/goldentweaks/compat/neoguanniao/client/BirdAnimationBridge.java)
- 修复观鸟手册（NeoGuanNiao）部分鸟（如八哥）没有对应动画时抱持会冻结在休息姿态的问题
  - [BirdMovementControllerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/neoguanniao/client/BirdMovementControllerMixin.java)
- 修复 FTB Quests 奖励类型注册非线程安全、并行构造扩展时抛 `ConcurrentModificationException` 导致客户端启动崩溃的问题
  - [RewardTypesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ftbquests/RewardTypesMixin.java)
- 修复 fidworkblock 教程书判重依赖不随存档持久化、每次登录重复发放的问题
  - [ExampleModMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/fidworkblock/ExampleModMixin.java)
- 修复 hazennstuff 的彩虹物品名在专用服务器读取客户端类导致崩服的问题（Meowmere、和谐吊坠、Spectrum）
  - [SpectrumItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/hazennstuff/SpectrumItemMixin.java)
  - [MeowmereItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/hazennstuff/MeowmereItemMixin.java)
  - [PendantOfHarmonyCurioMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/hazennstuff/PendantOfHarmonyCurioMixin.java)
- 修复 Krypton 自带解压实现无法处理 Velocity（Youer/Paper）服务端压缩流、配置阶段直接掉线的问题
  - [MinecraftCompressDecoderMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/krypton/MinecraftCompressDecoderMixin.java)
- 修复 lzxnonefate 在 `PlayerTickEvent.Post` 里 `instanceof LocalPlayer` 导致专用服务器每次玩家 tick 加载客户端类、玩家掉线的问题
  - [FateEventMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/lzxnonefate/FateEventMixin.java)
- 修复 oneenoughitem 客户端进服时数据同步早于注册表就绪、`isTagExists` 抛 NPE 导致同步中断的问题
  - [UtilsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/oneenoughitem/UtilsMixin.java)
- 移除 RAMization 在 `ServerTickEvent.Post` 里强制的 `System.gc()`，避免全量 GC 卡死服务端主线
  - [SmartGCSchedulerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ramization/SmartGCSchedulerMixin.java)
- 移除 Supplementaries 在主菜单/暂停界面左下角注册的设置按钮
  - [ConfigButtonMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/supplementaries/ConfigButtonMixin.java)
- 修复 WATUT 的 `watut:nbt_client` 载荷解码失败（NBT 被截断）会打断整条连接、玩家掉线的问题：给 STREAM_CODEC 包一层容错解码器
  - [WatutNbtTolerantCodec.java](src/main/java/net/fodoth/skina/goldentweaks/compat/watut/WatutNbtTolerantCodec.java)
  - [PacketNBTFromServerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/watut/PacketNBTFromServerMixin.java)
- 继续抑制 All The Compatibility 的联网请求，并把客户端 tick 的版本检查取消逻辑拆到客户端专用类，避免服务端加载客户端类
  - [ATCEventsClientMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/stop_mod_reposts/ATCEventsClientMixin.java)
  - [ATCEventsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/stop_mod_reposts/ATCEventsMixin.java)
- 修复 Aeronautics 延迟注册 JEI 兼容时，类路径上没有 JEI API 会加载 Create 的 JEI 分类类导致 modlauncher 计算栈帧失败的问题
  - [AeroNeoForgeCommonEventsLateMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/aeronautics/AeroNeoForgeCommonEventsLateMixin.java)
- 修复 Silent Gear 材料书界面逻辑留在 common 的 packet 类里、服务端加载时引用客户端类抛 `NoClassDefFoundError` 的问题
  - [MaterialBookClientHandler.java](src/main/java/net/fodoth/skina/goldentweaks/network/handler/MaterialBookClientHandler.java)
  - [S2COpenMaterialBookPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/S2COpenMaterialBookPacket.java)
- Lootr 快速拾取：掉落物改为从玩家实际点击的那个面飞出，不再固定从顶面生成
  - [LootrQuickLootEvent.java](src/main/java/net/fodoth/skina/goldentweaks/compat/lootr/LootrQuickLootEvent.java)

### 日志清理
- 新增女仆「火烧状态缓存」ERROR 的 log4j2 过滤器，按消息前缀精确屏蔽专用服务器上的无害报错
  - [TlmBurningCacheLogFilter.java](src/main/java/net/fodoth/skina/goldentweaks/compat/touhoulittlemaid/TlmBurningCacheLogFilter.java)
- 神秘时代物品要素数据引用的物品在本实例不存在时，日志由 WARN 降为 DEBUG（数据跨整合包共用，并非数据错误）
  - [GTItemAspectEntry.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTItemAspectEntry.java)
- 神秘时代配方解析未通过时由 WARN 降为 DEBUG，具体原因交由各解析器记录
  - [ThaumcraftRecipeUtil.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/ThaumcraftRecipeUtil.java)

### 汉化
- 新增神秘视界（Thaumic Horizons）完整简体中文翻译，涵盖物品、方块、法术核心、灌注与研究提示等
  - [zh_cn.json](src/main/resources/assets/thaumichorizons/lang/zh_cn.json)
- 补充新增配置项的中文文案，并将「异界裂缝」统一改称「异界漩涡」
  - [zh_cn.json](src/main/resources/assets/goldentweaks/lang/zh_cn.json)
- 同步补充英文文案（新增与重命名配置项的键及注释）
  - [en_us.json](src/main/resources/assets/goldentweaks/lang/en_us.json)

### 其它
- 修复服务端无法拾取物品的问题：把拾取距离工具从客户端专用类移到 common 工具类，服务端包校验不再连带加载客户端类
  - [ItemPickupUtil.java](src/main/java/net/fodoth/skina/goldentweaks/util/ItemPickupUtil.java)
  - [ClientClickHandler.java](src/main/java/net/fodoth/skina/goldentweaks/network/handler/ClientClickHandler.java)
  - [C2SPickupItemPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/C2SPickupItemPacket.java)
- 把配置就绪标记由客户端专用事件移到通用初始化事件，使新增的配置读取方法在双端都能正确就绪；客户端事件订阅标注为仅客户端
  - [CommonSetupEvent.java](src/main/java/net/fodoth/skina/goldentweaks/event/CommonSetupEvent.java)
  - [ClientSetupEvent.java](src/main/java/net/fodoth/skina/goldentweaks/event/ClientSetupEvent.java)
- 更新 Mixin 配置与插件：注册本次新增的全部 Mixin、补齐「模组是否加载」门控，并在 preApply 阶段接入两个新增 ASM
  - [GoldenTweaksMixinPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/GoldenTweaksMixinPlugin.java)
  - [goldentweaks.mixins.json](src/main/resources/goldentweaks.mixins.json)
- 为模组元数据新增 `thaumichorizons` 可选依赖（AFTER / BOTH）
  - [neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml)

### 依赖
- 新增 AI-Improvements、watut、linearbearing 编译依赖，并升级 neoguanniao 至 3.5.1
  - [libs/README.md](libs/README.md)

## 2026.09.16 - v4.5

### Thaumcraft
- 异界裂缝（神秘视界的平面漩涡）新增 JSON 配方 API `goldentweaks:rift_crafting`：支持物品与物品标签输入，附示例配方「凋零骷髅头颅 → 下界之星」；神秘视界自带的裂缝配方不受影响，两者不会互相拦截
  - [GTRiftRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTRiftRecipe.java)
  - [VortexBlockEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumichorizons/VortexBlockEntityMixin.java)
  - [wither_skeleton_skull_to_nether_star.json](src/main/resources/data/goldentweaks/recipe/thaumichorizons/rift_crafting/wither_skeleton_skull_to_nether_star.json)
- 新增「异界裂缝」配方查看页（JEI / EMI 双端）：同时列出神秘视界内置的裂缝链（元始珍珠、傀儡活化粉、僵尸脑、虚空种子、惰性法杖）与全部 JSON 配方；单个箭头居中，悬浮箭头显示裂缝说明，物品标签输入会展开成具体物品
  - [GTRiftDisplayRecipes.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTRiftDisplayRecipes.java)
  - [GTRiftEmiRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTRiftEmiRecipe.java)
  - [GTRiftJeiCategory.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTRiftJeiCategory.java)
- 注魔截流者支持神秘视界的改版符文矩阵：全自动绑定并启动注魔、主动搬运祭品、借入不稳定度并在结束时归还
  - [GTHorizonsVatSupport.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumichorizons/GTHorizonsVatSupport.java)
  - [GTInfusionIntercepterBlockEntity.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTInfusionIntercepterBlockEntity.java)

### 联动数据
- 补齐神秘时代附属模组的物品与实体要素（thaumicbases、thaumichorizons、thaumicenergistics、advanced_infusion 等，新增 374 个数据文件）
- 补齐新联动模组的物品要素（academy、ae2lt、aeallpattern、extendedae_plus、wands、wcwt，新增 356 个数据文件）
- 禁忌魔法（forbiddenmagic）已有条目按等量追加补齐（vitium、tenebrae、praecantatio 等），不覆盖原有数值
- 要素生成脚本新增合并模式：`python3 generate_aspect_batch.py <命名空间...>` 保留既有要素、只追加缺失项
  - [generate_aspect_batch.py](script/resource_locations/generate_aspect_batch.py)

### 其它
- 新增神秘时代数据 API 文档（GitHub wiki，中英双语）：要素、物品/实体要素、奥术合成、坩埚、注魔、研究分类与条目、裂缝合成
- 神秘视界「奥术针筒」效果汇总文档加入忽略列表

## 2026.09.15 - v4.4

### 移除
- 移除 gpubooster（GPU 批渲染 / DSA 加速）模块：`gpubooster/` 全部实现、`mixin/gpubooster/` 下 11 个 mixin、`util/math/` 与 SIMD 工具类，以及对应配置项和语言键（共 36 个文件、约 4000 行）

### Thaumcraft
- 新增「宇宙」（universes）要素（物质 + 秩序）
  - [universes.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/universes.json)
- 要素来源页面支持滚动浏览：改用 Too Many Recipe Viewers 的滚动实现（滚动条、滚动网格、九宫格贴图），修复要素列表无法滚动的问题
  - [TmrvScrollBarWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/TmrvScrollBarWidgetMixin.java)
  - [TmrvScrollGridWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/TmrvScrollGridWidgetMixin.java)
  - [TmrvRecipeScreenScrollMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/TmrvRecipeScreenScrollMixin.java)
  - [NineSliceTextureMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/tmrv/NineSliceTextureMixin.java)
- 修正要素槽位 Mixin 的 `@Unique` 声明
  - [SlotWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/SlotWidgetMixin.java)

### 模组兼容
- 移除 Flavor Immersed Daily 的日志抑制（上游已不再刷屏）
- 观鸟手册（NeoGuanNiao）：补充「鸟类摄影」词条内容，并调整相机配方数据

### 联动数据
- 同步 avaritia_more_items beta-1.0.1
  - 4 个线材物品改名，新增 49 个物品要素
  - Silent Gear 线/柄材料改用 avaritia_more_items 的 string/rod，并新增 cosmic（×4）、infinity（×2）档
  - 修正 13 个 Evolved Mekanism 熔炼/固化配方的物品命名空间
  - [generate_materials.py](script/silentgear/generate_materials.py)

### 其它
- 许可证更换为 PolyForm Noncommercial 1.0.0
- 更新模组图标与模组列表描述
- 仓库开源整理：不再随仓库分发第三方编译依赖与生成物，重写 README，补充 `libs/` 依赖清单

## 2026.09.06 - v4.3

### Thaumcraft
- 修复奥术合成配方中带数量物品的解析：现在会按实际数量展开为最多 9 个配方槽位，避免数量信息被忽略
  - [GTArcaneRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTArcaneRecipe.java)
- 修正绯红织物配方要素，将错误的熵（entropy）改为正确的混沌（perditio）
  - [bloody_fabric.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/arcane_crafting/bloody_fabric.json)

### 依赖
- 更新 Thaumic Energistics 至 2.3.13-alpha

## 2026.08.27 - v4.2

### AE2 自动样板与整合
- 新增 AE2 自动样板上传兼容：支持样板终端选择、图标同步及网络包处理
  - [AutoPatternUploadButton.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2autopatternupload/AutoPatternUploadButton.java)
  - [ProviderSelectScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ae2autopatternupload/ProviderSelectScreenMixin.java)
  - [S2CProviderIconsPacket.java](src/main/java/net/fodoth/skina/goldentweaks/network/packet/S2CProviderIconsPacket.java)
- 新增 AE2 All Pattern、AE2 Pattern Provider 和 AE2 Peat 的样板整合与 EMI 支持
  - [PeatPatternUploadCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2autopatternupload/PeatPatternUploadCompat.java)
  - [ProviderIconCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ae2autopatternupload/ProviderIconCache.java)
  - [pattern_binder.json](src/main/resources/data/aeallpattern/recipe/pattern_binder.json)

### Thaumcraft
- 扩展魔导显微与魔导手册配方页面，补充配方索引和研究页面联动
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)
  - [GTResearchRecipePages.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTResearchRecipePages.java)
- 新增矿脉生成兼容，修复相关矿石特征注册问题
  - [ThaumcraftOreFeatureMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftOreFeatureMixin.java)

### 模组兼容
- 适配 Exspectriments 0.3.0 / Spectrum 1.12.4 的界面与墨水 API 变化
  - [ExspectrimentsASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/exspectriments/ExspectrimentsASM.java)
  - [ExspScreensMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/exspectriments/ExspScreensMixin.java)
- 修复 FTB Quests、Carry On、NeoECOAE、Mekanism More Machines、Create、FancyMenu、Ponder 和 Too Many Recipe Viewers 的版本兼容问题
  - [FTBQuestsLangSplitterASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/FTBQuestsLangSplitterASM.java)
  - [CarryOnAeroCompatASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/carryon/CarryOnAeroCompatASM.java)
  - [NEExtraModelsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/neoecoae/NEExtraModelsMixin.java)
  - [MoreMachineEMIMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/mekanismextras/MoreMachineEMIMixin.java)

### 游戏体验与日志
- 新增语义字体换行支持，改善中文等宽文本显示
  - [SemanticFontWrapMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/vanilla/SemanticFontWrapMixin.java)
- 增加多个存储、配方、粒子和食物日志的已知无害错误抑制，减少启动与运行时刷屏
  - [日志抑制 Mixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/)
- 更新 NeoGuanniao、Flavor Immersed Daily、Biomes O' Plenty、AE2 All Pattern、EMI 等兼容依赖

## 2026.08.24 - v4.1

### Thaumcraft
- 新增「缸中研究者」研究：手持未完成的研究笔记右键脑罐，脑罐会消耗自身经验代为完成研究；经验不足时差额由玩家经验补充，总经验不足则不会扣除任何经验
  - [BrainJarResearchEvent.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/BrainJarResearchEvent.java)
  - [brain_jar_researcher.json](src/main/resources/data/goldentweaks/thaumcraft/research/brain_jar_researcher.json)
- 新增「魔导显微」研究：魔导透镜扫描容器或掉落物时，可同时补全容器内物品的研究记录
  - 支持 AE2 存储元件、ExtendedAE、NeoECOAE 存储矩阵、Mekanism QIO 驱动器/阵列，扫描任务按 tick 排队处理，避免大容量存储导致卡顿
  - [ThaumometerStorageScanQueue.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/ThaumometerStorageScanQueue.java)
  - [ThaumometerScanManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/ThaumometerScanManagerMixin.java)
  - [ThaumometerItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/ThaumometerItemMixin.java)
  - [thaumic_microscopy.json](src/main/resources/data/goldentweaks/thaumcraft/research/thaumic_microscopy.json)
- 研究笔记在附近研究台缺少墨水时可直接借用台内墨水，且 FakePlayer 不再触发研究请求
  - [ResearchNotesItemMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/ResearchNotesItemMixin.java)
- 魔导手册「要素知识」页面重做：每页 10 个要素并按层级排序、显示要素等级与合成组件，点击组件可跳转到对应要素页；悬浮要素时显示已扫描的要素来源物品列表，支持滚轮翻页与自动轮播（可配置）
  - [ThaumonomiconScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumonomiconScreenMixin.java)
  - [ResearchAspectPageLayoutMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ResearchAspectPageLayoutMixin.java)
- 新增研究：碎片炼金（16 种碎片坩埚配方，含陨星碎片）、原始珍珠复制、符文石板、绯红仪式、终极锭注魔
  - [研究文件](src/main/resources/data/goldentweaks/thaumcraft/research/)
  - [注魔/坩埚配方](src/main/resources/data/goldentweaks/recipe/thaumcraft/)
- 新增 17 种天域之华物品：adhaesio、aestus、ardor、favilla、fulmen、fungus、gravitas、illecebra、imperium、magnetis、orbita、profundum、reliquiae、sonus、tempus、textus、vas
  - [GTThaumcraftAdditionalItems.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftAdditionalItems.java)
- 研究 JSON 注册支持 `secondary` 字段
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)
- 修复神秘时代主世界腐化生物群系参数爆炸导致的生成异常
  - [ThaumcraftOverworldBiomesMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftOverworldBiomesMixin.java)
- 要素数据批量修正：重做 fames 数值分布、修正水晶种子/终极 Mek 系列错误要素、调整 accessory 条目，并将 aestus（浪涌）要素来源改为海潮/巨浪相关物品
  - [process_rewritten_aspects.py](script/thaumcraft/process_rewritten_aspects.py)
  - [rework_aestus_aspect.py](script/thaumcraft/rework_aestus_aspect.py)
  - [process_required_aspects.py](script/resource_locations/process_required_aspects.py)
- 汉化：aestus 统一为「浪涌」，修正若干实体/方块译名

### Thaumcraft Celestial（天象神秘学）
- 按天象学修正清单调整催化物与消耗：
  - 日辉精华：下界石英 → 炽骨立方，消耗 500 → 50
  - 月华精华：紫水晶碎片 → 望舒琼浆，消耗 500 → 50
  - 星铸合金：神秘锭 → 灵宝，三种辉光各 250 → 25
  - 主手直接右键仪器即可凝聚，无需打开界面
  - [CelestialInstrumentBlockMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraftcelestial/CelestialInstrumentBlockMixin.java)
  - [CelestialInstrumentBlockEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraftcelestial/CelestialInstrumentBlockEntityMixin.java)
- 观测获得的星灾增加量降为原来的 1/10
  - [CelestialAffinityManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraftcelestial/CelestialAffinityManagerMixin.java)
- 自动化（FakePlayer）凝聚产出改为掉落物，避免产出丢失
- 修正凝聚悬浮提示、研究文本等汉化

### 合成与配方
- 新增 Spellweave 系列锭配方：
  - Create 机械合成：绯红/寒霜/闪电 Spellweave 锭
  - Create 序列装配：远古/唤魔 Spellweave 锭（含新增中间物品）
  - Extended Crafting 组合：星界 Spellweave 锭
  - [配方文件](src/main/resources/data/goldentweaks/recipe/)
- 新增终极锭注魔配方（10 种 Spellweave 锭 + 光谱碎片），并调整 Extended Crafting 终极锭配方
  - [the_ultimate_ingot.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/the_ultimate_ingot.json)
- 新增符文石板、血腥仪式注魔配方
  - [runed_tablet.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/runed_tablet.json)
  - [crimson_rites.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/crimson_rites.json)
- 为 Spectrum 融合祭坛 4 种 Spellweave 锭配方补充 mod 加载条件
- 补充/修正大量物品要素与 Silent Gear 联动材料（新增炽骨立方材料，Silent Gems 材料归入 `compat/silentgems`）

### Bountiful
- 新增「炼金术士」「神秘学家」职业悬赏池与悬赏令，含任务/出售两套池子
  - [bounty_pools](src/main/resources/data/bountiful/bounty_pools/goldentweaks/)
  - [bounty_decrees](src/main/resources/data/bountiful/bounty_decrees/goldentweaks/)
- 重写生成脚本：支持命令行参数、Decimal 精确计价、无 linkedProfessions 职业等
  - [script/bountiful/](script/bountiful/)

### 模组兼容与修复
- 女仆现在会食用禁忌魔法奥术蛋糕
  - [ArcaneCakeMaidCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/snack_cabinet/forbiddenmagic/ArcaneCakeMaidCompat.java)
- 修复 Traveloptics 物品被 EMI 隐藏/缺失的问题
  - [EmiStackListMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/EmiStackListMixin.java)
- 修复 FTB Quests Questing Additions 章节图片配置组缺失字段导致的崩溃
  - [ChapterImageConfigGroupDummyMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ftbquests/ChapterImageConfigGroupDummyMixin.java)
- 修复 FTB Ultimine 将 Supplementaries 亚麻上半部分误判为作物的问题
  - [VanillaCropLikeHandlerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/ftbultimine/VanillaCropLikeHandlerMixin.java)
- 压制 Iris 光影管线 "Width must be greater than zero" 误报日志
  - [IrisPipelineErrorMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/IrisPipelineErrorMixin.java)
- Xaero 小地图/世界地图新增完整中文汉化，并声明可选依赖
  - [xaerominimap/lang/zh_cn.json](src/main/resources/assets/xaerominimap/lang/zh_cn.json)
  - [xaeroworldmap/lang/zh_cn.json](src/main/resources/assets/xaeroworldmap/lang/zh_cn.json)
- 标签与配方补充：C 标签（jade、oats、milks、raw_fishes、vegetables、cut_vegetable 等）、Farmer's Delight 热源/卷心菜卷配料、Flavor Immersed Daily seedtobran、Interiors 椅子染色转换、Kaleidoscope Cookery 热源、女仆零食柜半砖等

### 依赖
- 新增/更新编译依赖：ExtendedAE、FarmersDelight、Mekanism 10.7.19.85、Supplementaries、FTB Ultimine、Questing Additions、Create Optical、Create Interiors、Iron's Spellbooks 3.16.2 + Iron's Lib、ISS Magic from the East 等

## 2026.08.18 - v4.0

### Thaumcraft 要素系统
- 为整合包中的物品、方块和实体批量补充要素数据，覆盖 250 个命名空间；保留语义化分配、稀有要素来源补充及源质安瓿/灵气精华专用规则
  - [物品与方块要素](src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects/)
  - [实体要素](src/main/resources/data/goldentweaks/recipe/thaumcraft/entity_aspects/)
  - [generate_aspect_batch.py](script/resource_locations/generate_aspect_batch.py)
  - [supplement_rare_aspects.py](script/resource_locations/supplement_rare_aspects.py)
- 新增资源位置提取工具，可从生产端模组生成物品、方块和实体 ID 清单
  - [extract_resource_locations.py](script/resource_locations/extract_resource_locations.py)

### Silent Gear
- 新增 629 种整合包联动材料，覆盖金属、宝石、木材、石材、线、革织、柄、树叶和花羽等分类，包含属性、部件类型、特性、颜色及中英文名称
  - [联动材料数据](src/main/resources/data/goldentweaks/silentgear_materials/compat/)
  - [generate_materials.py](script/silentgear/generate_materials.py)
  - [材料设计文档](script/materials/)
- 新增模组材料提取工具，用于汇总并去重生产端可用材料
  - [extract_materials.py](script/materials/extract_materials.py)

### 魔导手册与配方
- JSON 研究页面新增奥术合成、坩埚、注魔和注魔附魔配方页，并兼容原有文本页
  - [GTResearchRecipePages.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTResearchRecipePages.java)
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)
- 魔导手册中的注魔与坩埚要素列表改为居中分页/滚动布局，与 JEI/EMI 页面保持一致
  - [ThaumonomiconScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumonomiconScreenMixin.java)

### RenderBlender / Iris
- 适配新版 RenderBlender API，移除已由上游实现的模型烘焙与队列逻辑，并修复宇宙渲染颜色数组、第一人称矩阵和深度状态问题
  - [RenderBlenderCosmicQueueFlushHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/fix/renderblender/RenderBlenderCosmicQueueFlushHandler.java)
  - [GTCosmicJarRenderQueue.java](src/main/java/net/fodoth/skina/goldentweaks/compat/renderblender/GTCosmicJarRenderQueue.java)
  - [CosmicRenderLayerDepthMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/CosmicRenderLayerDepthMixin.java)
- 为缺少末地闪光 uniform 的光影包提供 Iris 兼容值
  - [IrisExclusiveUniformsMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/iris/IrisExclusiveUniformsMixin.java)

### 模组兼容
- 适配 Exspectriments 0.3.0 与 Spectrum 1.12.4 的墨水 API 和界面注册变化
  - [ExspectrimentsASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/exspectriments/ExspectrimentsASM.java)
  - [ExspScreensMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/exspectriments/ExspScreensMixin.java)
- 修复 FTB Quests Lang Splitter 1.0.6 在新版 FTB Quests 下调用已移除权限方法的问题
  - [FTBQuestsLangSplitterASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/ftbquests/FTBQuestsLangSplitterASM.java)
- 修复 CarryOnAeroCompat 1.1.1 在 Carry On 2.2.6.13 下因目标调用变化而无法应用的问题
  - [CarryOnAeroCompatASM.java](src/main/java/net/fodoth/skina/goldentweaks/compat/carryon/CarryOnAeroCompatASM.java)

### 日志清理
- 压制 Thaumcraft JEI、Mekanical Create、Annuus、KubeJS、Silent Gear、女仆仓库管理器和 Yammo 的已知无害错误或刷屏日志
  - [日志压制 Mixin](src/main/java/net/fodoth/skina/goldentweaks/mixin/shut/)

## 2026.08.16 - v3.9

### Thaumcraft
- 新增宇宙（cosmic）要素及其要素图标、源质安瓿和灵气精华资源，并调整稠密（dense）与奇点（singularity）要素纹理
  - [cosmic.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/cosmic.json)
  - [GTThaumcraftAdditionalItems.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftAdditionalItems.java)
  - [dense.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/dense.json)
  - [singularity.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/singularity.json)
- 新增基于 JSON 数据包的实体要素注册，支持按实体 NBT 条件匹配
  - [GTEntityAspectEntry.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTEntityAspectEntry.java)
- JSON 注魔配方新增转换配方与注魔附魔配方支持
  - [GTInfusionRecipe.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTInfusionRecipe.java)
- 修复揭示之护目镜与魔导手册中的宇宙要素渲染和文字层级问题
  - [AlchemyGogglesOverlayMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/AlchemyGogglesOverlayMixin.java)
  - [ThaumonomiconScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumonomiconScreenMixin.java)

### RenderBlender / Iris
- 修复宇宙着色物品、源质罐子及标签在 Iris 光影下的渲染顺序、深度遮挡和第一人称手部晃动问题
  - [GTCosmicJarRenderQueue.java](src/main/java/net/fodoth/skina/goldentweaks/compat/renderblender/GTCosmicJarRenderQueue.java)
  - [CosmicRenderCallMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/CosmicRenderCallMixin.java)
  - [IrisCompatMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/IrisCompatMixin.java)
  - [JarBlockEntityRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/JarBlockEntityRendererMixin.java)
- 修复 RenderBlender 烘焙模型及 Avaritia 着色器兼容问题
  - [CosmicBakeModelMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/CosmicBakeModelMixin.java)
  - [AvaritiaShadersMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/renderblender/AvaritiaShadersMixin.java)

### Flavor Immersed Daily
- 更新至 2026.8.16，并适配新版包结构与女仆食物联动接口
  - [FidMaidCompat.java](src/main/java/net/fodoth/skina/goldentweaks/compat/snack_cabinet/flavor_immersed_daily/FidMaidCompat.java)
  - [MultiStageInteractiveBlockAccessor.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/flavorimmerseddaily/accessor/MultiStageInteractiveBlockAccessor.java)

### Registrate
- 修复注册回调因未使用检查而无法执行的问题
  - [AbstractRegistrateMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/registrate/AbstractRegistrateMixin.java)

### 依赖
- Jade 更新至 15.10.6
  - [Jade-1.21.1-NeoForge-15.10.6.jar](libs/implementation/Jade-1.21.1-NeoForge-15.10.6.jar)

## 2026.08.15 - v3.8

### Thaumcraft（TC4Tweaks 移植）
- 奥术合成缓存：以「最近命中的配方」为粒度的 LRU 缓存，避免配方数量庞大时每次合成都全量遍历，shift 批量合成收益最大，可配置缓存大小（默认 64，0 关闭）
  - [ArcaneCraftingCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/ArcaneCraftingCache.java)
  - [ThaumcraftCraftingManagerMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumcraftCraftingManagerMixin.java)
- 新增坩埚配方缓存、物品要素（object tags）缓存、研究查找缓存，降低高频查询的重复计算开销，均可独立开关
  - [CrucibleRecipeCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/CrucibleRecipeCache.java)
  - [ObjectTagsCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/ObjectTagsCache.java)
  - [ResearchCache.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/ResearchCache.java)
- 震荡波（Earth Shock）伤害模式配置：只伤害生物 / 除掉落物和经验球外全部 / 全部实体
  - [EarthShockHarmMode.java](src/main/java/net/fodoth/skina/goldentweaks/util/EarthShockHarmMode.java)
  - [ShockOrbEntityMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ShockOrbEntityMixin.java)
- 魔导手册研究标签分页：分类超过 18 个时分页显示，标签列下方提供 `«` `»` 翻页箭头
  - [ThaumonomiconScreenMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ThaumonomiconScreenMixin.java)
- 奥术工作台可禁用原版合成
  - [ArcaneWorkbenchMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/ArcaneWorkbenchMenuMixin.java)

### EMI
- 奥术工作台支持 EMI 配方转移（「+」按钮自动填充配方）
  - [GTArcaneWorkbenchEmiPlugin.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/emi/GTArcaneWorkbenchEmiPlugin.java)
  - [ArcaneWorkbenchEmiRecipeHandler.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/emi/ArcaneWorkbenchEmiRecipeHandler.java)
- 修复「要素来源」页面要素安瓿槽位框与物品错位（槽位框不动，仅物品位移）
  - [SlotWidgetMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/emi/SlotWidgetMixin.java)

### JEI/EMI 配方页面
- 坩埚炼金页要素排布重做：整块水平垂直居中，每页最多 8 个要素，超过一页每 2 秒轮播切换
  - [CrucibleRecipeCategoryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/CrucibleRecipeCategoryMixin.java)
- 注魔页调整：结果槽微调、不稳定度文字上移、要素按每行 7 个排布，超过 14 个时定时向下滚动轮播
  - [InfusionRecipeCategoryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/InfusionRecipeCategoryMixin.java)
- 「要素来源」页要素源质图标左移对齐
  - [AspectSourceRecipeCategoryMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/AspectSourceRecipeCategoryMixin.java)

### 研究系统
- 研究条目改为 JSON 数据包注册（`data/<namespace>/thaumcraft/research/`）
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)
- 新增研究分类 JSON 注册系统（`data/<namespace>/thaumcraft/categories/`），新增「金子修改」研究分类
  - [GTThaumcraftCategory.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftCategory.java)
  - [gt_modifications.json](src/main/resources/data/goldentweaks/thaumcraft/categories/gt_modifications.json)
- 注魔截流者、纯净泪水研究迁移至「金子修改」分类
  - [infusion_intercepter.json](src/main/resources/data/goldentweaks/thaumcraft/research/infusion_intercepter.json)
  - [warptheory_cleanser.json](src/main/resources/data/goldentweaks/thaumcraft/research/warptheory_cleanser.json)

### 动画图标
- `GTAspectTextureAnimator` 重命名为 `GTAnimatedIconAnimator`，动画支持扩展至魔导手册分类图标
  - [GTAnimatedIconAnimator.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAnimatedIconAnimator.java)

### 汉化
- Ali 模组完整中文汉化
  - [zh_cn.json](src/main/resources/assets/ali/lang/zh_cn.json)
- 新增配置项与研究分类汉化

### 脚本
- 新增标签翻译脚本
  - [tag_translation.py](script/translation/tag/tag_translation.py)
- 新增要素列表更新脚本
  - [update_aspect_list.py](script/thaumcraft/update_aspect_list.py)
- `update_plan.md` 移至 `script/tc4tweaks/`

## 2026.08.15 - v3.7

### Thaumcraft
- 要素注册系统扩展：新增 `tint` 与 `cosmic` 选项
  - `tint: false`：图标本身已着色，绘制时跳过要素色二次染色
  - `cosmic: true`：图标经 renderblender cosmic 着色器渲染，通过隐藏代理物品绘制，覆盖魔导手册 / JEI / 研究台等所有界面
  - [GTAspectEntry.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTAspectEntry.java)
  - 源质罐子内的 cosmic 要素以 renderblender cosmic RenderType 渲染，呈现星云效果
  - [JarBlockEntityRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/JarBlockEntityRendererMixin.java)
- 新增 35 个自定义要素：dense、singularity、destroy、dragon、dream、evil、expand、explosion、fossil、history、incantatio、laputa、lava、mixtura、mornogol、praecantaticherba、priscus、saxum、space、spelunca、substance、treasure、universes、vegetatio、waters、accessories、alfirin、anteanus、arche、atrpotentia、atrpraecantatic、atrsubstance、charta、childish、perfictus
  - [要素数据](src/main/resources/data/goldentweaks/thaumcraft/aspects/)
- 新增 171 条物品要素配方：全部原版木材系列（144）、石质方块系列（13）、中子锭系列（7）、终极系列（3）、旋风棒/风弹/风向标、附魔书
  - [配方数据](src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects/)
- 新增要素图标动画支持（竖排精灵表 .mcmeta 动画），魔导手册 / 研究台 / tooltip 通用
  - [GTAnimatedAspectTexture.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAnimatedAspectTexture.java)
  - [GTAspectTextureAnimator.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAspectTextureAnimator.java)
- 重做研究台界面：左右要素栏支持滚动（每侧 12 行可见）、cosmic 图标渲染、复合要素配方 tooltip 适配
  - [GTResearchTableScreen.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTResearchTableScreen.java)
- 新增 mask 纹理图集源
  - [blocks.json](src/main/resources/assets/minecraft/atlases/blocks.json)
- 汉化：神秘时代 zh_cn 整理、禁忌魔法七宗罪要素名与帮助文本补充、Tipsmod 修正

## 2026.08.14 - v3.6

### Thaumcraft
- 新增基于 JSON 数据包的自定义要素（源质）注册系统，支持原初/复合要素及链式依赖
  - [GTAspectEntry.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTAspectEntry.java)
- 新增两个自定义要素：终结（terminus）、飞升（ascension），带独立纹理
  - [terminus.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/terminus.json)
  - [ascension.json](src/main/resources/data/goldentweaks/thaumcraft/aspects/ascension.json)
- 为自定义要素动态注册要素安瓿与天域之华物品，并按要素颜色自动染色
  - [GTAspectPhials.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTAspectPhials.java)
  - [GTAspectPhialColors.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTAspectPhialColors.java)
  - [GTThaumcraftAdditionalItems.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftAdditionalItems.java)
- 自定义要素安瓿现可与源质罐子交互（倒入/抽取源质）
  - [JarBlockMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/JarBlockMixin.java)
- 重做研究台界面，调整槽位布局并新增交互提示
  - [GTResearchTableScreen.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTResearchTableScreen.java)
  - [ResearchTableMenuMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/ResearchTableMenuMixin.java)
  - [TCClientSetupMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/feature/thaumcraft/TCClientSetupMixin.java)
- 新增复合要素配方 tooltip，显示两个组成部分图标
  - [GTAspectRecipeTooltip.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAspectRecipeTooltip.java)
  - [GTAspectRecipeClientTooltip.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/client/GTAspectRecipeClientTooltip.java)
- 新增数据生成支持，自动生成要素物品模型与本地化
  - [GTThaumcraftDataGen.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftDataGen.java)

### Avaritia
- 为水晶矩阵锭、无限催化剂添加要素
  - [crystal_matrix_ingot.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects/crystal_matrix_ingot.json)
  - [infinity_catalyst.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/aspects/infinity_catalyst.json)

### Thaumcraft Celestial
- 新增完整中文汉化

### Farmer's Delight
- 新增亚麻切割配方（联动 Supplementaries / Silent Gear）
  - [flax.json](src/main/resources/data/farmersdelight/recipe/cutting/flax.json)

### 食物统一化
- 补充食物统一化条目

### 修复
- 调整纯净泪水注魔配方
- 调整腐化碎片坩埚配方
- 移除 Silent Gear 亚麻种子种植配方

## 2026.08.12 - v3.5

### Thaumcraft
- 新增物品：纯净泪水（Warp Theory Cleanser）
  - 食用后清除所有扭曲（永久扭曲、临时扭曲、粘性扭曲）
  - [GTCleanserItem.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTCleanserItem.java)
- 新增注魔合成配方：纯净泪水
  - [warptheory_cleanser.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/warptheory_cleanser.json)
- 新增魔导手册研究页面「纯净泪水」，前置研究：奥术浴场
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)

## 2026.08.12 - v3.4

### Thaumcraft
- 从 Thaumic Insurgence (1.7.10) 移植注魔截流者（Infusion Intercepter）至 1.21.1
  - [GTInfusionIntercepterBlock.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTInfusionIntercepterBlock.java)
  - [GTInfusionIntercepterBlockEntity.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTInfusionIntercepterBlockEntity.java)
- 注魔截流者功能：
  - 放置于符文矩阵正下方 3-10 格处，自动绑定最近的矩阵和基座
  - 每 10 秒扫描正下方方块为顶面的 13×13×3 区域，缓存所有源质容器
  - 检测基座物品变化，1 秒后自动催动注魔（无需法杖右键）
  - 源质阶段直接从缓存容器抽取要素并削减矩阵需求量
  - 物品阶段一次性消耗所有基座物品，瞬间完成注魔
  - 降低矩阵不稳定性 20 点，破坏时恢复
  - 记录放置者，玩家离线时利用 FakePlayer + 缓存的研究进度继续运作
  - 绑定矩阵/基座/源质源时播放各自不同的粒子提示
- 新增创造模式标签页「金子修改丨神秘扩展」
  - [GTThaumcraftAdditionalTabs.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftAdditionalTabs.java)
- 新增注魔合成配方：注魔截流者
  - 催化剂：炼金构材
  - 基座物品：炼金构材 ×4、奥术蒸馏器 ×4
  - 源质：魔力 64、饥饿 64、工具 64、陷阱 64、机械 64、贸易 64
  - 不稳定度：10
  - [infusion_intercepter.json](src/main/resources/data/goldentweaks/recipe/thaumcraft/infusion_matrix/infusion_intercepter.json)
- 新增魔导手册研究页面「注魔截流者」，前置研究：注魔
  - [GTThaumcraftResearch.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTThaumcraftResearch.java)

## 2026.08.12 - v3.3

### Thaumcraft
- 尝试修复腐化羊（TaintSheep）的纹理问题
  - 新增 `SafeSheepModel`，使用与 `SheepModel` 相同的骨骼层级，泛型参数为 `LivingEntity`，避免桥接方法强转
  - 新增 `LegacyThaumcraftMobRendererMixin`，使 TAINT_SHEEP 使用 SafeSheepModel，支持 OptiFine CEM / ETF / FreshAnimations 等资源包正确替换模型纹理
  - [SafeSheepModel.java](src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/SafeSheepModel.java)
  - [LegacyThaumcraftMobRendererMixin.java](src/main/java/net/fodoth/skina/goldentweaks/mixin/fix/thaumcraft/LegacyThaumcraftMobRendererMixin.java)
- 添加 ETF（Entity Texture Features）和 EMF（Entity Model Features）依赖库
- 制作 FreshAnimations 兼容的腐化实体纹理资源包（`ThaumcraftTaintFix_FA`）

### GoldenTweaks
- 更新 README

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
- 添加烟火凡人心野生芦苇叶种植配方

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

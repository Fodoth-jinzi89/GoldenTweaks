# GoldenTweaks Update Log

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

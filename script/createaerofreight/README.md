# AeroFreight Bountiful 奖励池

物理手杖兼容：Simulated 1.3.0 的 PhysicsStaffActionPacket.handle 只检查原版物理手杖，重力学 0.0.6 手杖在释放/锁定时会被服务端以字面消息 `Invalid packet` 踢出。
仅在 Simulated 与 Gravitation 都安装时，将此处双手检查扩展为同时认可 `gravitation:physics_staff`；其他物品仍执行原有拒绝逻辑。

TMRV 0.9 兼容：其 JEI 配方注册表烘焙后锁定，AeroFreight 的显示配置同步仍调用 add/hide/unhideRecipes 会抛异常并断线。
客户端仅对 TMRV 跳过这三类运行时修改，保留已有打包配方对象的参数更新；原生 JEI 路径不变。
TMRV 中新增或移除的打包配方列表需要下次配方重载更新；此修复不修改物理手杖或货箱状态同步。

普通委托介绍索引仅保留 `goldentweaks_yuanli`（圆理商会），另保留职业模板供特殊委托显式选择；旧索引备份为 `templates.toml.before-yuanli.bak`。
四位写信人依次为硝酸根、无人问津、周小胖、天临，默认等概率；独立文案在 `orders/introductions/goldentweaks_yuanli/contents/writer_1.toml` 至 `writer_4.toml`，已有修改不覆盖。
默认姓名和文案通过 `text.goldentweaks.aerofreight.introduction.general_*` 中英本地化键生成。

安装 CreateAeroFreight 时，进入世界／启动服务端会根据数据包中
`data/bountiful/bounty_decrees/goldentweaks/*.json` 的 `rewards` 引用重新生成奖励池。
当前包含 35 个职业售卖池和 1 个通用货币池；不读取 `objectives`。

- 文件：`config/CreateAeroFreight/rewards/goldentweaks-bountiful-<源池名>.toml`
- 池 ID：`goldentweaks_bountiful_<源池名>`，例如 `goldentweaks_bountiful_pilot_sell`。
  编辑器用冒号分隔池与物品，池 ID 不能含冒号。
- 抽取权重沿用 `weightMult` 的相对比例，每池统一扩大为整数，避免科学计数法；数量权重取 `(amount.min + amount.max) / 2`，不随抽取权重放大。
- AeroFreight 原生按订单规模、奖励倍率等计算实际数量，不复刻 Bountiful 的数量区间抽样。
- 特殊奖励项数采用取整、截断的正态分布 N(6, 3²)，最多 18 项；通用奖励项数不变。同一订单种子固定抽取结果，池不足时按实际数量发放。保底条目优先，显式固定奖励清单不随机删减，但同样受特殊奖励 18 项限制。
- 结算及预览时通用奖励数量权重乘 4、特殊奖励数量权重乘 0.3；抽取概率权重不变，显式 `fixed` 数量保留。原配置文件数值不改写，避免每次进入世界重复缩放。
- 特殊奖励排在通用奖励前。预览同步上限由 8 项提高至 64 项，容纳特殊奖励与全部通用奖励；每行最多 6 个图标，并按实际可用宽度每两秒轮换一页。原有更多奖励入口和图标悬浮提示保留。
  原有单次预览 4096 字节安全限制保留，复杂数据组件超限时仍可通过更多奖励入口查看。
- 移除找不到可用奖励池时的铁粒应急奖励，以及旧配置 default 池仅含铁粒时的兜底；其他职业池中明确配置的铁粒不受影响。
- 未注册的物品和零权重条目跳过；空池不生成。
- 通用配置 `feature.generateAeroFreightList`（界面：生成快递列表）默认开启。开启时每次进入世界覆盖生成文件；关闭时不生成、不改写奖励及权重文件。
- 生成后自动刷新奖励池快照，可通过 `/create_aerofreight editor` 选用。
  `reward-pool-weights.toml` 会覆盖为所有现存 `kind=pool` 奖励池，每池 `weight=1.0`，并立即生效。
- `goldentweaks-bountiful-golden_all_rews.toml` 使用 `kind=pool`，保留六种货币的特殊奖励抽取，并参与等权随机抽池。
- 另外覆盖 `general.toml`（`kind=general`），每单发放 `questshop:coin=1.0`、`goldentweaks:radiant_gold=0.4`、`goldentweaks:brilliant_gold=0.2`。
  这些是数量权重，实际数量由原模组按订单规模缩放、向上取整。其他 `kind=general` 文件会先移为不重名 `.bak`，避免单 general 限制。
- editor 载入和提交时会过滤不存在的奖励物品（包括通用奖励与任意特殊池），
  使用 `GoldenTweaks.LOGGER.warn` 记录物品 ID 和原始条目，同一条目每次运行只报告一次。
  载入过滤不改写磁盘配置；发布成功后按编辑器中的有效条目保存。
  格式错误仍交给原模组校验，过滤后全空的池仍需补充有效奖励才能发布。
- editor 打开与发布均移除 512 条限制。配置按 24000 字节分片传输，完整重组后才打开界面／执行原模组发布校验。
  客户端和服务端都需更新 GoldenTweaks；仍受实际可用内存、Java 数据结构和单条字符串长度约束。
- 开启生成时，同时替换 `orders/create_aerofreight-modifier-orders-common.toml` 的 `rewardPoolModifiers`：
  每个成功生成的职业售卖池一条，名称沿用 Bountiful 中文商会名，分数统一为 +20，绑定对应 TOML；不加入通用货币池。
  其余词条订单参数保留。职业奖励词条悬浮提示提供对应奖励类别介绍，并说明通用货币仍照常发放。
- 同时生成 `orders/introductions/goldentweaks_professions/`，在介绍索引中登记“职业商会委托”，包含 35 个职业正文。
  `factions.toml` 为每个职业独立配置店铺名称（沿用职业中文名）与四位等概率占位委托人“店员1～店员4”，ID 为 `<职业>_staff_1` 至 `_staff_4`；不会跨职业抽取。
  各职业 `contents/<职业>.toml` 可独立填写标题、正文和落款；`${commissioner}`、`${faction}` 显示选中的店员和对应店铺。
  生成目录内附 `填写说明.txt`。已有文案及店员姓名不再每次进入世界覆盖；旧 dispatcher 绑定会迁移为职业四位店员，保留原正文。
  每位店员另有 `contents/<职业>_staff_1.toml` 至 `_staff_4.toml` 独立文案，首次从职业基础文案复制，此后保留修改；共 140 份。
  新订单先选择店员，再使用该店员的独立标题、正文和落款，缺失时回退职业基础文案；附 `店员文案说明.txt`。
  写信人可以是外聘人员或代笔，不要求所属店铺与职业一致；自定义落款原样保留。
  新建词条订单时按所选职业奖励池锁定对应正文，含商会名、奖励类别、目的地、距离、货柜规格和订单编号。
  原有模板与词条悬浮介绍保留；已有订单已保存文案，不自动回写。
- 每职业预制一条特殊订单，写入 `orders/create_aerofreight-special-order-templates-common.toml`。
  仅替换 `goldentweaks_profession_` 前缀的自动条目，其他特殊订单保留。每条绑定本职业奖励池和介绍正文，
  尺寸与普通／限时类型随机，目的地自动，抽取权重 1.0，可由特殊订单券刷新；不参与自然刷新。
  使用特殊订单券时，潜行右击玩家配送站；成功刷新才消耗订单券。自定义订单券可指定某一职业模板。

JDK 21 验证（不修改项目构建配置）：

```powershell
.\gradlew.bat -I script/createaerofreight/check.init.gradle checkAeroFreightRewards build --no-configuration-cache
```

检查实际转换方法、缺失物品过滤、无效权重、原生 TOML 解析和全部源奖励条目的序列化往返。
世界启动及编辑器显示需在游戏中验证。

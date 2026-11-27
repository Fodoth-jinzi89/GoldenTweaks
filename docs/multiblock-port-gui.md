# 绔彛 GUI 涓庤涓哄紑鍙戞寚鍗?
鏈枃闈㈠悜瑕佸湪 GoldenTweaks 閲?*鏂板绔彛**鎴?*鎺ュ叆绔彛浣撶郴**鐨勫紑鍙戣€咃紙鍚?agent锛夈€?涓婁竴浠?`multiblock-host-gui.md` 璁蹭富鏈猴紝杩欎竴浠借绔彛鈥斺€斾袱鑰呮槸**瀹屽叏涓嶅悓鐨勪袱濂楁満鍒?*锛?鐓т富鏈洪偅濂楀啓娉曟妱绔彛浼氫竴鍦板潙锛屽師鍥犲湪涓嬮潰绗?1 鑺傘€?
缁撹鏉ヨ嚜鐜版湁瀹炵幇锛歚GTPortKind` / `GTPortTier` / `GTPortBlock` / `GTPortBlockEntity` /
`GTPortMenu` / `GTPortScreen` / `GTPortChannels` / `GTPortStorages`銆?
---

## 0. 涓€鍙ヨ瘽缁撹

绔彛鐨勪笁涓叧閿璁★紝鍜屼富鏈洪兘涓嶄竴鏍凤細

1. **GUI 鏄暟鎹腑蹇冪殑锛屼笉鏄柟鍧椾腑蹇冪殑**銆傚紑绐楀寘閲屼紶 `浣嶇疆 + kind + 妲戒綅鏁癭锛?   鑿滃崟鍦ㄥ鎴风**閲嶆柊瑙ｆ瀽**鏂瑰潡瀹炰綋锛屽姩鎬佹暟鍊煎叏閮ㄨ蛋 `ContainerData` 鍚屾銆?2. **灞傜骇鏄嫭绔嬬殑鏂瑰潡**锛屼笉鏄?blockstate銆?5 涓眰绾?脳 2 妯″紡 脳 N 绉?= 澶ч噺娉ㄥ唽椤癸紝
   杩欐牱鍋氭槸涓轰簡鐗╁搧鍚嶈兘甯﹀眰绾э紙`I ULV input bus`锛夈€?3. **涓诲姩浼犺緭鍙彂鐢熷湪 `mode == OUTPUT`**锛屼笖姣忎釜灞傜骇鏈夎嚜宸辩殑鑺傛媿鍜屽悶鍚愰噺銆?
---

## 1. 涓庝富鏈?GUI 鐨勬牴鏈樊寮?
| | 涓绘満 | 绔彛 |
|---|---|---|
| 寮€绐楀寘鍐呭 | 浠?BlockPos | BlockPos + kind 鏋氫妇 + 妲戒綅鏁?|
| 鑿滃崟濡備綍鎷挎柟鍧楀疄浣?| 浠?BlockPos 閲嶆柊鏌?| 鍚屾牱閲嶆煡锛屼絾**kind 浠ュ寘鍐呬负鍑?* |
| 鍔ㄦ€佹暟鍊兼潵婧?| 鐩存帴璇绘柟鍧楀疄浣?| `ContainerData`锛坄SyncedData`锛?|
| 瀹㈡埛绔槸鍚︽湁瀹炰綋 | 鏄?| 鏄紙`getUpdateTag`/`getUpdatePacket` 宸插疄鐜帮級 |
| 鑿滃崟绫诲瀷 | 姣忓彴鏈哄櫒涓€涓?| **鍏ㄩ儴绔彛鍏辩敤涓€涓?* `gt_port` |

绔彛鍏辩敤涓€涓彍鍗曠被鍨嬶紝闈犲寘閲岀殑 `kind` 鍜?`slots` 鍖哄垎銆傝繖鏄埢鎰忕殑锛?浜旂绔彛銆?5 涓眰绾с€? 涓ā寮忥紝姝ｅ父鍋氭硶瑕佹敞鍐?250 涓彍鍗曠被鍨嬶紝
鑰屽畠浠櫎浜嗗浘鏍囧拰瀹归噺涔嬪娌℃湁鍖哄埆銆?
---

## 2. 寮€绐楅摼璺?
### 2.1 鏈嶅姟绔細鍐欎笁涓€?
`GTPortBlock#useWithoutItem`锛堢 180 琛岃捣锛夛細

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

**涓轰粈涔?kind 瑕佷粠鏈嶅姟绔啓鑰屼笉鏄鎴风鑷繁璇?*锛堟簮鐮佹敞閲婂師璇濓級锛?
> 瀹㈡埛绔涓嶅彲闈狅細瀹冨彧鑳借鑷繁閭ｄ唤鏂瑰潡瀹炰綋鍓湰锛岃€岄偅浠藉壇鏈彧琚憡鐭ョ墿鍝佸爢锛?> 浠庝笉琚憡鐭ュ竷灞€銆?
杩欎竴鐐瑰緢鍏抽敭銆傚鎴风鐨勬柟鍧楀疄浣撴湁鏁版嵁锛堝洜涓哄疄鐜颁簡 `getUpdateTag`锛夛紝
浣?*瀹冪殑妲戒綅鏁般€乲ind 娲剧敓鍏崇郴涓嶄竴瀹氬畬鏁?*锛屽繀椤诲湪寮€绐楁椂鐢辨湇鍔＄鏉冨▉鍦板憡璇夊畠銆?
### 2.2 瀹㈡埛绔細鎸夊悓鏍烽『搴忚鍥?
`GTPortMenu#MENU`锛堢 43 琛岋級锛?
```java
public static final DeferredHolder<MenuType<?>, MenuType<GTPortMenu>> MENU =
        MENU_TYPES.register("gt_port", () -> IMenuTypeExtension.create(
                (id, inventory, data) -> new GTPortMenu(id, inventory,
                        data.readBlockPos(), data.readEnum(GTPortKind.class),
                        data.readVarInt())));
```

**璇诲啓椤哄簭蹇呴』涓ユ牸涓€鑷?*锛欱lockPos 鈫?enum 鈫?varInt銆俙writeEnum`/`readEnum` 鐢?`GTPortKind` 鐨?ordinal 缂栫爜锛屾墍浠?*鏋氫妇甯搁噺鐨勯『搴忎笉鑳介殢渚块噸鎺?*鈥斺€旈噸鎺掍細璁╂棫瀛樻。/鏃у寘瑙ｆ瀽鎴愬埆鐨?kind銆?
### 2.3 鑿滃崟鏋勯€?
```java
public GTPortMenu(int id, Inventory inventory, BlockPos pos, GTPortKind kind, int slots) {
    super(MENU.get(), id);
    this.port = resolve(inventory, pos);
    this.clientSide = inventory.player.level().isClientSide;
    this.kind = this.port != null ? this.port.kind() : kind;   // 鈫?瀹炰綋浼樺厛锛屽寘鍐呭€煎厹搴?    this.data = new SyncedData();
    addDataSlots(this.data);

    addSlot(new SlotItemHandler(ownHandler(this.port, true), 0, TOOL_X, TOOL_Y));
    addSlot(new SlotItemHandler(ownHandler(this.port, false), 0, FILTER_X, FILTER_Y));
    // 鈥︾帺瀹惰儗鍖?3脳9 + 蹇嵎鏍?}
```

娉ㄦ剰 `this.kind` 鐨勫彇鍊奸『搴忥細**鑳芥煡鍒板疄浣撳氨鐢ㄥ疄浣撶殑 kind**锛屾煡涓嶅埌鎵嶇敤鍖呴噷浼犳潵鐨勩€?杩欐牱鍗充究鍖呴噷鐨勫€煎拰瀹炰綋涓嶄竴鑷达紙姣斿閲嶆帓杩囨灇涓撅級锛屼篃鑳借嚜鎰堛€?
---

## 3. 鍔ㄦ€佹暟鍊硷細`SyncedData`

绔彛鏈€閲嶈鐨勪竴娈佃璁°€俙GTPortMenu` 绗?149 琛岃捣锛?
```java
private final class SyncedData implements ContainerData {
    private final int[] cache = new int[DATA_COUNT];

    @Override
    public int get(int index) {
        if (!GTPortMenu.this.clientSide) {
            return switch (index) {          // 鏈嶅姟绔細鐜扮畻
                case DATA_AMOUNT -> GTPortMenu.this.filled();
                case DATA_CAPACITY -> GTPortMenu.this.capacity();
                case DATA_RATE -> GTPortMenu.this.inputRate();
                case DATA_SINGLE_CELL -> GTPortMenu.this.isSingleCellMode() ? 1 : 0;
                case DATA_ACTIVE -> GTPortMenu.this.isActivePort() ? 1 : 0;
                default -> 0;
            };
        }
        return this.cache[index];            // 瀹㈡埛绔細璇荤紦瀛?    }

    @Override
    public void set(int index, int value) {
        this.cache[index] = value;           // 鍘熺増缃戠粶灞傚畾鏈熻皟鐢?    }
}
```

鍚屾鐨勪簲涓€硷細

| 甯搁噺 | 鍚箟 |
|---|---|
| `DATA_AMOUNT` | 宸插瓨锛堢墿鍝?鎬诲拰锛屾祦浣?mB锛岃兘閲?FE锛?|
| `DATA_CAPACITY` | 瀹归噺锛屽崟浣嶅悓涓?|
| `DATA_RATE` | 杈撳叆閫熺巼锛團E/tick锛?|
| `DATA_SINGLE_CELL` | 姣忎釜鏉＄洰鍗犱竴鏍硷紙0/1锛?|
| `DATA_ACTIVE` | 鏄惁绌块潰涓诲姩浼犺緭锛?/1锛?|

**涓轰粈涔堜笉鐩存帴璇诲鎴风瀹炰綋**锛氭簮鐮佹敞閲婅瀹㈡埛绔偅浠藉壇鏈?銆屽彧琚憡鐭ョ墿鍝佸爢鍜屽瓧娈垫枃鏈紝浠庝笉鏄祦浣撴垨鑳介噺鐨勬暟閲忋€嶃€傛墍浠ユ暟鍊煎繀椤婚潬 `ContainerData`銆?
鏂板涓€涓鏄剧ず鐨勬暟鍊硷紝鏀?`DATA_COUNT`銆佸姞甯搁噺銆佸湪 `get` 閲屽姞涓€涓?case銆?鍦ㄥ睆骞曢噷璋?`menu.syncedXxx()`銆?*涓嶈**璇曞浘鍦ㄥ鎴风璇诲疄浣撶畻銆?
---

## 4. 妲戒綅璁捐

鍙湁涓夌被鐪熷疄妲戒綅锛?*绔彛鍐呭鏈韩涓嶅崰妲戒綅**锛?
```java
public static final int TOOL_INDEX = 0;      // 宸ュ叿妲斤紙鎵虫墜涔嬬被锛?public static final int FILTER_INDEX = 1;    // 杩囨护妲?private static final int OWN_SLOTS = 2;
```

`TOOL_INDEX`/`FILTER_INDEX` 鎭掑畾鍦ㄥ墠涓や綅锛?*涓庣帺瀹惰儗鍖呮Ы鏁版棤鍏?*銆傝繖涓€鐐瑰緢閲嶈锛?`quickMoveStack` 闈犺繖涓や釜涓嬫爣鍖哄垎銆屽摢涓槸鑷繁鐨勬Ы銆嶃€?
绔彛鍐呭锛堜粨搴撻噷鐨勭墿鍝併€佺綈閲岀殑娴佷綋锛?*涓嶈繘妲戒綅绯荤粺**锛?
> A port's own contents are not slotted: the window lists them and reads them from the block
> entity on both sides, so nothing about them travels through the slot sync.

瀹冧滑鍦ㄥ睆骞曚笂鏄竴鍧?3脳3 鐨?*灞曠ず鍖?*锛坄STORAGE_CELLS = 9`锛夛紝
鎸夐〉缈婚槄绔彛瀹為檯鎸佹湁鐨勬潯鐩€傛墍浠ヤ笉瑕佽瘯鍥剧敤妲戒綅琛ㄨ揪绔彛鍐呭銆?
瀹㈡埛绔病鏈夊疄浣撴椂锛宍SlotItemHandler` 浠嶉渶瑕佷竴涓惤鑴氱偣锛?
```java
private static final ItemStackHandler CLIENT_TOOL = new ItemStackHandler(1);
private static final ItemStackHandler CLIENT_FILTER = new ItemStackHandler(1);
```

涓や釜鐙珛瀹炰緥锛岄伩鍏嶅伐鍏锋Ы鍜岃繃婊ゆЫ浜掔浉瑕嗙洊銆?
---

## 5. 灞傜骇绯荤粺

### 5.1 涓轰粈涔堟槸鐙珛鏂瑰潡

`GTPortTier` 鏈?25 涓€硷紙ULV 鈫?MAX锛夈€傛瘡涓?tier 鏄?*鐙珛鐨勬柟鍧楀拰鐗╁搧**锛屼笉鏄?blockstate锛?
> Each tier is its own block and its own item rather than a block state value. That is what
> lets the inventory name a part "I ULV input bus" instead of a generic "input bus".

鐗╁搧涓嶆惡甯?blockstate锛屾墍浠ュ眰绾ф斁杩涚姸鎬佸氨"鍦ㄦ墜涓婄湅涓嶈浜?銆?
娉ㄥ唽鍚嶈鍒欙紙`GTMultiblockBlocks#declarePorts`锛夛細

```
gt_port_<kind>_<mode>_<tier>
渚嬶細gt_port_item_input_ulv, gt_port_fluid_output_max
```

25 脳 2 脳 5 = 250 涓柟鍧楋紝鍏ㄩ儴缁戝埌**鍚屼竴涓?* `BlockEntityType`锛坄gt_port`锛夈€?
### 5.2 涓?AMI 鐨勫眰绾фˉ鎺?
`GTPortTier#ami()`锛?
```java
public MultiblockPortTier ami() {
    MultiblockPortTier[] all = MultiblockPortTier.values();
    return all[Math.min(all.length - 1, ordinal())];   // 閽冲埌 AMI 鐨勬渶楂樼骇
}
```

AMI 鑷繁鐨勬瀛愬彧鍒?IX銆傛垜浠鍑烘潵鐨?UHV 鈫?MAX **閽冲埌 IX**锛?鍥犱负瀹冧滑浣滀负缁撴瀯闆朵欢浠嶇劧鏈夋晥锛屾嫆缁濈粍瑁呮病鏈夋剰涔夈€?
**杩欐剰鍛崇潃锛氱粨鏋勬壂鎻忕湅鍒扮殑鏄?IX锛屼笉鏄?UHV銆?* 濡傛灉涓嬫父瑕佹寜灞傜骇鍋氱粨鏋勫垽瀹氾紝
蹇呴』鐭ラ亾杩欎釜閽充綅銆?
### 5.3 瀹归噺涓庡悶鍚?
鍏ㄩ儴鐢?`level()` 娲剧敓锛屼笉瑕佸彟寤鸿〃锛?
```java
itemSlots()      = level虏            // ULV=1, MAX=625
slotLimit()      = 64 脳 level
fluidTanks()     = level
tankCapacity()   = 8000 << (level-1) // 鍑犱綍澧為暱锛?000 鈥?2,048,000
chemicalTanks()  = level
chemicalTankCap()= tankCapacity/10
essentiaTanks()  = itemSlots()
essentiaTankCap()= slotLimit()       // 涓庣墿鍝佹Ы涓€鑷达細涓変釜瑕佺礌 鈮?涓変釜閿?reachInterval()  = ULV:80, LV:20, 鍏朵綑:1
reachItems()     = 1,1,4,16,64,64,64,256,1024,鈥?MAX:Integer.MAX_VALUE
reachFluid()     = 1000,1000,4000,16000,64000,鈥?
```

娉ㄦ剰 `reachFluid` **涓嶆槸** `reachItems 脳 1000`锛屾槸鍗曠嫭鐨勮〃銆?
---

## 6. 琛屼负锛氫富鍔ㄤ紶杈?
### 6.1 鍞竴鐨?tick 鍏ュ彛

`GTPortBlockEntity#serverTick()`锛堢 735 琛岋級锛?
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
        case ENERGY -> reachEnergy(target, front);          // 姣?tick锛屼笉鍙楄妭鎷嶉檺鍒?        case ESSENTIA -> { if (stepDue) reachEssentia(target, front); }
        case CHEMICAL -> { if (stepDue) reachChemicals(target, front); }
    }
}
```

瑕佺偣锛?
- **`active` 榛樿鍙湪 `mode == OUTPUT` 鏃朵负鐪?*锛?
  ```java
  this.singleCell = mode() == PortMode.INPUT;
  this.active     = mode() == PortMode.OUTPUT;
  ```

  婧愮爜瑙ｉ噴锛欼NPUT 鏄?琚満鍣ㄤ几鎵嬭繘鏉ュ彇"锛孫UTPUT 鏄?鑷繁寰€鍓嶉潰鎺?銆?  浣嗚繖涓粯璁ゅ€?*鍙互琚帺瀹跺紑鍏虫敼鎺?*锛坄toggleActive`锛夈€?
- **鍏抽棴鐨勭鍙ｄ粈涔堥兘涓嶅仛锛屽寘鎷┛闈紶杈?*銆傛敞閲婃槑纭寚鍑鸿繖鏄埢鎰忕殑锛?  鍦?tick 閲屽皧閲嶅紑鍏宠€屽湪绌块潰浼犺緭閲屼笉灏婇噸锛屼細璁?宸茬鐢?鐨勭鍙ｇ户缁惉涓滆タ銆?
- **鑳介噺涓嶅彈鑺傛媿闄愬埗**銆傚洜涓哄畠鐨勯€熺巼鏈韩灏辨槸 FE/tick锛屽啀鍙犱竴涓€掕鏃朵細閲嶅闄愰€熴€?
### 6.2 浼犺緭鏂瑰悜鐢?mode 鍐冲畾

```java
boolean input = mode() == PortMode.INPUT;
IItemHandler from = input ? theirs : ours;
IItemHandler to   = input ? ours   : theirs;
```

鍚屾牱瑙勫垯閫傜敤浜庢祦浣撱€佽绱犮€佸寲瀛﹀搧銆傛柊澧炰竴绉?kind 鏃剁収杩欎釜妯″紡鍐欍€?
### 6.3 閫氶亾

`GTPortChannels` 鏄?*绾嚱鏁拌В鏋愬櫒**锛屼笉瀛樼浜屼唤鐘舵€侊細

```java
public static final int WILDCARD = 0;

// "0" = 閫氶厤锛堝姞鍏ユ墍鏈夌粍锛?// "1,3" = 鍙姞鍏?1 鍜?3
// "0,3" 鈫?褰掍竴鍖栦负 "0"锛堥€氶厤 + 鍏蜂綋鍊?= 閫氶厤锛?// 绌?/ 鏃犳硶瑙ｆ瀽 鈫?瑙嗕负閫氶厤锛堢粷涓嶉潤榛樻帓闄わ級
public static String normalise(String raw);
public static List<Integer> effective(String raw);
public static boolean joins(@Nullable String raw, int channel);
```

**澶辫触鍗抽€氶厤**鏄埢鎰忕殑锛?
> An empty result means the text is unparseable 鈥?callers treat that as the wildcard rather than
> as "no channels", so a broken field can never silently exclude a port from everything.

娉ㄦ剰 `"1,,2"` 浼氳鍒や负闈炴硶锛堣繑鍥炵┖ 鈫?閫氶厤锛夛紝鑰屼笉鏄瀹瑰湴褰撴垚 `"1,2"`锛?
> An empty segment is a malformed list, not a missing entry: "1,,2" is a typo and treating it as
> "1,2" would hide that from the player.

### 6.4 閫氶亾閿?
`channelLocked` 鏄?*鏈哄櫒鍦ㄨ繍琛屼腑璁剧疆**鐨勶紝涓嶆槸鐜╁璁剧疆锛?
```java
public boolean isChannelLocked()
public void setChannelLocked(boolean locked)
```

閿佷綇鏃?`setText(Field.CHANNEL, ...)` 鐩存帴蹇界暐鍐欏叆锛屽睆骞曚笂璇ヨ緭鍏ユ鍙樼伆銆佷笉鍙紪杈戯細

```java
boolean locked = port != null && port.isChannelLocked();
this.channelField.setEditable(!locked);
this.channelField.setTextColor(locked ? INACTIVE_TEXT_COLOUR : TEXT_COLOUR);
```

涓绘満鍦ㄥ紑濮嬪悎鎴愭椂閿佷綇鍙備笌绔彛锛岀粨鏉熸椂瑙ｉ攣锛?*鎵撶牬涓绘満鏂瑰潡涔熷繀椤昏В閿?*
锛坄GTChemicalReactorMachineBlockEntity#onBlockRemoved()`锛夛紝
鍚﹀垯绔彛浼氭案涔呬繚鐣欎竴涓病浜烘潵瑙ｇ殑閿併€?
---

### 6.5 婧愯川绔彛锛氫笁鏉″繀椤诲悓鏃舵垚绔嬬殑閾捐矾

婧愯川鍜屽叾浠栧洓绉?kind 涓嶄竴鏍凤細瀹冪殑"鎺ュ彛"鐢?*涓や釜妯＄粍鍒嗗埆瀹氫箟**锛岃€屼袱杈圭殑鍒ゅ畾閮戒笉璧版爣鍑嗙殑
NeoForge 鑳藉姏鏌ヨ銆傚皯鎺ヤ竴鏉★紝琛ㄧ幇灏辨槸"瑕佺礌鎺ㄤ笉杩涙潵"锛岃€屼笖**娌℃湁浠讳綍鎶ラ敊**銆?
#### 6.5.1 涓夋潯閾捐矾鍚勭浠€涔?
| 閾捐矾 | 璋佸湪闂?| 瑕佹眰 |
|---|---|---|
| `EssentiaCapabilities.TRANSPORT` 鑳藉姏 | 绁炵鏃朵唬鐨勭閬撱€乣EssentiaEndpoints.transport` | 绔彛娉ㄥ唽浜?`IEssentiaTransport` |
| `IAspectContainer` 鎺ュ彛锛堝湪**鏂瑰潡瀹炰綋**涓婏級 | TE 鐨勬牱鏉夸緵搴斿櫒銆佸瓨鍌ㄦ€荤嚎 | 瀹炰綋鏈韩瀹炵幇鎺ュ彛锛屼笉鍙槸鍐呴儴鐨?storage |
| `IEssentiaTransport.simulateAddEssentia` | 瀛樺偍鎬荤嚎 / 鏍锋澘渚涘簲鍣ㄧ殑 SIMULATE 闃舵 | 杩斿洖鐪熷疄浣欓噺锛?*涓嶈兘渚濊禆鎺ュ彛榛樿瀹炵幇** |

#### 6.5.2 绗竴鍧戯細`IAspectContainer` 蹇呴』鍦ㄥ疄浣撲笂

`IAspectContainer` 瀹炵幇鍦?`GTPortEssentiaStorage` 涓婃槸涓嶅鐨勨€斺€擳E 妫€鏌ョ殑鏄?*鏂瑰潡瀹炰綋**锛?
```java
// ThEEssentiaPushTarget.thE$typeAccepts锛宎ccessSide == null 鏃?blockEntity instanceof IAspectContainer
    && TCReflection.isEssentiaStore(blockEntity)
    && container.doesContainerAccept(aspect)      // 涓夋潯鍏ㄤ腑鎵嶈
```

`ProviderEssentiaTargets.wrap` 涔熷厛鐪?`blockEntity instanceof IAspectContainer`銆?
鎵€浠?`fix.thaumcraft.PortAspectContainerMixin` 鎶婃帴鍙ｆ贩鍏?`GTPortBlockEntity`锛屼節涓柟娉曞叏閮?杞彂缁欏唴閮?storage銆?*鐢?mixin 鑰屼笉鏄洿鎺?`implements`**锛屾槸鍥犱负 `IAspectContainer` 鏉ヨ嚜
绁炵鏃朵唬锛岃€岀鍙ｅ疄浣撳湪娌℃湁璇ユā缁勭殑鐜閲屼篃蹇呴』鑳藉姞杞姐€?
#### 6.5.3 绗簩鍧戯紙鏈€瀹规槗婕忥級锛歚simulateAddEssentia` 鐨勯粯璁ゅ疄鐜版槸 `return 0`

`IEssentiaTransport` 缁欎簡涓や釜 default 鏂规硶锛屽畠浠殑瀛楄妭鐮?*鍙湁涓ゆ潯鎸囦护**锛?
```
simulateAddEssentia   0: iconst_0   1: ireturn
simulateTakeEssentia  0: iconst_0   1: ireturn
```

鑰屽瓨鍌ㄦ€荤嚎寰€鐩爣鍐欏叆鏃讹紙`EssentiaContainerStorage.insert`锛夛細

```java
if (actionable == SIMULATE) {
    if (transport == null || side == null) return 0;
    return Math.min(amount, transport.simulateAddEssentia(aspect, amount, side));
}
```

**SIMULATE 杩斿洖 0 灏辩瓑浜?鐩爣鏀朵笉浜?**锛屼簬鏄牴鏈笉浼氬彂璧风湡姝ｇ殑鍐欏叆銆備笉瑕嗗啓杩欎袱涓柟娉曪紝
浠讳綍缁?`IEssentiaTransport` 涓庣鍙ｄ氦浜掔殑瑁呯疆閮戒細璁や负绔彛鏄┖鐨勶細

- 瀛樺偍鎬荤嚎璐寸潃绔彛 鈫?瑕佺礌鍋滃湪缃戠粶閲屼笉鍔?- 鏍锋澘渚涘簲鍣ㄦ帹瑕佺礌 鈫?瑕佺礌鍗″湪渚涘簲鍣ㄩ噷锛堜互閫氱敤鍫嗘爤褰㈠紡锛?
瑕嗗啓鏃跺繀椤?*鐢ㄥ拰鐪熸鍐欏叆鐩稿悓鐨勫垽鎹?*锛坄admit()`锛氬崟鏍兼ā寮忋€佸悓瑕佺礌缁姞锛夛紝鍚﹀垯浼氬嚭鐜?"鎺㈡祴璇磋兘鏀躲€佺湡鍐欏嵈澶辫触"銆?
```java
@Override
public int simulateAddEssentia(Aspect aspect, int amount, Direction side) {
    Integer tank = admit(aspect);              // 涓?add() 鍚屼竴濂楄鍒?    if (tank == null) return 0;
    return Math.max(0, Math.min(amount, this.tankCapacity - this.amounts[tank]));
}
```

`simulateTakeEssentia` 鍚屾牱瑕佽鍐欙紝鍚﹀垯浠庣鍙ｆ娊瑕佺礌鐨勮缃紙瀛樺偍鎬荤嚎銆佸鍑烘€荤嚎锛変細璁や负绔彛绌恒€?
#### 6.5.4 绗笁鍧戯細TE 鐨勭鐐规墿灞曠偣

`IEssentiaEndpointProvider` 鏄?TE 鐨勫叕寮€ API锛屾敞鍐屽悗 `EssentiaEndpoints.insert` **浼樺厛**璧板畠锛?涓?*瀹屽叏涓嶇湅 `accessSide`**锛?
```java
// EssentiaEndpoints.insert 鐨勪紭鍏堢骇
IEssentiaEndpointProvider provider = EssentiaEndpointProviders.find(blockEntity);
if (provider != null) return provider.insert(...);   // 鈫?鍏堣蛋杩欓噷
// 鍚﹀垯鍥炶惤鍒?IEssentiaTransport.addEssentia
```

`GTPortEssentiaEndpointProvider` 瀹炵幇浜嗗畠銆傝繖鏉′笉缁忚繃 SIMULATE 鍒ゅ畾锛屾槸绗簩閬撲繚闄╋紱
浣?*鍙潬瀹冧笉澶?*鈥斺€旀牱鏉夸緵搴斿櫒鐨勭洰鏍囪В鏋愶紙`EssentiaContainerStorage`锛変粛鐒惰蛋 transport 璺緞锛?涔熷氨鏄?6.5.3 閭ｄ釜 `return 0` 鐨勫潙銆備袱鑰呴兘瑕併€?
#### 6.5.5 鎺掓煡锛氫竴涓弽鐩磋鐨勭幇璞?
濡傛灉鍦?ME 瀛愮綉缁滈噷濉炰竴涓兘瀛樿绱犵殑瀛樺偍鍏冧欢锛坅mi 鍏ㄧ被鍨嬪厓浠剁瓑锛夛紝瑕佺礌灏?姝ｅ父"浜嗭紝
鎷旀帀閭ｄ釜鍏冧欢鍙堜笉宸ヤ綔鈥斺€?*杩欎笉鏄厓浠惰В鍐充簡闂**銆?
鍏冧欢鍙槸璁╄绱犲湪缃戠粶鍐呴儴琚秷鍖栨帀锛岀粫寮€浜?寰€绔彛鎺?杩欐潯鍒ゅ畾銆傜湡姝ｇ殑鎺ㄩ€佸埌绔彛浠庢湭鎴愬姛銆?鎵€浠ユ帓鏌ユ椂涓嶈琚繖涓幇璞¤瀵硷紝鐩存帴鐪?`simulateAddEssentia` 鏄惁琚鍐欍€?
涓€涓揩閫熼獙璇侊細涓存椂鎶?`simulateAddEssentia` 鏀规垚鎵撳嵃涓€琛屾棩蹇楋紝鎴愬姛鏀跺埌瑕佺礌鏃跺簲璇ヨ兘鐪嬪埌銆?鐪嬩笉鍒板氨璇存槑鍒ゅ畾浠嶅湪杩斿洖 0銆?
#### 6.5.6 鏈哄櫒渚х殑瀵瑰簲瑕佹眰

绔彛鏀朵笅浜嗚绱狅紝鏈哄櫒杩樺緱**璁ゅ緱瀹冩槸蹇呴渶鍘熸枡**鈥斺€斿惁鍒欎細鍑虹幇"瑕佺礌涓嶅湪绔彛锛岄厤鏂圭収鏍疯窇瀹?銆?瑙?`multiblock-recipe.md` 鐨勩€屾墽琛岃涔夈€嶄竴鑺傦細`typedInputsAvailable` 蹇呴』鍦ㄩ厤鏂规悳绱㈣矾寰勯噷
涔熻璋冪敤锛屼笉鑳藉彧鍦ㄩ攣瀹氶厤鏂归偅鏉¤矾涓娿€?
---

## 7. 灞忓箷绔?
### 7.1 鏁版嵁鏉ユ簮鍒嗕笁绫?
```java
// 1. 鐩存帴璇昏彍鍗曠殑鍚屾鍊硷紙瀹㈡埛绔畨鍏級
this.menu.syncedAmount(), syncedCapacity(), syncedRate()

// 2. 璇昏彍鍗曚笂鐨勬枃鏈瓧娈碉紙鍐呴儴宸插鐞?null 鍏滃簳锛?this.menu.modeText(), channelText(), priorityText()

// 3. 闇€瑕佹柟鍧楀疄浣撶殑锛堝鎴风涔熸嬁寰楀埌锛屼絾瑕佸垽 null锛?GTPortBlockEntity port = this.menu.port();
if (port == null) return;
```

`GTPortMenu` 瀵归偅浜涙枃鏈闂櫒閮藉啓浜嗗厹搴曪細

```java
public String modeText() {
    return this.port == null ? GTPortBlockEntity.DEFAULT_TEXT : this.port.modeText();
}
```

**鏂板姞鏂囨湰瀛楁鏃朵篃瑕佺収姝ゅ啓鍏滃簳**锛屽惁鍒欏鎴风瀹炰綋杩樻病鍚屾鍒版椂浼?NPE銆?
### 7.2 缂栬緫妗嗙殑鍙戦€佹椂鏈?
杈撳叆妗嗕笉鏄瘡娆℃寜閿兘鍙戝寘锛岃€屾槸 `send(...)` 閲屾瘮瀵瑰悗鍙戦€侊細

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
        // 涓庛€屽湪閫斿€笺€嶄篃姣斾竴娆★細杈撳叆琚湇鍔＄鎷掔粷鍚庯紝妗嗛噷鐨勫€煎拰绔彛涓嶄竴鑷达紝
        // 鍙窡绔彛姣斾細鎶婄帺瀹剁殑鏇存褰撴垚銆屾病鍙樸€嶈€屼粈涔堥兘涓嶅彂銆?        String baseline = this.pendingFields.getOrDefault(field, current);
        if (baseline.equals(value)) return;
        this.pendingFields.put(field,
                field == GTPortBlockEntity.Field.PRIORITY ? displayedPriority(value) : value);
        PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));
    }
}
```

涓や釜鍧戯紝娉ㄩ噴閲岄兘鍐欎簡锛?
1. **蹇呴』涓庡湪閫斿€兼瘮杈?*銆傛湇鍔＄鍙兘鎷掔粷鍐欏叆锛堟瘮濡傞€氶亾琚攣锛夛紝
   姝ゆ椂妗嗗拰绔彛涓嶄竴鑷达紱鍙窡绔彛姣旓紝鐜╁鐨勬敼姝ｆ槸"娌″彉"锛屼簬鏄案杩滃彂涓嶅嚭鍘汇€?2. **`pendingFields` 瀛樼殑鏄樉绀哄舰寮?*锛屼笉鏄嚎涓婂舰寮忋€備紭鍏堢骇浼氭樉绀烘垚 `1,600` 杩欐牱鐨勫垎缁勶紝
   鑻ュ瓨瑁告暣鏁帮紝`echo` 姣斿鏃舵案杩滃尮閰嶄笉涓婏紝瀛楁浼氭案涔呴拤鍦?pending 鍊笺€?
### 7.3 鍥炴樉

`echo` 鍦ㄦ湇鍔＄鍊煎洖鏉ユ椂鏇存柊妗嗗唴瀹癸紝浣?*璺宠繃姝ｅ湪缂栬緫鐨勬**锛?
```java
if (this.channelField == null || this.channelField.isFocused()) return;
echo(this.modeField, Field.MODE, port.modeText());
echo(this.channelField, Field.CHANNEL, port.channelText());
```

涓嶈烦杩囩殑璇濓紝鐜╁鎵撳瓧鎵撳埌涓€鍗婁細琚洖鍖呰鐩栥€?
---

## 8. 鏂板涓€绉嶇鍙?kind

浠ユ柊澧?`GAS`锛堟煇绉嶆皵浣擄級涓轰緥銆?
### 8.1 鍔犳灇涓惧€?
`GTPortKind`锛?
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

**鍔犱笂鏂板€煎繀椤绘斁鍦ㄦ灇涓炬湯灏?*锛堝畠鏄?`writeEnum`/`readEnum` 鐨?ordinal 缂栫爜锛?涓棿鎻掑叆浼氭敼鍙樺悗缁墍鏈夊€肩殑搴忓彿锛夈€?
### 8.2 鍔犲閲?
`GTPortTier` 閲屽姞瀵瑰簲鏂规硶锛屾瘮鐓?`chemicalTanks()` / `chemicalTankCapacity()`銆?
### 8.3 鍔犺兘鍔?
`GTPortCapabilities` 閲屼负璇?kind 娉ㄥ唽鑳藉姏锛坈apability锛夛紝
`GTPortStorages` 閲屽疄鐜板叿浣撴惉杩愶紙姣旂収 `reachChemical`锛夈€?
### 8.4 鑿滃崟涓庡睆骞?
- `GTPortMenu#totalCells()` 鐨?`switch` 鍔犱竴涓?case銆?- `GTPortMenu#filled()` 鑻ヨ kind 鏄崟涓€鎬婚噺锛堝儚鑳介噺锛夛紝鍦ㄩ偅閲岀壒鍒わ紱
  鍚﹀垯璧伴粯璁ょ殑 `contents()` 姹傚拰銆?- `GTPortBlockEntity#serverTick()` 鐨?`switch` 鍔犱竴涓垎鏀€?- 濡傛柊澧炴樉绀烘暟鍊硷紝鎸夌 3 鑺傛墿 `ContainerData`銆?
### 8.5 璧勬簮

- 鍥炬爣锛歚assets/goldentweaks/textures/block/multiblock/port/input_gas.png`
- 鏂瑰潡妯″瀷 / blockstate锛?50 涓懡鍚嶈寰嬭绗?5.1 鑺?- 璇█閿細`block.goldentweaks.gt_port_gas_input_ulv` 绛夛紝闇€甯﹀眰绾т唬鐮?
---

## 9. 鎺掓煡娓呭崟

| 鐥囩姸 | 鏈€鍙兘鍘熷洜 | 妫€鏌ョ偣 |
|---|---|---|
| 寮€鐣岄潰鍗冲穿 `BufferUnderflow` | 璇诲啓椤哄簭涓嶄竴鑷?| `writeBlockPos/writeEnum/writeVarInt` 鈫?`readBlockPos/readEnum/readVarInt` |
| kind 鏄剧ず閿欎贡 | 鏋氫妇甯搁噺琚噸鎺掕繃 | `writeEnum` 鎸?ordinal 缂栫爜锛?*鍙兘鍦ㄦ湯灏捐拷鍔?* |
| 鏁板€兼案杩滄槸 0 | 灞忓箷鐩磋浜嗗疄浣撳瓧娈?| 蹇呴』璧?`menu.syncedXxx()`锛坄ContainerData`锛?|
| 绔彛涓嶄富鍔ㄦ惉涓滆タ | `active` 涓哄亣锛屾垨 `enabled` 涓哄亣 | 榛樿鍙湁 `OUTPUT` 鎵?active锛涚帺瀹跺彲鍏?|
| 鑳介噺鎼緱姣旈鏈熸參 | 璇互涓哄畠鍙楄妭鎷嶉檺鍒?| 鑳介噺**涓嶅彈** `reachInterval` 闄愬埗 |
| 閫氶亾杈撳叆鎬庝箞鏀归兘娌″弽搴?| 閫氶亾琚満鍣ㄩ攣浜?| `isChannelLocked()`锛涜緭鍏ユ搴斿彉鐏?|
| 閫氶亾鍐欎簡 `1,,2` 鍗村彉鎴愰€氶厤 | 杩欐槸**鍒绘剰**鐨?| 绌烘瑙嗕负闈炴硶锛岄潪娉曗啋閫氶厤 |
| 宸ュ叿/杩囨护妲戒覆浣?| `TOOL_INDEX`/`FILTER_INDEX` 椤哄簭琚姩杩?| 瀹冧滑蹇呴』鎭掍负 0 鍜?1 |
| 缁撴瀯鎵弿涓嶈 UHV 浠ヤ笂绔彛 | AMI 閽充綅 | `ami()` 閽冲埌 IX锛岃繖鏄鏈熻涓?|
| 鎵撶牬涓绘満鍚庣鍙ｉ€氶亾閿佹 | 娌¤蛋 `onBlockRemoved()` | 涓绘満 `onRemove` 蹇呴』閫氱煡鏈哄櫒瑙ｉ攣 |

### 蹇€熻嚜妫€

鎵撳紑绔彛鐣岄潰锛岀湅鍙充笅瑙掔殑 `cells` 璇绘暟锛?
- 鏄剧ず鐨勬槸**灞傜骇瀵瑰簲鐨勬Ы鏁?*锛圲LV=1銆丩V=4銆丮V=9鈥︼級鈫?`totalCells()` 璧扮殑鏄疄浣撳垎鏀紝閾捐矾姝ｅ父銆?- 鏄剧ず **0** 鈫?`port == null`锛屽鎴风娌¤В鏋愬埌鏂瑰潡瀹炰綋锛岄棶棰樺湪寮€绐楀寘鎴栧疄浣撳悓姝ャ€?
娉ㄦ剰 `totalCells()` 鎶ョ殑鏄?*灞傜骇妲芥暟锛屼笉鏄睍绀哄尯鐨?9 鏍?*锛?
> Not the size of the visible band: the band is a fixed 3 by 3 window that pages through
> whatever the port holds, so reporting its nine would say "9" for every tier and hide the very
> thing the readout is for.

---

## 10. 鏁版嵁鎸佷箙鍖?
`getUpdateTag` / `getUpdatePacket` 宸插疄鐜帮紝鎵€浠ュ鎴风鏈夊疄浣撳壇鏈€備絾璁颁綇锛?
- **瀹㈡埛绔壇鏈彲闈犵殑鍐呭**锛氱墿鍝佸爢銆佹枃鏈瓧娈碉紙mode/channel/priority/name锛夈€?- **涓嶅彲闈犵殑鍐呭**锛氭祦浣撻噺銆佽兘閲忓€笺€傝繖浜涘繀椤昏蛋 `ContainerData`銆?- `saveAdditional` / `loadAdditional` 瑕嗙洊浜嗗瓨妗ｈ鍐欙紱
  鏂板鐘舵€佸瓧娈佃寰椾袱杈归兘鍔狅紝涓?`loadAdditional` 閲岃涓虹己澶卞瓧娈垫彁渚涢粯璁ゅ€硷紙鏃у瓨妗ｅ吋瀹癸級銆?
---

## 11. 鍙傝€冨疄鐜扮储寮?
| 鍏虫敞鐐?| 鏂囦欢 |
|---|---|
| kind 鏋氫妇涓庡浘鏍?| `content/multiblock/GTPortKind.java` |
| 灞傜骇涓庡閲忚〃 | `content/multiblock/GTPortTier.java` |
| 鏂瑰潡 / 寮€绐?/ 鎺夎惤 | `content/multiblock/GTPortBlock.java` |
| 琛屼负 / tick / 閫氶亾閿?| `content/multiblock/GTPortBlockEntity.java` |
| 鑿滃崟 / SyncedData / 妲戒綅 | `content/multiblock/GTPortMenu.java` |
| 灞忓箷 / 杈撳叆妗?| `content/multiblock/GTPortScreen.java` |
| 閫氶亾瑙ｆ瀽 | `content/multiblock/GTPortChannels.java` |
| 鎼繍瀹炵幇 | `content/multiblock/GTPortStorages.java` |
| 鑳藉姏娉ㄥ唽 | `content/multiblock/GTPortCapabilities.java` |
| 娉ㄥ唽锛?50 鏂瑰潡锛?| `content/multiblock/GTMultiblockBlocks.java#declarePorts` |

灞忓箷涓庤彍鍗曠殑 UI 鍧愭爣鍏ㄩ儴鍙栬嚜 `gt_port_io.png`锛屾敞閲婃槑纭
銆宔very coordinate here is taken from that sheet rather than computed銆嶃€?鏀瑰竷灞€瑕佸厛鏀瑰浘锛屼笉瑕佹敼甯搁噺銆?
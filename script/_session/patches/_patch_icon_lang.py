import io
import json

# drop the now-unused machine texture reference
p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java'
s = io.open(p, encoding='utf-8').read()
s = s.replace("""    /** The machine's sheet, which is the only one carrying the run button's icons. */
    private static final ResourceLocation MACHINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GoldenTweaks.MODID,
                    "textures/gui/gt_multiblock_machine.png");
""", "")
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('screen cleaned')

zh = {'gui.goldentweaks.chemical_reactor.status.processing_channel': '运行中：%s / %s　频道：%s'}
en = {'gui.goldentweaks.chemical_reactor.status.processing_channel': 'Running: %s / %s  Channel: %s'}

for path, extra in [
    (r'src/main/resources/assets/goldentweaks/lang/zh_cn.json', zh),
    (r'src/main/resources/assets/goldentweaks/lang/en_us.json', en),
]:
    data = json.load(io.open(path, encoding='utf-8'))
    data.update(extra)
    with io.open(path, 'w', encoding='utf-8', newline='\n') as h:
        json.dump(data, h, indent=2, ensure_ascii=False)
        h.write('\n')
    print('lang updated', path)

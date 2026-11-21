import io
import json

zh = {
    'gui.goldentweaks.jade.channel': '频道：%s',
    'gui.goldentweaks.jade.max_input': '最大输入：%s FE / t',
    'gui.goldentweaks.jade.progress': '%s / %s',
    'gui.goldentweaks.jade.energy': '%s / %s FE',
    'gui.goldentweaks.jade.expected': '预计产出：',
    'gui.goldentweaks.jade.more': '……还有 %s 项',
    'gui.goldentweaks.jade.status.processing': '运行中',
}
en = {
    'gui.goldentweaks.jade.channel': 'Channel: %s',
    'gui.goldentweaks.jade.max_input': 'Max input: %s FE/t',
    'gui.goldentweaks.jade.progress': '%s / %s',
    'gui.goldentweaks.jade.energy': '%s / %s FE',
    'gui.goldentweaks.jade.expected': 'Expected output:',
    'gui.goldentweaks.jade.more': '...and %s more',
    'gui.goldentweaks.jade.status.processing': 'Running',
}
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

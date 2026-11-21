import io
import json

zh = {'gui.goldentweaks.port.running.on': '正在参与机器（点击停用）',
      'gui.goldentweaks.port.running.off': '已停用（点击启用）'}
en = {'gui.goldentweaks.port.running.on': 'Active (click to disable)',
      'gui.goldentweaks.port.running.off': 'Disabled (click to enable)'}
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

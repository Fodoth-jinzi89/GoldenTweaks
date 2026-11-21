import io

p = r'src/main/java/net/fodoth/skina/goldentweaks/compat/jade/GTReactorJadeComponent.java'
s = io.open(p, encoding='utf-8').read()
orig = s

# both bars passed null as the box style, which the helper rejects
s = s.replace('        return IElementHelper.get().progress(fraction, label, style, null, false);\n    }\n\n    /** The energy bar',
              '        return IElementHelper.get().progress(fraction, label, style,\n                BoxStyle.getNestedBox(), false);\n    }\n\n    /** The energy bar', 1)
s = s.replace('''                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style, null, false);
    }''',
'''                .fitContentX(false);
        return IElementHelper.get().progress(fraction, label, style,
                BoxStyle.getNestedBox(), false);
    }''', 1)

s = s.replace('import snownee.jade.api.ui.IElement;',
              'import snownee.jade.api.ui.BoxStyle;\nimport snownee.jade.api.ui.IElement;', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('changed' if s != orig else 'NO CHANGE')
print('remaining null boxes:', s.count('style, null, false'))

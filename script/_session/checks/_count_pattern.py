layers = [
 ["xxx","xxx","xxx"],
 ["xxx","xox","xax"],
 ["xxx","xxx","xxx"],
 ["xxo","xxx","oxx"],
]
from collections import Counter
c = Counter()
for layer in layers:
    for row in layer:
        c.update(row)
print(c)
print("total cells", sum(c.values()))

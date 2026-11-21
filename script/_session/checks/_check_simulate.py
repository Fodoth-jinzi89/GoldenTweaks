"""Check that the single-cell refusal also applies under simulation.

AMI's canInsertItems simulates; if the refusal only happened on a real insert, the recipe check
would pass and the craft would then find nowhere to deliver.
"""
import io, re

p = r'src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortBlockEntity.java'
s = io.open(p, encoding='utf-8').read()
m = re.search(r"public ItemStack insertItem\(int slot, ItemStack stack, boolean simulate\) \{(.*?)\n                    \}",
              s, re.S)
print(m.group(0) if m else 'NOT FOUND')

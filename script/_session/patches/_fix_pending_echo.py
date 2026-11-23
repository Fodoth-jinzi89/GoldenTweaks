"""Fix the in-flight echo guard so it compares like with like.

`send` stores what it transmits, which for the priority box is a bare integer ("1600"), while
`echo` compares against what the box displays, which is grouped ("1,600"). The two never match,
so the pending entry was never cleared and that box silently stopped accepting server updates
for the rest of the session.

The guard now stores the same form the box will show, leaving the wire value untouched.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/content/multiblock/GTPortScreen.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''            this.pendingFields.put(field, value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));''',
'''            // Stored in the form the box will show, not the form that goes on the wire: `echo`
            // compares against the displayed text, and a bare integer would never match a group
            // like "1,600", leaving this field pinned to its pending value for good.
            this.pendingFields.put(field,
                    field == GTPortBlockEntity.Field.PRIORITY ? displayedPriority(value) : value);
            PacketDistributor.sendToServer(new C2SPortTextPacket(port.getBlockPos(), field, value));''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("pending echo guard compares displayed forms")

"""Make the essentia storage speak IEssentiaTransport.

Thaumcraft's pipe network does not talk to a container: it talks to a transport, which answers
per-face questions about suction and moves essentia one aspect at a time. The storage keeps its
own books and this interface is the adapter the tubes reach.
"""
import io

p = r"src/main/java/net/fodoth/skina/goldentweaks/compat/thaumcraft/GTPortEssentiaStorage.java"
s = io.open(p, encoding="utf-8").read()

s = s.replace('''public class GTPortEssentiaStorage implements IAspectContainer {''',
'''public class GTPortEssentiaStorage implements IAspectContainer, IEssentiaTransport {''', 1)

s = s.replace('''import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;''',
'''import net.minecraft.core.Direction;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;''', 1)

s = s.replace('''    // ---- persistence ----''',
'''    // ---- IEssentiaTransport: what the tubes reach ----

    @Override
    public boolean isConnectable(Direction side) {
        return true;
    }

    /**
     * Whether essentia may enter from this side.
     *
     * <p>Both directions are open. A port's own input/output switch already decides which half of
     * the machine it belongs to; refusing here as well would make a correctly oriented tank
     * silently refuse its own supply.</p>
     */
    @Override
    public boolean canInputFrom(Direction side) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return true;
    }

    /**
     * Suction is not modelled.
     *
     * <p>A port is a buffer inside a machine, not a tube: it neither pulls from nor competes with
     * the network, so it advertises the minimum and lets the machine decide when to ask.</p>
     */
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public Aspect getSuctionType(Direction side) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction side) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction side) {
        return take(aspect, amount);
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction side) {
        return add(aspect, amount);
    }

    /** The first aspect this port holds, which is what a tube asks before deciding to pull. */
    @Override
    public Aspect getEssentiaType(Direction side) {
        for (Aspect aspect : this.aspects) {
            if (aspect != null) {
                return aspect;
            }
        }
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction side) {
        return totalAmount();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    // ---- persistence ----''', 1)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("essentia storage is a transport")

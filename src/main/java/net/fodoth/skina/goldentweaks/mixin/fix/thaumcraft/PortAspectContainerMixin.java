package net.fodoth.skina.goldentweaks.mixin.fix.thaumcraft;

import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.port.GTPortKind;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

/**
 * 让源质端口自身也实现 {@link IAspectContainer}。
 *
 * <p>ThaumicEnergistics 的随时可用能力（合成结果导入、ME 接口的源质访问卡、存储总线）并不只看
 * {@code EssentiaCapabilities.TRANSPORT}：{@code EssentiaContainerStorage} 在判断「对面这个方块
 * 能不能收/能不能给」时直接做 {@code target instanceof IAspectContainer}，并在命中后调用
 * {@code doesContainerAccept} / {@code addToContainer} / {@code takeFromContainer} 这一组方法。
 * 我们的端口只把 {@code IAspectContainer} 实现在内部存储对象上，方块实体本身没有实现它，
 * 于是接口会跳过容器校验、既不认「能不能收」也拿不到内容 —— 表现就是主动模式的罐无法与挂了
 * 源质访问卡的 ME 接口直接交互，只能靠输入/输出总线中转。</p>
 *
 * <p>这里把端口实体补齐为 {@code IAspectContainer}，并整体转发给已经实现了该接口的内部存储
 * （{@link GTPortBlockEntity#essentiaStorage()}）。非源质端口一律按空容器处理。</p>
 */
@Mixin(value = GTPortBlockEntity.class, remap = false)
public abstract class PortAspectContainerMixin implements IAspectContainer {

    @Unique
    @Nullable
    private IAspectContainer gt$aspectContainer() {
        GTPortBlockEntity port = (GTPortBlockEntity) (Object) this;
        if (port.kind() != GTPortKind.ESSENTIA) {
            return null;
        }
        Object storage = port.essentiaStorage();
        return storage instanceof IAspectContainer container ? container : null;
    }

    @Override
    public AspectList getAspects() {
        IAspectContainer container = gt$aspectContainer();
        return container == null ? new AspectList() : container.getAspects();
    }

    @Override
    public void setAspects(AspectList aspects) {
        IAspectContainer container = gt$aspectContainer();
        if (container != null) {
            container.setAspects(aspects);
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        IAspectContainer container = gt$aspectContainer();
        return container != null && container.doesContainerAccept(aspect);
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        IAspectContainer container = gt$aspectContainer();
        return container == null ? amount : container.addToContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        IAspectContainer container = gt$aspectContainer();
        return container != null && container.takeFromContainer(aspect, amount);
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        IAspectContainer container = gt$aspectContainer();
        return container != null && container.takeFromContainer(aspects);
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        IAspectContainer container = gt$aspectContainer();
        return container != null && container.doesContainerContainAmount(aspect, amount);
    }

    @Override
    public boolean doesContainerContain(AspectList aspects) {
        IAspectContainer container = gt$aspectContainer();
        return container != null && container.doesContainerContain(aspects);
    }

    @Override
    public int containerContains(Aspect aspect) {
        IAspectContainer container = gt$aspectContainer();
        return container == null ? 0 : container.containerContains(aspect);
    }
}

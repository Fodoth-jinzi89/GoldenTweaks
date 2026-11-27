package net.fodoth.skina.goldentweaks.compat.thaumicenergistics;

import net.fodoth.skina.goldentweaks.GoldenTweaks;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortBlockEntity;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortKind;
import net.fodoth.skina.goldentweaks.content.multiblock.GTPortStorages;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.AspectStack;
import thaumicenergistics.api.essentia.EssentiaEndpointProviders;
import thaumicenergistics.api.essentia.IEssentiaEndpointProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 让 ThaumicEnergistics 的样板供应器能直接把要素推进源质端口。
 *
 * <p><b>为什么除了 {@code IAspectContainer} 还需要这个：</b>端口实体已经通过
 * {@code fix.thaumcraft.PortAspectContainerMixin} 实现了容器接口，那解决了「能不能被认出来」。
 * 但 {@code ThEEssentiaPushTarget.thE$typeAccepts} 在 {@code accessSide == null}
 * （AE 推送到方块时不带方向，这是常见情况）时对容器要求三条同时成立：
 * 是 {@code IAspectContainer}、{@code TCReflection.isEssentiaStore} 认可、且
 * {@code doesContainerAccept} 为真。任一条不满足，要素就会以通用堆栈的形式留在供应器里。</p>
 *
 * <p>{@code EssentiaEndpoints.insert} 的优先级是 provider 高于 {@code IEssentiaTransport}，
 * 且 provider 完全不看 {@code accessSide}，所以从这一层接管最稳：识别走容器接口，
 * 实际写入走这里，两条路互不依赖。</p>
 */
public final class GTPortEssentiaEndpointProvider implements IEssentiaEndpointProvider {

    private static final GTPortEssentiaEndpointProvider INSTANCE = new GTPortEssentiaEndpointProvider();

    private GTPortEssentiaEndpointProvider() {
    }

    /**
     * 注册进 TE 的端点表。
     *
     * <p>挂在 {@code FMLCommonSetupEvent} 上：此时所有模组的构造与注册都已完成，
     * {@code EssentiaEndpointProviders} 的静态表已经可用，而配方/界面尚未开始运行。</p>
     */
    public static void register(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        // 端点表是普通静态列表，跨线程写会与 TE 自己的注册竞争；按 NeoForge 的约定放到主线程。
        event.enqueueWork(() -> {
            EssentiaEndpointProviders.register(INSTANCE);
            GoldenTweaks.LOGGER.info("[GT] registered essentia endpoint provider for source ports");
        });
    }

    /**
     * 只认源质端口。
     *
     * <p>{@code owns} 是 TE 选路的唯一依据：它一旦认领，{@code insert}/{@code extract}/
     * {@code contents} 就全由这里答复，不再回落到 {@code IEssentiaTransport}。</p>
     */
    @Override
    public boolean owns(BlockEntity blockEntity) {
        return blockEntity instanceof GTPortBlockEntity port && port.kind() == GTPortKind.ESSENTIA;
    }

    /** 端口内容，按 TE 自己的栈类型报告。 */
    @Override
    public List<AspectStack> contents(BlockEntity blockEntity, @Nullable Direction side) {
        Object storage = storageOf(blockEntity);
        if (storage == null) {
            return List.of();
        }
        List<AspectStack> stacks = new ArrayList<>();
        for (ResourceLocation aspect : GTPortStorages.essentiaAspects(storage)) {
            long amount = GTPortStorages.essentiaAmount(storage, aspect);
            if (amount > 0) {
                stacks.add(new AspectStack(aspect, amount));
            }
        }
        return stacks;
    }

    /** AE 从端口抽要素。 */
    @Override
    public AspectStack extract(BlockEntity blockEntity, ResourceLocation aspect, int amount,
                               @Nullable Direction side) {
        Object storage = storageOf(blockEntity);
        if (storage == null || amount <= 0) {
            return null;
        }
        long taken = GTPortStorages.takeEssentia(storage, aspect, amount);
        return taken <= 0 ? null : new AspectStack(aspect, taken);
    }

    /**
     * AE 把要素推进端口。
     *
     * <p>这是本次修复的核心入口：返回实际接收量。返回 0 会让供应器把这一份要素留在原处，
     * 也就是玩家看到的「卡在样板供应器里」。</p>
     *
     * <p>不看方向也不看开关：方向由样板供应器自己决定连哪一面，而端口的输入/输出语义
     * 已经由 {@code mode} 表达；在这里再拦一次只会让正确摆放的端口静默拒收。</p>
     */
    @Override
    public int insert(BlockEntity blockEntity, ResourceLocation aspect, int amount,
                      @Nullable Direction side) {
        Object storage = storageOf(blockEntity);
        if (storage == null || amount <= 0) {
            return 0;
        }
        // 空 id 或无法解析的要素直接拒收，避免把垃圾写进罐里。
        if (Aspect.getAspect(aspect.getPath()) == null) {
            GoldenTweaks.LOGGER.warn("[GT] essentia insert rejected: unknown aspect {}", aspect);
            return 0;
        }
        int accepted = (int) Math.max(0, GTPortStorages.addEssentia(storage, aspect, amount));
        return accepted;
    }

    /** 端口里的源质容器，或 {@code null}（非源质端口）。 */
    @Nullable
    private static Object storageOf(BlockEntity blockEntity) {
        if (!(blockEntity instanceof GTPortBlockEntity port)
                || port.kind() != GTPortKind.ESSENTIA) {
            return null;
        }
        return port.essentiaStorage();
    }
}

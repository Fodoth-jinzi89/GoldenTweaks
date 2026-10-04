package net.fodoth.skina.goldentweaks.compat.academy;

import com.mohistmc.academy.world.AcademySounds;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 学院（AcademyCraft）的音效注册失败时，{@link AcademySounds#SOUND_EVENTS} 里的
 * DeferredHolder 全部未绑定，客户端读取音效 Holder 会抛
 * "Trying to access unbound value" 导致崩溃。统一在这里判断音效是否全部绑定。
 */
public final class AcademySoundGuard {

    public static boolean hasUnboundSoundEvent() {
        for (DeferredHolder<?, ?> holder : AcademySounds.SOUND_EVENTS.getEntries()) {
            if (!holder.isBound()) {
                return true;
            }
        }
        return false;
    }

    private AcademySoundGuard() {
    }
}

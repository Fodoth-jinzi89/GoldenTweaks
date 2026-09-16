package net.fodoth.skina.goldentweaks.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * NeoForge 的 RuntimeDistCleaner 在专用服务器（DEDICATED_SERVER）上拒绝加载的包。
 * 本项目实测被拒的类：{@code Screen}、{@code ClientLevel}、{@code LocalPlayer}、{@code BakedModel}、
 * {@code PoseStack$Pose}、{@code VertexFormat$Mode}、{@code BlockEntityRendererProvider$Context}。
 *
 * <p>注意 {@code net.neoforged.neoforge.client.*} 在 1.21.1 里**不会**被剥离。</p>
 */
public final class ClientDistRefs {

    private static final String[] STRIPPED_PACKAGES = {
            "net/minecraft/client/",
            "com/mojang/blaze3d/",
            "com/mojang/realmsclient/",
            "net/minecraft/realms/"
    };

    private ClientDistRefs() {
    }

    public static boolean isStripped(String internalName) {

        if (internalName == null) {
            return false;
        }

        for (String stripped : STRIPPED_PACKAGES) {

            if (internalName.startsWith(stripped)) {
                return true;
            }
        }

        return false;
    }

    public static boolean descriptorHasStripped(String descriptor) {

        if (descriptor == null) {
            return false;
        }

        for (String stripped : STRIPPED_PACKAGES) {

            if (descriptor.contains(stripped)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 这个类自己的字节码里是否含被剥离的引用。解析 lambda 的方法句柄、或调用一个类，都会触发该类的链接，
     * 只要它内部有客户端类型（比如方法签名带 {@code BakedModel}），服务端就会在这里炸——
     * linearbearing 的 ClientModHandler 就是这种情况。
     */
    public static boolean classReferencesStripped(String internalName) {

        if (internalName == null) {
            return false;
        }

        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(internalName + ".class")) {

            if (in == null) {
                return false;
            }

            String constantPool = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);

            for (String stripped : STRIPPED_PACKAGES) {

                if (constantPool.contains(stripped)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // 读不到就当它没问题：宁可少改，也不要误删
        }

        return false;
    }
}

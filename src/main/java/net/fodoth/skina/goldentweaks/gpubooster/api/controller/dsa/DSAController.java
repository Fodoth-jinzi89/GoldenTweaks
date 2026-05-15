package net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa;

import net.fodoth.skina.goldentweaks.config.GoldenTweaksClientConfig;
import net.fodoth.skina.goldentweaks.util.DSAVariant;

public interface DSAController {

    CoreDSAController CORE = new CoreDSAController();
    ARBDSAController ARB = new ARBDSAController();

    static DSAController get() {
        return GoldenTweaksClientConfig.DSA_VARIANT.get() == DSAVariant.ARB
                ? ARB
                : CORE;
    }

    int createVAO();

    int createBO();

    int createFBO();

    int createTexture();

    void enableVAOAttrib(int vao, int index);

    void vaoFormat(int vao, int index, int size, int type, boolean normalized, int offset);

    void vaoFormat(int vao, int index, int size, int type, int offset);

    void bindVAOAttrib(int vao, int index, int buffer);

    void namedBufferData(int buffer, java.nio.ByteBuffer data, int usage);

    void namedBufferData(int buffer, long size, int usage);

    void namedBufferSubData(int buffer, long offset, java.nio.ByteBuffer data);

    void addEBO2VAO(int vao, int ebo);

    void addVBO2VAO(int vao, int attrib, int vbo, long offset, int stride);

    void namedFramebufferTexture(int fbo, int attachment, int texture, int level);

    void textureParameter(int texture, int pname, int param);

    void textureStorage(int texture, int levels, int internalFormat, int width, int height);

    void deleteVAO(int vao);
}
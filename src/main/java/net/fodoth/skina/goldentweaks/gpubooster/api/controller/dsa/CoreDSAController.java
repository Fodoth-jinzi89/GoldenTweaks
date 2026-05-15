package net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL46;

public class CoreDSAController implements DSAController {
    public CoreDSAController() {
    }

    public int createVAO() {
        return GL46.glCreateVertexArrays();
    }

    public int createBO() {
        return GL46.glCreateBuffers();
    }

    public int createFBO() {
        return GL46.glCreateFramebuffers();
    }

    public int createTexture() {
        return GL46.glCreateTextures(3553);
    }

    public void enableVAOAttrib(int vao, int index) {
        GL46.glEnableVertexArrayAttrib(vao, index);
    }

    public void vaoFormat(int vao, int attrib, int size, int type, boolean norm, int relativeOffset) {
        GL46.glVertexArrayAttribFormat(vao, attrib, size, type, norm, relativeOffset);
    }

    public void vaoFormat(int vao, int attrib, int size, int type, int relativeOffset) {
        GL46.glVertexArrayAttribIFormat(vao, attrib, size, type, relativeOffset);
    }

    public void bindVAOAttrib(int vao, int index, int bindIndex) {
        GL46.glVertexArrayAttribBinding(vao, index, bindIndex);
    }

    public void namedBufferData(int id, ByteBuffer buffer, int usage) {
        GL46.glNamedBufferData(id, buffer, usage);
    }

    public void namedBufferData(int id, long size, int usage) {
        GL46.glNamedBufferData(id, size, usage);
    }

    public void namedBufferSubData(int id, long offset, ByteBuffer buffer) {
        GL46.glNamedBufferSubData(id, offset, buffer);
    }

    public void addEBO2VAO(int vao, int ebo) {
        GL46.glVertexArrayElementBuffer(vao, ebo);
    }

    public void addVBO2VAO(int vao, int binding, int vbo, long offset, int stride) {
        GL46.glVertexArrayVertexBuffer(vao, binding, vbo, offset, stride);
    }

    public void namedFramebufferTexture(int fbo, int attachment, int texture, int levels) {
        GL46.glNamedFramebufferTexture(fbo, attachment, texture, levels);
    }

    public void textureParameter(int texture, int name, int param) {
        GL46.glTextureParameteri(texture, name, param);
    }

    public void textureStorage(int texture, int levels, int format, int w, int h) {
        GL46.glTextureStorage2D(texture, levels, format, w, h);
    }

    @Override
    public void deleteVAO(int vao) {
        GL46.glDeleteVertexArrays(vao);
    }
}

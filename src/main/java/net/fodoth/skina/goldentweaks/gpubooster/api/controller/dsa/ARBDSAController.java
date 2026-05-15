package net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.ARBVertexArrayObject;

public class ARBDSAController implements DSAController {
    public ARBDSAController() {
    }

    public int createVAO() {
        return ARBDirectStateAccess.glCreateVertexArrays();
    }

    public int createBO() {
        return ARBDirectStateAccess.glCreateBuffers();
    }

    public int createFBO() {
        return ARBDirectStateAccess.glCreateFramebuffers();
    }

    public int createTexture() {
        return ARBDirectStateAccess.glCreateTextures(3553);
    }

    public void enableVAOAttrib(int vao, int index) {
        ARBDirectStateAccess.glEnableVertexArrayAttrib(vao, index);
    }

    public void vaoFormat(int vao, int attrib, int size, int type, boolean norm, int relativeOffset) {
        ARBDirectStateAccess.glVertexArrayAttribFormat(vao, attrib, size, type, norm, relativeOffset);
    }

    public void vaoFormat(int vao, int attrib, int size, int type, int relativeOffset) {
        ARBDirectStateAccess.glVertexArrayAttribIFormat(vao, attrib, size, type, relativeOffset);
    }

    public void bindVAOAttrib(int vao, int index, int bindIndex) {
        ARBDirectStateAccess.glVertexArrayAttribBinding(vao, index, bindIndex);
    }

    public void namedBufferData(int id, ByteBuffer buffer, int usage) {
        ARBDirectStateAccess.glNamedBufferData(id, buffer, usage);
    }

    public void namedBufferData(int id, long size, int usage) {
        ARBDirectStateAccess.glNamedBufferData(id, size, usage);
    }

    public void namedBufferSubData(int id, long offset, ByteBuffer buffer) {
        ARBDirectStateAccess.glNamedBufferSubData(id, offset, buffer);
    }

    public void addEBO2VAO(int vao, int ebo) {
        ARBDirectStateAccess.glVertexArrayElementBuffer(vao, ebo);
    }

    public void addVBO2VAO(int vao, int binding, int vbo, long offset, int stride) {
        ARBDirectStateAccess.glVertexArrayVertexBuffer(vao, binding, vbo, offset, stride);
    }

    public void namedFramebufferTexture(int fbo, int attachment, int texture, int levels) {
        ARBDirectStateAccess.glNamedFramebufferTexture(fbo, attachment, texture, levels);
    }

    public void textureParameter(int texture, int name, int param) {
        ARBDirectStateAccess.glTextureParameteri(texture, name, param);
    }

    public void textureStorage(int texture, int levels, int format, int w, int h) {
        ARBDirectStateAccess.glTextureStorage2D(texture, levels, format, w, h);
    }

    @Override
    public void deleteVAO(int vao) {
        ARBVertexArrayObject.glDeleteVertexArrays(vao);
    }
}

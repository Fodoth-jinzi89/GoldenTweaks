package net.fodoth.skina.goldentweaks.gpubooster.client.renderer.gl;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.fodoth.skina.goldentweaks.gpubooster.api.controller.dsa.DSAController;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class VAOInstance {

    private final VertexFormat format;
    private final int vao;

    public static VAOInstance makeVAO(VertexFormat format) {
        VAOInstance instance = new VAOInstance(format);
        instance.setupFormat();
        return instance;
    }

    public VAOInstance(VertexFormat format) {
        this.format = format;
        this.vao = DSAController.get().createVAO();
    }

    public void setupFormat() {
        List<VertexFormatElement> elements = this.format.getElements();

        for (int j = 0; j < elements.size(); j++) {

            VertexFormatElement element = elements.get(j);

            int count = element.count();
            VertexFormatElement.Type type = element.type();
            int glType = type.glType();
            int offset = this.format.getOffset(element);

            VertexFormatElement.Usage usage = element.usage();

            boolean normalized =
                    usage == VertexFormatElement.Usage.NORMAL
                            || usage == VertexFormatElement.Usage.COLOR;

            DSAController.get().enableVAOAttrib(this.vao, j);

            /*
             * PADDING 在 NeoForge/Mojmap VertexFormat 中已不存在语义
             * 这里直接当作 no-op attribute（或依赖 stride padding）
             */
            DSAController.get().vaoFormat(
                    this.vao,
                    j,
                    count,
                    glType,
                    normalized,
                    offset
            );

            DSAController.get().bindVAOAttrib(this.vao, j, 0);
        }
    }

    public VertexFormat getFormat() {
        return this.format;
    }

    public int getID() {
        return this.vao;
    }

    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof VAOInstance other)) {
            return false;
        }

        return this.format.equals(other.format);
    }

    @Override
    public int hashCode() {
        return this.format.hashCode();
    }
}
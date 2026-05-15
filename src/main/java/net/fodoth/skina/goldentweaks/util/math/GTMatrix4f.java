package net.fodoth.skina.goldentweaks.util.math;

import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class GTMatrix4f extends Matrix4f {

    public GTMatrix4f() {
        super();
    }

    public GTMatrix4f(Matrix4f src) {
        super(src);
    }

    @Override
    public GTMatrix4f rotateX(float ang) {
        float c = Mth.cos(ang);
        float s = Mth.sin(ang);

        applyRotation(
                1.0F, 0.0F, 0.0F,
                0.0F, c, -s,
                0.0F, s, c
        );

        return this;
    }

    @Override
    public GTMatrix4f rotateY(float ang) {
        float c = Mth.cos(ang);
        float s = Mth.sin(ang);

        applyRotation(
                c, 0.0F, s,
                0.0F, 1.0F, 0.0F,
                -s, 0.0F, c
        );

        return this;
    }

    @Override
    public GTMatrix4f rotateZ(float ang) {
        float c = Mth.cos(ang);
        float s = Mth.sin(ang);

        applyRotation(
                c, -s, 0.0F,
                s, c, 0.0F,
                0.0F, 0.0F, 1.0F
        );

        return this;
    }

    @Override
    public GTMatrix4f rotate(float ang, float x, float y, float z) {

        float invLen = GTMath.invLen(x, y, z);

        x *= invLen;
        y *= invLen;
        z *= invLen;

        float c = Mth.cos(ang);
        float s = Mth.sin(ang);
        float t = 1.0F - c;

        float r00 = c + x * x * t;
        float r01 = x * y * t - z * s;
        float r02 = x * z * t + y * s;

        float r10 = y * x * t + z * s;
        float r11 = c + y * y * t;
        float r12 = y * z * t - x * s;

        float r20 = z * x * t - y * s;
        float r21 = z * y * t + x * s;
        float r22 = c + z * z * t;

        applyRotation(
                r00, r01, r02,
                r10, r11, r12,
                r20, r21, r22
        );

        return this;
    }

    public GTMatrix4f rotateXYZ(float angleX, float angleY, float angleZ) {

        float cx = Mth.cos(angleX);
        float sx = Mth.sin(angleX);

        float cy = Mth.cos(angleY);
        float sy = Mth.sin(angleY);

        float cz = Mth.cos(angleZ);
        float sz = Mth.sin(angleZ);

        float r00 = cy * cz;
        float r01 = -cy * sz;

        float r10 = sx * sy * cz + cx * sz;
        float r11 = -sx * sy * sz + cx * cz;
        float r12 = -sx * cy;

        float r20 = -cx * sy * cz + sx * sz;
        float r21 = cx * sy * sz + sx * cz;
        float r22 = cx * cy;

        applyRotation(
                r00, r01, sy,
                r10, r11, r12,
                r20, r21, r22
        );

        return this;
    }

    @Override
    public GTMatrix4f mul(Matrix4fc right) {

        float a00 = m00();
        float a01 = m01();
        float a02 = m02();
        float a03 = m03();

        float a10 = m10();
        float a11 = m11();
        float a12 = m12();
        float a13 = m13();

        float a20 = m20();
        float a21 = m21();
        float a22 = m22();
        float a23 = m23();

        float a30 = m30();
        float a31 = m31();
        float a32 = m32();
        float a33 = m33();

        float b00 = right.m00();
        float b01 = right.m01();
        float b02 = right.m02();
        float b03 = right.m03();

        float b10 = right.m10();
        float b11 = right.m11();
        float b12 = right.m12();
        float b13 = right.m13();

        float b20 = right.m20();
        float b21 = right.m21();
        float b22 = right.m22();
        float b23 = right.m23();

        float b30 = right.m30();
        float b31 = right.m31();
        float b32 = right.m32();
        float b33 = right.m33();

        m00(a00 * b00 + a10 * b01 + a20 * b02 + a30 * b03);
        m01(a01 * b00 + a11 * b01 + a21 * b02 + a31 * b03);
        m02(a02 * b00 + a12 * b01 + a22 * b02 + a32 * b03);
        m03(a03 * b00 + a13 * b01 + a23 * b02 + a33 * b03);

        m10(a00 * b10 + a10 * b11 + a20 * b12 + a30 * b13);
        m11(a01 * b10 + a11 * b11 + a21 * b12 + a31 * b13);
        m12(a02 * b10 + a12 * b11 + a22 * b12 + a32 * b13);
        m13(a03 * b10 + a13 * b11 + a23 * b12 + a33 * b13);

        m20(a00 * b20 + a10 * b21 + a20 * b22 + a30 * b23);
        m21(a01 * b20 + a11 * b21 + a21 * b22 + a31 * b23);
        m22(a02 * b20 + a12 * b21 + a22 * b22 + a32 * b23);
        m23(a03 * b20 + a13 * b21 + a23 * b22 + a33 * b23);

        m30(a00 * b30 + a10 * b31 + a20 * b32 + a30 * b33);
        m31(a01 * b30 + a11 * b31 + a21 * b32 + a31 * b33);
        m32(a02 * b30 + a12 * b31 + a22 * b32 + a32 * b33);
        m33(a03 * b30 + a13 * b31 + a23 * b32 + a33 * b33);

        return this;
    }

    @Override
    public GTMatrix4f transpose() {

        float t;

        t = m01();
        m01(m10());
        m10(t);

        t = m02();
        m02(m20());
        m20(t);

        t = m03();
        m03(m30());
        m30(t);

        t = m12();
        m12(m21());
        m21(t);

        t = m13();
        m13(m31());
        m31(t);

        t = m23();
        m23(m32());
        m32(t);

        return this;
    }

    @Override
    public GTMatrix4f invert() {

        float r00 = m00();
        float r01 = m01();
        float r02 = m02();

        float r10 = m10();
        float r11 = m11();
        float r12 = m12();

        float r20 = m20();
        float r21 = m21();
        float r22 = m22();

        float tx = m03();
        float ty = m13();
        float tz = m23();

        m00(r00);
        m01(r10);
        m02(r20);

        m10(r01);
        m11(r11);
        m12(r21);

        m20(r02);
        m21(r12);
        m22(r22);

        m03(-(m00() * tx + m01() * ty + m02() * tz));
        m13(-(m10() * tx + m11() * ty + m12() * tz));
        m23(-(m20() * tx + m21() * ty + m22() * tz));

        affineIdentity();

        return this;
    }

    private void applyRotation(
            float r00, float r01, float r02,
            float r10, float r11, float r12,
            float r20, float r21, float r22
    ) {

        float a00 = m00();
        float a01 = m01();
        float a02 = m02();

        float a10 = m10();
        float a11 = m11();
        float a12 = m12();

        float a20 = m20();
        float a21 = m21();
        float a22 = m22();

        float a03 = m03();
        float a13 = m13();
        float a23 = m23();

        m00(a00 * r00 + a01 * r10 + a02 * r20);
        m01(a00 * r01 + a01 * r11 + a02 * r21);
        m02(a00 * r02 + a01 * r12 + a02 * r22);
        m03(a03);

        m10(a10 * r00 + a11 * r10 + a12 * r20);
        m11(a10 * r01 + a11 * r11 + a12 * r21);
        m12(a10 * r02 + a11 * r12 + a12 * r22);
        m13(a13);

        m20(a20 * r00 + a21 * r10 + a22 * r20);
        m21(a20 * r01 + a21 * r11 + a22 * r21);
        m22(a20 * r02 + a21 * r12 + a22 * r22);
        m23(a23);
    }

    private void affineIdentity() {
        m30(0.0F);
        m31(0.0F);
        m32(0.0F);
        m33(1.0F);
    }
}
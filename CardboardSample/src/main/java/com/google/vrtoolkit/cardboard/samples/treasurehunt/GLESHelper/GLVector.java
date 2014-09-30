package com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper;

import android.opengl.Matrix;
import android.util.FloatMath;

public class GLVector {
    public static float TO_RADIANS = (1 / 180.0f) * (float) Math.PI;
    public static float TO_DEGREES = (1 / (float) Math.PI) * 180;

    private static final float[] matrix = new float[16];
    private static final float[] inVec = new float[4];
    private static final float[] outVec = new float[4];
    private float x, y, z;

    public GLVector() { }

    public GLVector(float x, float y, float z) {
        set(x, y, z);
    }

    public GLVector(GLVector other) {
        set(other);
    }

    public GLVector set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public GLVector set(GLVector other) {
        return set(other.getX(), other.getY(), other.getZ());
    }

    public GLVector add(float x, float y, float z) {
        set(this.x + x, this.y + y, this.z + z);
        return this;
    }

    public GLVector add(GLVector other) {
        set(this.x + other.x, this.y + other.y, this.z + other.z);
        return this;
    }

    public GLVector sub(float x, float y, float z) {
        set(this.x - x, this.y - y, this.z - z);
        return this;
    }

    public GLVector sub(GLVector other) {
        set(this.x - other.x, this.y - other.y, this.z - other.z);
        return this;
    }

    public GLVector mul(float scalar) {
        set(this.x * scalar, this.y * scalar, this.z * scalar);
        return this;
    }

    public float len() {
        return FloatMath.sqrt(x * x + y * y + z * z);
    }

    public GLVector nor() {
        float len = len();
        if(len != 0) {
            set(x / len, y / len, z / len);
        }
        return this;
    }

    public GLVector rotate(float angle, float axisX, float axisY, float axisZ) {
        inVec[0] = x;
        inVec[1] = y;
        inVec[2] = z;
        inVec[3] = 1;

        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, angle, axisX, axisY, axisZ);
        Matrix.multiplyMV(outVec, 0, matrix, 0, inVec, 0);

        x = outVec[0];
        y = outVec[1];
        z = outVec[2];

        return this;
    }

    public GLVector rotate(float angle, GLVector other) {
        return rotate(angle, other.getX(), other.getY(), other.getZ());
    }

    public float dist(float x, float y, float z) {
        float distX = this.x - x;
        float distY = this.y - y;
        float distZ = this.z - z;
        return FloatMath.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    public float dist(GLVector other) {
        return dist(other.getX(), other.getY(), other.getZ());
    }

    public float distSquared(float x, float y, float z) {
        float distX = this.x - x;
        float distY = this.y - y;
        float distZ = this.z - z;
        return distX * distX + distY * distY + distZ * distZ;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public GLVector copy() { return new GLVector(x, y, z); }
}

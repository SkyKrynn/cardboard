package com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper;

import android.opengl.GLES20;

public class GLProgram {
    private static final String TAG = "GLProgram";

    private int mProgramId;

    public GLProgram() {
        createProgram();
    }

    private void createProgram() {
        mProgramId = GLES20.glCreateProgram();
        GLError.check(TAG, "createProgram");
    }

    public GLProgram(GLShader vertexShader, GLShader fragmentShader) {
        this();
        addShader(vertexShader);
        addShader(fragmentShader);
    }

    public void addShader(GLShader shader) {
        GLES20.glAttachShader(mProgramId, shader.getShaderId());
        GLError.check(TAG, "addShader");
    }

    public void link() {
        GLES20.glLinkProgram(mProgramId);
        GLError.check(TAG, "link");
    }

    public void activate() {
        GLES20.glUseProgram(mProgramId);
        GLError.check(TAG, "activate");
    }

    public int getUniform(String uniformName) {
        int result = GLES20.glGetUniformLocation(mProgramId, uniformName);
        GLError.check(TAG, "getUniform");
        return result;
    }

    public int getAttrib(String attribName) {
        int result = GLES20.glGetAttribLocation(mProgramId, attribName);
        GLError.check(TAG, "getAttrib");
        return result;
    }

    protected void finalize() throws Throwable {
        GLES20.glDeleteProgram(mProgramId);
    }
}

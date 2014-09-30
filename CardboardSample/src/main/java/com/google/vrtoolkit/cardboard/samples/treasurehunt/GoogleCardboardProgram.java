package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.opengl.GLES20;

import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.DataBuffer;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.GLProgram;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.GLShader;

public class GoogleCardboardProgram extends GLProgram {
    private int mModelViewProjectionParam;
    private int mLightPosParam;
    private int mModelViewParam;
    private int mModelParam;
    private int mIsFloorParam;

    private int mPositionParam;
    private int mNormalParam;
    private int mColorParam;

    public GoogleCardboardProgram(GLShader vertexShader, GLShader fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public void link() {
        super.link();
        refresh();
    }

    public void  refresh() {
        mModelViewProjectionParam = getUniform("u_MVP");
        mLightPosParam = getUniform("u_LightPos");
        mModelViewParam = getUniform("u_MVMatrix");
        mModelParam = getUniform("u_Model");
        mIsFloorParam = getUniform("u_IsFloor");

        mPositionParam = getAttrib("a_Position");
        mNormalParam = getAttrib("a_Normal");
        mColorParam = getAttrib("a_Color");
    }

    public void setIsFloor(float isFloor) {
        GLES20.glUniform1f(mIsFloorParam, isFloor);
    }

    public void setLightPosition(float[] lightPosition) {
        GLES20.glUniform3f(mLightPosParam, lightPosition[0], lightPosition[1],
                lightPosition[2]);
    }

    public void setModel(float[] model) {
        GLES20.glUniformMatrix4fv(mModelParam, 1, false, model, 0);
    }

    public void setModelView(float[] modelView) {
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, modelView, 0);
    }

    public void setPositionVertices(DataBuffer buffer) {
        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glVertexAttribPointer(mPositionParam, 3, GLES20.GL_FLOAT,
            false, 0, buffer.getBuffer());
    }

    public void setModelViewProjection(float[] modelViewProjection) {
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, modelViewProjection, 0);
    }

    public void setNormalVertices(DataBuffer buffer) {
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT,
                false, 0, buffer.getBuffer());
    }

    public void setColor(DataBuffer buffer) {
        GLES20.glEnableVertexAttribArray(mColorParam);
        GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false,
                0, buffer.getBuffer());
    }

}

package com.google.vrtoolkit.cardboard.samples.treasurehunt.Models;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.DataBuffer;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.GLError;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GoogleCardboardProgram;

public class CubeModel {
    private static final String TAG = "CubeModel";
    private static final String DEFAULT = "DEFAULT";

    private ModelData mData;
    private DataBuffer mCurrentColor;
    private GoogleCardboardProgram mProgram;

    private float[] mModel = new float[16];

    private float[] mTranslateMatrix = new float[16];
    private float[] mRotateMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];

    public CubeModel(GoogleCardboardProgram program, ModelData data, DataBuffer defaultColors) {
        mProgram = program;
        mData = data;
        mData.addColorBuffer(DEFAULT, defaultColors);
        reset();
    }

    public void reset() {
        Matrix.setIdentityM(mModel, 0);
        Matrix.setIdentityM(mTranslateMatrix, 0);
        Matrix.setIdentityM(mRotateMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(mModel, 0, x, y, z);
        //Matrix.translateM(mTranslateMatrix, 0, x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        Matrix.rotateM(mModel, 0, angle, x, y, z);
        //Matrix.rotateM(mRotateMatrix, 0, angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        Matrix.scaleM(mModel, 0, x, y, z);
        //Matrix.scaleM(mScaleMatrix, 0, x, y, z);
    }

    public void addColorBuffer(String bufferName, DataBuffer colorBuffer) {
        if(bufferName.equals(DEFAULT))
            return;

        mData.addColorBuffer(bufferName, colorBuffer);
    }

    public void setColorBuffer(String bufferName) {
        mCurrentColor = mData.getColorBuffer(bufferName);
    }

    public void draw(EyeTransform transform, float[] view, float[] modelView, float[] modelViewProjection) {
/*
        Matrix.setIdentityM(mModel, 0);

        Matrix.multiplyMM(mModel, 0, mModel, 0, mRotateMatrix, 0);
//        Matrix.multiplyMM(mModel, 0, mModel, 0, mScaleMatrix, 0);
        Matrix.multiplyMM(mModel, 0, mModel, 0, mTranslateMatrix, 0);
*/

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, transform.getPerspective(), 0, modelView, 0);

        mProgram.setIsFloor(0f);
        mProgram.setModel(mModel);
        mProgram.setModelView(modelView);
        mProgram.setPositionVertices(mData.getVerticesBuffer());
        mProgram.setModelViewProjection(modelViewProjection);
        mProgram.setNormalVertices(mData.getNormalsBuffer());
        mProgram.setColor(mData.getColorBuffer(DEFAULT));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLError.check(TAG, "Drawing cube");
    }

}

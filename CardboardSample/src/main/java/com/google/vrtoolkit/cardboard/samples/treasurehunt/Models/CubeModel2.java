package com.google.vrtoolkit.cardboard.samples.treasurehunt.Models;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.DataBuffer;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.GLError;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.GLVector;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GoogleCardboardProgram;

public class CubeModel2 {
    private static final String TAG = "CubeModel2";
    private static final String DEFAULT = "DEFAULT";

    private DataBuffer mCurrentColor;
    private GoogleCardboardProgram mProgram;

    private float[] mModel = new float[16];
    private ModelData mData;
    private GLVector mPosition = new GLVector(0, 0, 0);
    private GLVector mOrientation = new GLVector(1, 1, 1);
    private float mAngle = 0;

    private GLVector mVelocity = new GLVector(0, 0, 0);
    private GLVector mAcceleration = new GLVector(0, 0, 0);
    private GLVector mSpin = new GLVector(1, 1, 1);
    private boolean mIsSpinning = false;

    public CubeModel2(GoogleCardboardProgram program, ModelData data, DataBuffer defaultColors) {
        mProgram = program;
        mData = data;
        mData.addColorBuffer(DEFAULT, defaultColors);
    }

    public void setPosition(float x, float y, float z) { mPosition.set(x, y, z); }
    public void setVelocity(float x, float y, float z) { mVelocity.set(x, y, z); }
    public void setAcceleration(float x, float y, float z) { mAcceleration.set(x, y, z); }
    public void setOrientation(float angle, float x, float y, float z) { this.mAngle = angle; mOrientation.set(x, y, z); }
    public void setSpin(float x, float y, float z) { mIsSpinning = true; mSpin.set(x, y, z); }
    public void setSpin(float angle, float x, float y, float z) { this.mAngle = angle; setSpin(x, y, z); }

    public void update(float timeDelta) {
        mVelocity.add(mAcceleration.getX() * timeDelta, mAcceleration.getY() * timeDelta, mAcceleration.getZ() * timeDelta);
        mPosition.add(mVelocity.getX() * timeDelta, mVelocity.getY() * timeDelta, mVelocity.getZ() * timeDelta);

        if(mIsSpinning) {
            mOrientation.add(mSpin.getX() * timeDelta, mSpin.getY() * timeDelta, mSpin.getZ() * timeDelta);
            Log.i(TAG, "Spin:  " + mOrientation.getX() + "," + mOrientation.getY() + "," + mOrientation.getZ());
        }
    }

    public void draw(EyeTransform transform, float[] view, float[] modelView, float[] modelViewProjection) {
/*
        Matrix.setIdentityM(mModel, 0);

        Matrix.multiplyMM(mModel, 0, mModel, 0, mRotateMatrix, 0);
//        Matrix.multiplyMM(mModel, 0, mModel, 0, mScaleMatrix, 0);
        Matrix.multiplyMM(mModel, 0, mModel, 0, mTranslateMatrix, 0);
*/

        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mPosition.getX(), mPosition.getY(), mPosition.getZ());
        Matrix.rotateM(mModel, 0, mAngle, mOrientation.getX(), mOrientation.getY(), mOrientation.getZ());

        Matrix.multiplyMM(modelView, 0, view, 0, mModel, 0);
        Matrix.multiplyMM(modelViewProjection, 0, transform.getPerspective(), 0, modelView, 0);

        mProgram.setIsFloor(0f);
        mProgram.setModel(mModel);
        mProgram.setModelView(modelView);
        mProgram.setPositionVertices(mData.getVerticesBuffer());
        mProgram.setModelViewProjection(modelViewProjection);
        mProgram.setNormalVertices(mData.getNormalsBuffer());
        mProgram.setColor(mData.getColorBuffer(DEFAULT));

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mData.getNumVertices());
        GLError.check(TAG, "Drawing cube");
    }
}

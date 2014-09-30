/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import com.google.vrtoolkit.cardboard.*;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.*;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.Models.CubeModel;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.Models.CubeModel2;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.Models.ModelData;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A Cardboard sample application.
 */
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "MainActivity";
    private static final boolean USE_OLD = false;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    // We keep the light always position just above the user.
    private final float[] mLightPosInWorldSpace = new float[] {0.0f, 2.0f, 0.0f, 1.0f};
    private final float[] mLightPosInEyeSpace = new float[4];

    private static final int COORDS_PER_VERTEX = 3;

    private final WorldLayoutData DATA = new WorldLayoutData();

    private DataBuffer mFloorVertices;
    private DataBuffer mFloorColors;
    private DataBuffer mFloorNormals;

    private DataBuffer mCubeVertices;
    private DataBuffer mCubeColors;
    private DataBuffer mCubeFoundColors;
    private DataBuffer mCubeNormals;

    private GoogleCardboardProgram mGlProgram;
    private CubeModel mCube;
    private CubeModel2 mCube2;
    private CubeModel2 mTarget;
    private CubeModel2 mMushroom;

    private int mPositionParam;
    private int mNormalParam;
    private int mColorParam;
    private int mModelViewProjectionParam;
    private int mLightPosParam;
    private int mModelViewParam;
    private int mModelParam;
    private int mIsFloorParam;

    private float[] mModelCube;
    private float[] mCamera;
    private float[] mView;
    private float[] mHeadView;
    private float[] mModelViewProjection;
    private float[] mModelView;

    private float[] mModelFloor;

    private int mScore = 0;
    private float mObjectDistance = 12f;
    private float mObjectMinDistance = 5f;
    private float mObjectMaxDistance = 20f;
    private float mObjectMoveDelta = 0.1f;
    private float mObjectDirection = 1f;
    private float mObjectCurrentDistance = mObjectDistance;
    private float mFloorDepth = 20f;

    private Vibrator mVibrator;

    private CardboardOverlayView mOverlayView;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private GLShader createGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        return new GLShader(type, code);
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        mModelCube = new float[16];
        mCamera = new float[16];
        mView = new float[16];
        mModelViewProjection = new float[16];
        mModelView = new float[16];
        mModelFloor = new float[16];
        mHeadView = new float[16];
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("Pull the magnet when you find an object.");
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world. OpenGL doesn't use Java
     * arrays, but rather needs data in a format it can understand. Hence we use ByteBuffers.
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well

        mCubeVertices = new DataBuffer(DATA.CUBE_COORDS, 3);
        mCubeColors = new DataBuffer(DATA.CUBE_COLORS, 4);
        mCubeFoundColors = new DataBuffer(DATA.CUBE_FOUND_COLORS, 4);
        mCubeNormals = new DataBuffer(DATA.CUBE_NORMALS, 3);


        mFloorVertices = new DataBuffer(DATA.FLOOR_COORDS, 3);
        mFloorNormals = new DataBuffer(DATA.FLOOR_NORMALS, 3);
        mFloorColors = new DataBuffer(DATA.FLOOR_COLORS, 4);

        GLShader vertexShader = createGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        GLShader gridShader = createGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        mGlProgram = new GoogleCardboardProgram(vertexShader, gridShader);
        mGlProgram.link();

        mCube = new CubeModel(mGlProgram,
                new ModelData(new DataBuffer(DATA.CUBE_COORDS, 3),
                new DataBuffer(DATA.CUBE_NORMALS, 3)),
                new DataBuffer(DATA.CUBE_COLORS, 4));
        mCube.addColorBuffer("Found", new DataBuffer(DATA.CUBE_FOUND_COLORS, 4));

        mCube2 = new CubeModel2(mGlProgram,
                new ModelData(new DataBuffer(DATA.CUBE_COORDS, 3),
                        new DataBuffer(DATA.CUBE_NORMALS, 3)),
                new DataBuffer(DATA.CUBE_COLORS, 4));
        mCube2.setPosition(0, 0, -mObjectDistance);
        //mCube2.setVelocity(0, 0.5f, -0.6f);
        //mCube2.setAcceleration(0, -0.05f, 0);
        mCube2.setOrientation(10f, 0.0f, 1.0f, 0.0f);
        mCube2.setOrientation(45f, 0.5f, 0.5f, 1.0f);
        mCube2.setSpin(0.5f, 0.5f, 1.0f);


        mTarget = new CubeModel2(mGlProgram,
                new ModelData(new DataBuffer(TargetModelData.COORDS, 3),
                        new DataBuffer(TargetModelData.NORMALS, 3)),
                new DataBuffer(TargetModelData.COLORS, 4));
        mTarget.setPosition(-3f, 0f, mObjectDistance);
        mTarget.setVelocity(0, 0.0f, -0.1f);
        //mTarget.setAcceleration(0, -0.05f, 0);
        mTarget.setOrientation(90f, -1.0f, 0.0f, 0.0f);

        mMushroom = new CubeModel2(mGlProgram,
                new ModelData(new DataBuffer(MushroomModelData.COORDS, 3),
                        new DataBuffer(MushroomModelData.NORMALS, 3)),
                new DataBuffer(MushroomModelData.COLORS, 4));
        mMushroom.setPosition(3f, 0f, mObjectDistance);
        mMushroom.setVelocity(0, 0.0f, -0.06f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Object first appears directly in front of user
        Matrix.setIdentityM(mModelCube, 0);
        Matrix.translateM(mModelCube, 0, 0, 0, -mObjectDistance);

        mCube.translate(-3, 0, -mObjectDistance);
        mCube.rotate(10, 0.0f, 1.0f, 0.0f);


        Matrix.setIdentityM(mModelFloor, 0);
        Matrix.translateM(mModelFloor, 0, 0, -mFloorDepth, 0); // Floor appears below user

        GLError.check(TAG, "onSurfaceCreated");
    }

    /**
     * Converts a raw text file into a string.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        mGlProgram.activate();

        if(USE_OLD) {
            mModelViewProjectionParam = mGlProgram.getUniform("u_MVP");
            mLightPosParam = mGlProgram.getUniform("u_LightPos");
            mModelViewParam = mGlProgram.getUniform("u_MVMatrix");
            mModelParam = mGlProgram.getUniform("u_Model");
            mIsFloorParam = mGlProgram.getUniform("u_IsFloor");
        }

        mObjectCurrentDistance += mObjectMoveDelta * mObjectDirection;
        if((mObjectCurrentDistance < mObjectMinDistance) || (mObjectCurrentDistance > mObjectMaxDistance)) {
            mObjectDirection *= -1f;
        }

        //Matrix.translateM(mModelCube, 0, 0, 0, mObjectMoveDelta * mObjectDirection);
        //mCube.translate(0, 0, mObjectMoveDelta * mObjectDirection);
        Log.i(TAG, "Distance: " + mObjectCurrentDistance);

        // Build the Model part of the ModelView matrix.
        Matrix.rotateM(mModelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
        mCube.rotate(TIME_DELTA, 0.5f, 0.5f, 1.0f);

        mCube2.update(TIME_DELTA);
        mTarget.update(TIME_DELTA);
        mMushroom.update(TIME_DELTA);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(mHeadView, 0);

        GLError.check(TAG, "onReadyToDraw");
    }

    /**
     * Draws a frame for an eye. The transformation for that eye (from the camera) is passed in as
     * a parameter.
     * @param transform The transformations to apply to render this eye.
     */
    @Override
    public void onDrawEye(EyeTransform transform) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(USE_OLD) {
            mPositionParam = mGlProgram.getAttrib("a_Position");
            mNormalParam = mGlProgram.getAttrib("a_Normal");
            mColorParam = mGlProgram.getAttrib("a_Color");

            GLES20.glEnableVertexAttribArray(mPositionParam);
            GLES20.glEnableVertexAttribArray(mNormalParam);
            GLES20.glEnableVertexAttribArray(mColorParam);
            GLError.check(TAG, "mColorParam");
        }

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, transform.getEyeView(), 0, mCamera, 0);

        // Set the position of the light
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, mLightPosInWorldSpace, 0);
        if(USE_OLD) {
            GLES20.glUniform3f(mLightPosParam, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1],
                    mLightPosInEyeSpace[2]);
        } else {
            mGlProgram.setLightPosition(mLightPosInEyeSpace);
        }

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCube, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, transform.getPerspective(), 0, mModelView, 0);
        //drawCube();

        //mCube.draw(transform, mView, mModelView, mModelViewProjection);
        mCube2.draw(transform, mView, mModelView, mModelViewProjection);

        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCube, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, transform.getPerspective(), 0, mModelView, 0);
        mTarget.draw(transform, mView, mModelView, mModelViewProjection);

        mMushroom.draw(transform, mView, mModelView, mModelViewProjection);

        // Set mModelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelFloor, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, transform.getPerspective(), 0,
            mModelView, 0);
        drawFloor(transform.getPerspective());
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draw the cube. We've set all of our transformation matrices. Now we simply pass them into
     * the shader.
     */
    public void drawCube() {
        // 1. This is not the floor!
        // 2. Set the Model in the shader, used to calculate lighting
        // 3. Set the ModelView in the shader, used to calculate lighting
        // 4. Set the position of the cube
        // 5. Set the ModelViewProjection matrix in the shader.
        // 6. Set the normal positions of the cube, again for shading
        if(USE_OLD) {
            GLES20.glUniform1f(mIsFloorParam, 0f);
            GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelCube, 0);
            GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
            GLES20.glVertexAttribPointer(mPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                    false, 0, mCubeVertices.getBuffer());
            GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, mModelViewProjection, 0);
            GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT,
                    false, 0, mCubeNormals.getBuffer());
        } else {
            mGlProgram.setIsFloor(0f);
            mGlProgram.setModel(mModelCube);
            mGlProgram.setModelView(mModelView);
            mGlProgram.setPositionVertices(mCubeVertices);
            mGlProgram.setModelViewProjection(mModelViewProjection);
            mGlProgram.setNormalVertices(mCubeNormals);
        }


        if (isLookingAtObject()) {
            if(USE_OLD) {
                GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false,
                        0, mCubeFoundColors.getBuffer());
            } else {
                mGlProgram.setColor(mCubeFoundColors);
            }
        } else {
            if(USE_OLD) {
                GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false,
                        0, mCubeColors.getBuffer());
            } else {
                mGlProgram.setColor(mCubeColors);
            }
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLError.check(TAG, "Drawing cube");
    }

    /**
     * Draw the floor. This feeds in data for the floor into the shader. Note that this doesn't
     * feed in data about position of the light, so if we rewrite our code to draw the floor first,
     * the lighting might look strange.
     */
    public void drawFloor(float[] perspective) {
        if(USE_OLD) {
            // This is the floor!
            GLES20.glUniform1f(mIsFloorParam, 1f);

            // Set ModelView, MVP, position, normals, and color
            GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelFloor, 0);
            GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
            GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, mModelViewProjection, 0);
            GLES20.glVertexAttribPointer(mPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                    false, 0, mFloorVertices.getBuffer());
            GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0, mFloorNormals.getBuffer());
            GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0, mFloorColors.getBuffer());
        } else {
            mGlProgram.setIsFloor(1f);
            mGlProgram.setModel(mModelFloor);
            mGlProgram.setModelView(mModelView);
            mGlProgram.setModelViewProjection(mModelViewProjection);
            mGlProgram.setPositionVertices(mFloorVertices);
            mGlProgram.setNormalVertices(mFloorNormals);
            mGlProgram.setColor(mFloorColors);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        GLError.check(TAG, "drawing floor");
    }

    /**
     * Increment the score, hide the object, and give feedback if the user pulls the magnet while
     * looking at the object. Otherwise, remind the user what to do.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        if (isLookingAtObject()) {
            mScore++;
            mOverlayView.show3DToast("Found it! Look around for another one.\nScore = " + mScore);
            hideObject();
        } else {
            mOverlayView.show3DToast("Look around to find the object!");
        }
        // Always give user feedback
        mVibrator.vibrate(50);
    }

    /**
     * Find a new random position for the object.
     * We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
     */
    private void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = mObjectDistance;
        mObjectDistance = (float) Math.random() * 15 + 5;
        mObjectCurrentDistance = mObjectDistance;
        float objectScalingFactor = mObjectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, mModelCube, 12);

        // Now get the up or down angle, between -20 and 20 degrees
        float angleY = (float) Math.random() * 80 - 40; // angle in Y plane, between -40 and 40
        angleY = (float) Math.toRadians(angleY);
        float newY = (float)Math.tan(angleY) * mObjectDistance;

        Matrix.setIdentityM(mModelCube, 0);
        Matrix.translateM(mModelCube, 0, posVec[0], newY, posVec[2]);
    }

    /**
     * Check if user is looking at object by calculating where the object is in eye-space.
     * @return
     */
    private boolean isLookingAtObject() {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(mModelView, 0, mHeadView, 0, mModelCube, 0);
        Matrix.multiplyMV(objPositionVec, 0, mModelView, 0, initVec, 0);

        float pitch = (float)Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float)Math.atan2(objPositionVec[0], -objPositionVec[2]);

        Log.i(TAG, "Object position: X: " + objPositionVec[0]
                + "  Y: " + objPositionVec[1] + " Z: " + objPositionVec[2]);
        Log.i(TAG, "Object Pitch: " + pitch +"  Yaw: " + yaw);

        return (Math.abs(pitch) < PITCH_LIMIT) && (Math.abs(yaw) < YAW_LIMIT);
    }
}

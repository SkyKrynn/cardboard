package com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper;

import android.opengl.GLES20;
import android.util.Log;

public class GLError {
    public static void check(String tag, String func) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, func + ": glError " + error);
            throw new RuntimeException(func + ": glError " + error);
        }
    }
}

package com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper;

import android.opengl.GLES20;
import android.util.Log;

public class GLShader {
    private static final String TAG = "GLShader";

    private int shaderId;

    public GLShader(int type, String source)
    {
        shaderId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderId, source);
        GLES20.glCompileShader(shaderId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderId));
            GLES20.glDeleteShader(shaderId);
            shaderId = 0;
        }

        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader.");
        }
    }

    public int getShaderId() { return shaderId; }
}

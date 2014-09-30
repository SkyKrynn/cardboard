package com.google.vrtoolkit.cardboard.samples.treasurehunt.Models;

import com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper.DataBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brsmith on 9/25/2014.
 */
public class ModelData {
    private DataBuffer mVertices;
    private DataBuffer mNormals;
    private Map colorMap = new HashMap();

    public ModelData(DataBuffer vertices, DataBuffer normals) {
        mVertices = vertices;
        mNormals = normals;
    }

    public void addColorBuffer(String bufferName, DataBuffer colorBuffer) {
        colorMap.put(bufferName, colorBuffer);
    }

    public DataBuffer getColorBuffer(String bufferName) {
        return (DataBuffer)colorMap.get(bufferName);
    }

    public DataBuffer getVerticesBuffer() { return mVertices; }
    public int getNumVertices() { return mVertices.getNumUnits(); }
    public DataBuffer getNormalsBuffer() { return mNormals; }
}

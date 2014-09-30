package com.google.vrtoolkit.cardboard.samples.treasurehunt.GLESHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DataBuffer {
    FloatBuffer dataBuffer;
    int unitSize;
    int dataLength;
    int numUnits;

    public DataBuffer(float[] data, int unitSize) {
        setDataPoints(data, unitSize);
    }

    private void setDataPoints(float[] data, int unitSize) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        dataBuffer = buffer.asFloatBuffer();
        dataBuffer.put(data);
        dataBuffer.position(0);

        dataLength = data.length;
        this.unitSize = unitSize;
        numUnits = dataLength / unitSize;
    }

    public FloatBuffer getBuffer() { return dataBuffer; }
    public int getUnitSize() { return unitSize; }
    public int getNumUnits() { return numUnits; }
}

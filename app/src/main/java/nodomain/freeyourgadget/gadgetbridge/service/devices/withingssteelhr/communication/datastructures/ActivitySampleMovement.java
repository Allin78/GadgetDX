package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleMovement extends WithingsStructure {

    private short steps;
    private int distance;
    private int asc;
    private int desc;

    public short getSteps() {
        return steps;
    }

    public void setSteps(short steps) {
        this.steps = steps;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public short getLength() {
        return 18;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putShort(steps);
        buffer.putInt(distance);
        buffer.putInt(asc);
        buffer.putInt(desc);
    }

    @Override
    public void fillFromRawData(byte[] rawData) {
        if (rawData.length < 14) {
            throw new IllegalArgumentException();
        }

        steps = (short)BLETypeConversions.toInt16(rawData[1], rawData[0]);
        distance = BLETypeConversions.toInt16(rawData[5], rawData[4], rawData[3], rawData[2]);
        asc = BLETypeConversions.toInt16(rawData[9], rawData[8], rawData[7], rawData[6]);
        desc = BLETypeConversions.toInt16(rawData[13], rawData[12], rawData[11], rawData[10]);
    }

    @Override
    short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_MOVEMENT;
    }
}

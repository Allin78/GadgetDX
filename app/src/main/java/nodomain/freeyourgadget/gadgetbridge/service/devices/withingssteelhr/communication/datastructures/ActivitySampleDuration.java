package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleDuration extends WithingsStructure {

    private short duration;

    public short getDuration() {
        return duration;
    }

    @Override
    public short getLength() {
        return 6;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawData(byte[] rawData) {
        if (rawData.length < 2) {
            throw new IllegalArgumentException();
        }

        duration = (short) BLETypeConversions.toInt16(rawData[1],rawData[0]);
    }


    @Override
    short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_DURATION;
    }
}

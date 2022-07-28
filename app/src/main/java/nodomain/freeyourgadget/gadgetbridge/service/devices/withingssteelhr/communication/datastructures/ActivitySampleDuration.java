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
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        duration = rawDataBuffer.getShort();
    }


    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_DURATION;
    }
}

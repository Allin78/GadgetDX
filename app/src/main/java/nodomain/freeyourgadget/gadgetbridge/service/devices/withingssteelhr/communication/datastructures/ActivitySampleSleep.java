package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleSleep extends WithingsStructure {

    private short sleepType;

    public short getSleepType() {
        return sleepType;
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
        sleepType = rawDataBuffer.getShort();
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_SLEEP;
    }
}

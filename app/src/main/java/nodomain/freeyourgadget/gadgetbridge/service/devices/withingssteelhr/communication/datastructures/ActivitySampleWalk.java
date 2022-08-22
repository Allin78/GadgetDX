package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleWalk extends WithingsStructure {

    private short level;

    public short getLevel() {
        return level;
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
        level = (short) (rawDataBuffer.getShort() & 65535);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_WALK;
    }
}

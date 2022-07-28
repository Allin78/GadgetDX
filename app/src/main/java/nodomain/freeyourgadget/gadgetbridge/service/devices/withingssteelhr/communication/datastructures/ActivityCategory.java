package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivityCategory extends WithingsStructure {

    public static short RUNNING = 0;

    private short category;

    public short getCategory() {
        return category;
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
        category = rawDataBuffer.getShort();
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_CATEGORY;
    }
}

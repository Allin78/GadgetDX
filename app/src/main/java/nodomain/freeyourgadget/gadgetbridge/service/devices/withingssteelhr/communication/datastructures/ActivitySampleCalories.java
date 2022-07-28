package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleCalories extends WithingsStructure {

    private int calories;
    private int yetUnknown;

    public int getCalories() {
        return calories;
    }

    public int getYetUnknown() {
        return yetUnknown;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        calories = rawDataBuffer.getShort() & 65535;
        yetUnknown = rawDataBuffer.getShort() & 65535;
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_CALORIES;
    }
}

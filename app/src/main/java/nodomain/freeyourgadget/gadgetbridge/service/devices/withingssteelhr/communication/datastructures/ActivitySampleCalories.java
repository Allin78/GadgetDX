package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivitySampleCalories extends WithingsStructure {

    private int calories;
    private int met;

    public int getCalories() {
        return calories;
    }

    public int getMet() {
        return met;
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
        met = rawDataBuffer.getShort() & 65535;
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_CALORIES;
    }
}

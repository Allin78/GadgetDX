package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivitySampleUnknown extends WithingsStructure {
    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_UNKNOWN;
    }
}

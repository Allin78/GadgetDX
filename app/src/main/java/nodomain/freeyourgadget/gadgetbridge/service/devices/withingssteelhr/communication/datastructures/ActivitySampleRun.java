package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivitySampleRun extends WithingsStructure {
    @Override
    public short getLength() {
        return 0;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_RUN;
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class SleepActivityRequest extends WithingsStructure {

    private int startTime;

    public SleepActivityRequest(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putInt(startTime);
    }

    @Override
    public short getType() {
        return WithingsStructureType.SLEEP_ACTIVITY_REQUEST;
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivityTarget extends WithingsStructure {

    private long targetCount;

    public ActivityTarget(long targetCount) {
        this.targetCount = targetCount;
    }

    public long getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(long targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public short getLength() {
        return 12;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
        rawDataBuffer.putLong(targetCount);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_TARGET;
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class GetActivitySamples extends WithingsStructure {

    public long timestampFrom;

    public short maxSampleCount;

    public GetActivitySamples(long timestampFrom, short maxSampleCount) {
        this.timestampFrom = timestampFrom;
        this.maxSampleCount = maxSampleCount;
    }

    @Override
    public short getLength() {
        return 10;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putInt((int)(timestampFrom & 4294967295L));
        buffer.putShort((short)maxSampleCount);
    }

    @Override
    short getType() {
        return WithingsStructureType.GET_ACTIVITY_SAMPLES;
    }
}

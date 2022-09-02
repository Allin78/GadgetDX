package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivityEnd extends WithingsStructure {

    private int endtime;

    public int getEndtime() {
        return endtime;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        endtime = rawDataBuffer.getInt();
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_END;
    }
}

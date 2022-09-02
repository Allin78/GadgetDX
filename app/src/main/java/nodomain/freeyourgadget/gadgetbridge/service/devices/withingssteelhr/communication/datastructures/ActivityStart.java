package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivityStart extends WithingsStructure {

    private int starttime;

    public int getStarttime() {
        return starttime;
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
        starttime = rawDataBuffer.getInt();
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_START;
    }
}

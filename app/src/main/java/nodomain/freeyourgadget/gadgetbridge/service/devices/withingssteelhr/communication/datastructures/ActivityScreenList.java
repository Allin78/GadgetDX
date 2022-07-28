package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivityScreenList extends WithingsStructure {

    @Override
    public short getLength() {
        return 37;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        super.fillFromRawDataAsBuffer(rawDataBuffer);
    }

    @Override
    public short getType() {
        return WithingsStructureType.GET_ACTIVITY_SCREEN_LIST;
    }
}

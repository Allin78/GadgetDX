package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class HeartRate extends WithingsStructure {

    private int heartrate;

    public int getHeartrate() {
        return heartrate;
    }

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        heartrate = rawDataBuffer.get(1) & 0xff;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
    }

    @Override
    public short getType() {
        return WithingsStructureType.HR;
    }
}

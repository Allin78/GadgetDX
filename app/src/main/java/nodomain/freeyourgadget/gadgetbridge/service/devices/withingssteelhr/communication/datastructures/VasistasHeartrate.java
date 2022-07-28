package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class VasistasHeartrate extends WithingsStructure
{
    private int heartrate;

    public int getHeartrate() {
        return heartrate;
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
        heartrate = rawDataBuffer.get(0) & 0xff;
    }

    @Override
    public short getType() {
        return WithingsStructureType.VASISTAS_HR;
    }
}

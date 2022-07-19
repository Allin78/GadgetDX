package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class LiveHeartRate extends WithingsStructure {

    private int heartrate;

    public int getHeartrate() {
        return heartrate;
    }

    @Override
    public short getLength() {
        return 1;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawData(byte[] rawData) {
        if (rawData.length != 1) {
            throw new IllegalArgumentException();
        }

        heartrate = rawData[0] & 0xff;
    }

    @Override
    short getType() {
        return WithingsStructureType.LIVE_HR;
    }
}

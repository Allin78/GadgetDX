package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

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
    public void fillFromRawData(byte[] rawData) {
        heartrate = rawData[1] & 0xff;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
    }

    @Override
    short getType() {
        return WithingsStructureType.HR;
    }
}

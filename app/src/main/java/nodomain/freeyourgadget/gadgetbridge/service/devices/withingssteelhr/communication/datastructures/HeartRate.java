package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class HeartRate extends WithingsStructure {

    private byte heartrate = 0x01;

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    public void fillFromRawData(byte[] rawData) {

    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putShort((short) 1);
        buffer.put(heartrate);
    }

    @Override
    short getType() {
        return WithingsStructureType.HR;
    }
}

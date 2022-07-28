package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class Status extends WithingsStructure {
    @Override
    public short getLength() {
        return 5;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put((byte) 0x01);
    }

    @Override
    public short getType() {
        return (short) 2420;
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class TypeVersion extends WithingsStructure {

    private byte version = 0x01;

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(version);
    }

    @Override
    public short getType() {
        return 2401;
    }
}

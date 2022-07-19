package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class UserUnit extends WithingsStructure {

    private byte unknown1 = 0;
    private byte unknown2 = 0;
    private short type;
    private short unit;

    public UserUnit() {}

    public UserUnit(short type, short unit) {
        this.type = type;
        this.unit = unit;
    }

    @Override
    public short getLength() {
        return 10;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(unknown1);
        buffer.put(unknown2);
        buffer.putShort(type);
        buffer.putShort(unit);
    }

    @Override
    short getType() {
        return 0;
    }
}

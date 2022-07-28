package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class AncsStatus extends WithingsStructure {

    private boolean isOn;

    public AncsStatus() {}

    public AncsStatus(boolean isOn) {
        this.isOn = isOn;
    }

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(isOn? (byte)0x01 : 0x00);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ANCS_STATUS;
    }
}

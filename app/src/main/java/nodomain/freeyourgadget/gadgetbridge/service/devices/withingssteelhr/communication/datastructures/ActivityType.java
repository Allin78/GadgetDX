package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivityType extends WithingsStructure {

    private int type;

    public ActivityType(int type) {
        this.type = type;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putInt(type);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_TYPE;
    }
}

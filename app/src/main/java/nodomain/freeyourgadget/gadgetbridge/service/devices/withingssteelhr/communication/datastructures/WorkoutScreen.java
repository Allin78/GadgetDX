package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class WorkoutScreen extends WithingsStructure {
    @Override
    public short getType() {
        return WithingsStructureType.SCREEN_SETTINGS;
    }

    @Override
    public short getLength() {
        return 0;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
    }
}

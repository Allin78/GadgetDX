package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class WorkoutType extends WithingsStructure {

    public static short RUNNING = 0;

    private short activityType;

    public short getActivityType() {
        return activityType;
    }

    @Override
    public short getLength() {
        return 6;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        activityType = rawDataBuffer.getShort();
    }

    @Override
    public short getType() {
        return WithingsStructureType.WORKOUT_TYPE;
    }
}

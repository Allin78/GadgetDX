package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class WorkoutScreenList extends WithingsStructure {

    private int[] workoutIds;

    public int[] getWorkoutIds() {
        return workoutIds;
    }

    @Override
    public short getLength() {
        return 37;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        workoutIds = getNextIntArray(rawDataBuffer);
    }

    @Override
    public short getType() {
        return WithingsStructureType.WORKOUT_SCREEN_LIST;
    }
}

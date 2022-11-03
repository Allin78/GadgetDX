package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class WorkoutGpsState extends WithingsStructure {

    private final boolean gpsEnabled;

    public WorkoutGpsState(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    @Override
    public short getLength() {
        return 6;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putShort(gpsEnabled? (short)1 : 0);
    }

    @Override
    public short getType() {
        return WithingsStructureType.WORKOUT_GPS_STATE;
    }
}

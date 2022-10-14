package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Date;

public class LiveWorkoutStart extends WithingsStructure {

    private Date starttime;

    public Date getStarttime() {
        return starttime;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        long timestampInSeconds = rawDataBuffer.getInt() & 4294967295L;
        starttime = new Date(timestampInSeconds * 1000);
    }

    @Override
    public short getType() {
        return WithingsStructureType.LIVE_WORKOUT_START;
    }
}

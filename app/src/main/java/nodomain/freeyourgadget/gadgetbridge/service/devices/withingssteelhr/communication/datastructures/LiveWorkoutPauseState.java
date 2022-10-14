package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Date;

public class LiveWorkoutPauseState extends WithingsStructure {

    private byte yetunknown;

    // Is always the same as long as the actual pause continues:
    private Date starttime;

    // This is just a guess, but observation show that this is quite possible the meaning of this value that is send when the pause is over
    private int lengthInSeconds;

    public Date getStarttime() {
        return starttime;
    }

    public int getLengthInSeconds() {
        return lengthInSeconds;
    }

    @Override
    public short getLength() {
        return 13;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        yetunknown = rawDataBuffer.get();

        long timestampInSeconds = rawDataBuffer.getInt() & 4294967295L;
        if (timestampInSeconds > 0) {
            starttime = new Date(timestampInSeconds * 1000);
        }

        lengthInSeconds = rawDataBuffer.getInt();
    }

    @Override
    public short getType() {
        return WithingsStructureType.LIVE_WORKOUT_PAUSE_STATE;
    }
}

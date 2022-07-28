package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class SleepActivitySample extends WithingsStructure {

    public int startdate;

    public int enddate;

    public int awake;

    public int sleepLight;

    public int sleepDeep;

    public int sleepRem;

    public int wakeupCount;

    public int durationToSleep;

    public int completed;

    public int getStartdate() {
        return startdate;
    }

    public int getEnddate() {
        return enddate;
    }

    public int getAwake() {
        return awake;
    }

    public int getSleepLight() {
        return sleepLight;
    }

    public int getSleepDeep() {
        return sleepDeep;
    }

    public int getSleepRem() {
        return sleepRem;
    }

    public int getWakeupCount() {
        return wakeupCount;
    }

    public int getDurationToSleep() {
        return durationToSleep;
    }

    public int getCompleted() {
        return completed;
    }

    @Override
    public short getLength() {
        return 40;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        this.startdate = rawDataBuffer.getInt();
        this.enddate = rawDataBuffer.getInt();
        this.awake = rawDataBuffer.getInt();
        this.sleepLight = rawDataBuffer.getInt();
        this.sleepDeep = rawDataBuffer.getInt();
        this.sleepRem = rawDataBuffer.getInt();
        this.wakeupCount = rawDataBuffer.getInt();
        this.durationToSleep = rawDataBuffer.getInt();
        this.completed = rawDataBuffer.getInt();
    }

    @Override
    public short getType() {
        return WithingsStructureType.SLEEP_ACTIVITY_SAMPLE;
    }
}

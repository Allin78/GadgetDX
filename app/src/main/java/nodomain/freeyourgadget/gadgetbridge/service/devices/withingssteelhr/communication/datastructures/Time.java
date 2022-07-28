package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.zone.ZoneOffsetTransition;
import org.threeten.bp.zone.ZoneRules;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;

import ch.qos.logback.core.encoder.ByteArrayUtil;

public class Time extends WithingsStructure {

    private Instant now;
    private int timeOffsetInSeconds;
    private Instant nextDaylightSavingTransition;
    private int nextDaylightSavingTransitionOffsetInSeconds;

    public Time() {
        now = Instant.now();
        final TimeZone timezone = TimeZone.getDefault();
        final ZoneId zoneId = ZoneId.systemDefault();
        final ZoneRules zoneRules = zoneId.getRules();
        final ZoneOffsetTransition nextTransition = zoneRules.nextTransition(Instant.now());
        long nextTransitionTs = 0;
        if (nextTransition != null) {
            nextTransitionTs = nextTransition.getDateTimeBefore().atZone(zoneId).toEpochSecond();
            nextDaylightSavingTransitionOffsetInSeconds = nextTransition.getOffsetAfter().getTotalSeconds();
        }

        timeOffsetInSeconds = timezone.getRawOffset() / 1000;
        nextDaylightSavingTransition = Instant.ofEpochMilli(nextTransitionTs);
    }

    public Instant getNow() {
        return now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public int getTimeOffsetInSeconds() {
        return timeOffsetInSeconds;
    }

    public void setTimeOffsetInSeconds(int TimeOffsetInSeconds) {
        this.timeOffsetInSeconds = TimeOffsetInSeconds;
    }

    public Instant getNextDaylightSavingTransition() {
        return nextDaylightSavingTransition;
    }

    public void setNextDaylightSavingTransition(Instant nextDaylightSavingTransition) {
        this.nextDaylightSavingTransition = nextDaylightSavingTransition;
    }

    public int getNextDaylightSavingTransitionOffsetInSeconds() {
        return nextDaylightSavingTransitionOffsetInSeconds;
    }

    public void setNextDaylightSavingTransitionOffsetInSeconds(int nextDaylightSavingTransitionOffsetInSeconds) {
        this.nextDaylightSavingTransitionOffsetInSeconds = nextDaylightSavingTransitionOffsetInSeconds;
    }

    @Override
    public short getType() {
        return WithingsStructureType.TIME;
    }

    @Override
    public short getLength() {
        return 20;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
        rawDataBuffer.putInt((int)now.getEpochSecond());
        rawDataBuffer.putInt(timeOffsetInSeconds);
        rawDataBuffer.putInt((int)nextDaylightSavingTransition.getEpochSecond());
        rawDataBuffer.putInt(nextDaylightSavingTransitionOffsetInSeconds);
    }
}

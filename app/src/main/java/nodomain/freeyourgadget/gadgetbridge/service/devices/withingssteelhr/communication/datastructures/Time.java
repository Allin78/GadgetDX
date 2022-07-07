package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.util.Date;
import java.util.TimeZone;

import ch.qos.logback.core.encoder.ByteArrayUtil;

public class Time extends WithingsStructure {

    private LocalDateTime now;
    private int timeOffsetInSeconds;
    private LocalDateTime nextDaylightSavingTransition;
    private int nextDaylightSavingTransitionOffsetInSeconds;

    public Time() {
        now = LocalDateTime.now();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        ZoneOffset offset = defaultZoneId.getRules().getOffset(now);
        timeOffsetInSeconds = offset.getTotalSeconds();
        ZoneOffsetTransition transition = defaultZoneId.getRules().nextTransition(ZonedDateTime.now(defaultZoneId).toInstant());
        nextDaylightSavingTransition = transition.getDateTimeBefore();
        nextDaylightSavingTransitionOffsetInSeconds = transition.getOffsetBefore().getTotalSeconds();
    }

    public LocalDateTime getNow() {
        return now;
    }

    public void setNow(LocalDateTime now) {
        this.now = now;
    }

    public int getTimeOffsetInSeconds() {
        return timeOffsetInSeconds;
    }

    public void setTimeOffsetInSeconds(int TimeOffsetInSeconds) {
        this.timeOffsetInSeconds = TimeOffsetInSeconds;
    }

    public LocalDateTime getNextDaylightSavingTransition() {
        return nextDaylightSavingTransition;
    }

    public void setNextDaylightSavingTransition(LocalDateTime nextDaylightSavingTransition) {
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
        rawDataBuffer.putInt((int)getUnixTimeInSeconds(now));
        rawDataBuffer.putInt(timeOffsetInSeconds);
        rawDataBuffer.putInt(getUnixTimeInSeconds(nextDaylightSavingTransition));
        rawDataBuffer.putInt(nextDaylightSavingTransitionOffsetInSeconds);
    }

    private int getUnixTimeInSeconds(LocalDateTime date) {
        long unixTimeInSeconds = 0;
        if (date != null) {
            ZoneId zoneId = ZoneId.systemDefault();
            unixTimeInSeconds = date.atZone(zoneId).toEpochSecond();
        }

        // This loss in precision might be a problem around 2038, but at the moment
        // the Steel HR does onl understand 32 bit timestamps :-(
        return ((int)unixTimeInSeconds);
    }
}

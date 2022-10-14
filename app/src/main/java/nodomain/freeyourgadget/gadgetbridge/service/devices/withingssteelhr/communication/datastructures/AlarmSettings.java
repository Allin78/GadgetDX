package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class AlarmSettings extends WithingsStructure {
    private short hour;
    private short minute;
    private short dayOfWeek;
    private short dayOfMonth;
    private short month;
    private short year;

    // TODO: find out what this field is used for.
    private short smartWakeupMinutes;

    public short getHour() {
        return hour;
    }

    public void setHour(short hour) {
        this.hour = hour;
    }

    public short getMinute() {
        return minute;
    }

    public void setMinute(short minute) {
        this.minute = minute;
    }

    public short getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(short dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public short getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(short dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public short getMonth() {
        return month;
    }

    public void setMonth(short month) {
        this.month = month;
    }

    public short getYear() {
        return year;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public short getYetUnkown() {
        return smartWakeupMinutes;
    }

    public void setSmartWakeupMinutes(short smartWakeupMinutes) {
        this.smartWakeupMinutes = smartWakeupMinutes;
    }

    @Override
    public short getLength() {
        return 11;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put((byte)hour);
        buffer.put((byte)minute);
        buffer.put((byte)dayOfWeek);
        buffer.put((byte)dayOfMonth);
        buffer.put((byte)month);
        buffer.put((byte)year);
        buffer.put((byte)smartWakeupMinutes);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ALARM;
    }

    @Override
    public boolean withEndOfMessage() {
        return true;
    }
}

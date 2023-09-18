package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.calendar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;

public class CalendarEntry implements Wena3Packetable {
    public Date begin;
    public Date end;
    public boolean isAllDay;
    public String title;
    public String location;
    /// 1-based, not index!
    public byte position;
    public byte totalItemCount;

    public CalendarEntry(Date begin, Date end, boolean isAllDay, String title, String location, byte position, byte totalItemCount) {
        this.begin = begin;
        this.end = end;
        this.isAllDay = isAllDay;
        this.title = title;
        this.location = location;
        this.position = position;
        this.totalItemCount = totalItemCount;
    }

    @Override
    public byte[] toByteArray() {
        byte[] cstrTitle = title.getBytes(StandardCharsets.UTF_8);
        byte[] cstrLocation = location.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(14 + cstrTitle.length + cstrLocation.length)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x02)
                .put(position)
                .put(totalItemCount)
                .put((byte) cstrTitle.length)
                .put((byte) cstrLocation.length)
                .put((byte) (isAllDay ? 0x1 : 0x0))
                .putInt(TimeUtil.dateToWenaTime(begin))
                .putInt(TimeUtil.dateToWenaTime(end))
                .put(cstrTitle)
                .put(cstrLocation)
                .array();

    }

    public static byte[] byteArrayForEmptyEvent(byte position, byte totalItemCount) {
        return ByteBuffer.allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x02)
                .put(position)
                .put(totalItemCount)
                .put((byte) 0x0)
                .put((byte) 0x0)
                .put((byte) 0x0)
                .array();

    }
}

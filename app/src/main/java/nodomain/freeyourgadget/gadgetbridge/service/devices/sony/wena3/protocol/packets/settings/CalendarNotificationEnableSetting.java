package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class CalendarNotificationEnableSetting implements Wena3Packetable {
    public boolean enableCalendar;
    public boolean enableNotifications;

    public CalendarNotificationEnableSetting(boolean enableCalendar, boolean enableNotifications) {
        this.enableCalendar = enableCalendar;
        this.enableNotifications = enableNotifications;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[] {
                0x12,
                (byte) (enableNotifications ? 0x1 : 0x0),
                (byte) (enableCalendar ? 0x1 : 0x0)
        };
    }
}

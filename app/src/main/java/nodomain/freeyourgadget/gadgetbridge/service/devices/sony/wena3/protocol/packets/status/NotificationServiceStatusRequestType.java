package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

public enum NotificationServiceStatusRequestType {
    LOCATE_PHONE(2),
    MUSIC_INFO_FETCH(11);

    public final int value;
    NotificationServiceStatusRequestType(int val) {
        this.value = val;
    }
}

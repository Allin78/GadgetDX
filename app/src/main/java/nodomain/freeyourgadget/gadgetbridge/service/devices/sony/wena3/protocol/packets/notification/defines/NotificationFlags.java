package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines;

public class NotificationFlags {
    public static NotificationFlags NONE = new NotificationFlags(0);
    public static NotificationFlags IS_TEST = new NotificationFlags(1);
    public static NotificationFlags IS_RETRANSMISSION = new NotificationFlags(2);
    public static NotificationFlags HAS_ACTION = new NotificationFlags(4);

    public int value = 0;
    public NotificationFlags(int value) {
        this.value = value;
    }

    public NotificationFlags() {
        this.value = 0;
    }

    public NotificationFlags set(NotificationFlags flag) {
        this.value |= flag.value;
        return this;
    }

    public NotificationFlags unset(NotificationFlags flag) {
        this.value &= ~flag.value;
        return this;
    }
}

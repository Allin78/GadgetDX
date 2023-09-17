package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines;

public class MenuIconId {
    public static final MenuIconId TIMER = new MenuIconId(1);
    public static final MenuIconId ALARM = new MenuIconId(2);
    public static final MenuIconId FIND_PHONE = new MenuIconId(3);
    public static final MenuIconId ALEXA = new MenuIconId(4);
    public static final MenuIconId PAYMENT = new MenuIconId(5);
    public static final MenuIconId QRIO = new MenuIconId(6);
    public static final MenuIconId WEATHER = new MenuIconId(7);
    public static final MenuIconId MUSIC = new MenuIconId(8);
    public static final MenuIconId CAMERA = new MenuIconId(9);

    public byte value;

    public MenuIconId(int value) {
        this.value = (byte) value;
    }
}

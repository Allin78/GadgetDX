package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines;

public class StatusPageId {
    // TODO: List enumerations? They seemed to be of no use in HomeIconId etc...
    public byte value;

    public StatusPageId(int val) {
        this.value = (byte) val;
    }
}

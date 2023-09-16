package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

public class DeviceStateInfo {
    public int batteryPercentage;

    public DeviceStateInfo(byte[] packet) {
        this.batteryPercentage = Integer.valueOf(packet[0]);
    }
}

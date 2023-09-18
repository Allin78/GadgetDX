package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

public class BatteryLevelInfo {
    public int batteryPercentage;

    public BatteryLevelInfo(byte[] packet) {
        this.batteryPercentage = Integer.valueOf(packet[0]);
    }
}

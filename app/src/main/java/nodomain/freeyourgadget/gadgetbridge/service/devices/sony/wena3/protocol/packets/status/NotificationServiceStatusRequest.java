package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

public class NotificationServiceStatusRequest {
    public int requestType;
    public int unknown;

    public NotificationServiceStatusRequest(byte[] packet) {
        this.requestType = Integer.valueOf(packet[0]);
    }
}

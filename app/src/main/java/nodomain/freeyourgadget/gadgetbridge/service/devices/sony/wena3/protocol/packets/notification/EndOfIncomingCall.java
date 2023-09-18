package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class EndOfIncomingCall implements Wena3Packetable {
    @Override
    public byte[] toByteArray() {
        return new byte[] { 0x1 };
    }
}

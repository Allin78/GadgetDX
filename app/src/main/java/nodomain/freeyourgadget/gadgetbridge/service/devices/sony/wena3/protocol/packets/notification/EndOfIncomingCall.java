package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

// NB: This seems to not work on the Wena 3!
// For me, the vendor's app just sends an "APP" notification for incoming calls as well,
// just with a continuous vibration...
@Deprecated
public class EndOfIncomingCall implements Wena3Packetable {
    @Override
    public byte[] toByteArray() {
        return new byte[] { 0x1 };
    }
}

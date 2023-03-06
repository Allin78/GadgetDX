package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.ExpectedResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageType;

public class SyncRequestHandler implements IncomingMessageHandler {

    private final WithingsSteelHRDeviceSupport support;

    public SyncRequestHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    @Override
    public void handleMessage(Message message) {
        support.sendToDevice(new WithingsMessage(WithingsMessageType.SYNC_RESPONSE));
        support.doSync();
    }
}

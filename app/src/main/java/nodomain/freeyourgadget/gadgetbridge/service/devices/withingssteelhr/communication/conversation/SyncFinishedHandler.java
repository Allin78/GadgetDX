package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;

public class SyncFinishedHandler extends AbstractResponseHandler {

    public SyncFinishedHandler(WithingsSteelHRDeviceSupport support) {
        super(support);
    }



    @Override
    public void handleResponse(Message response) {
        if (response.getType() == WithingsMessageTypes.SYNC_OK) {
            support.finishSync();
        }
    }
}

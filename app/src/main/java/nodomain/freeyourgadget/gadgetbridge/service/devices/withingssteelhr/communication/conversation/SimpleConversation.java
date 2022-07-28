package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public class SimpleConversation extends AbstractConversation {

    public SimpleConversation(ResponseHandler responseHandler) {
        super(responseHandler);
    }

    public SimpleConversation() {
        super(null);
    }

    @Override
    protected void doSendRequest(Message message) {
        // Do nothing
    }

    @Override
    protected void doHandleResponse(Message message) {
        if (responseHandler != null) {
            responseHandler.handleResponse(message);
        }
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public interface Conversation {
    void registerObserver(ConversationObserver observer);
    void removeObserver(ConversationObserver observer);
    void setRequest(Message message);
    Message getRequest();
    void handleResponse(Message mesage);
    boolean isComplete();
}

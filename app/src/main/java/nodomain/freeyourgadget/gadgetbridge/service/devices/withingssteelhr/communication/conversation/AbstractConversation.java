package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public abstract class AbstractConversation implements Conversation {

    private List<ConversationObserver> observers = new ArrayList();

    private boolean complete;

    protected Message request;

    private short requestType;

    protected ResponseHandler responseHandler;

    public AbstractConversation(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void registerObserver(ConversationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ConversationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void setRequest(Message message) {
        this.request = message;
        this.requestType = message.getType();
    }

    @Override
    public Message getRequest() {
        return request;
    }

    @Override
    public void handleResponse(Message response) {
        if (response.getType() == requestType) {
            if (request.needsResponse()) {
                complete = true;
            } else if (request.needsEOT()) {
                complete = hasEOT(response);
            }

            doHandleResponse(response);
            if (complete) {
                notifyObservers(requestType);
            }
        }
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    protected void notifyObservers(short messageType) {
        for (ConversationObserver observer : observers) {
            observer.onConversationCompleted(messageType);
        }
    }

    private boolean hasEOT(Message message) {
        List<WithingsStructure> dataList = message.getDataStructures();
        if (dataList != null) {
            for (WithingsStructure strucuter :
                    dataList) {
                if (strucuter.getType() == WithingsStructureType.END_OF_TRANSMISSION) {
                    return true;
                }
            }
        }

        return false;
    }

    protected abstract void doSendRequest(Message message);

    protected abstract void doHandleResponse(Message message);
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import android.bluetooth.BluetoothGattCharacteristic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class ConversationQueue implements ConversationObserver
{
    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);
    private final LinkedList<Conversation> queue = new LinkedList<>();
    private WithingsSteelHRDeviceSupport support;

    public ConversationQueue(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    @Override
    public void onConversationCompleted(short conversationType) {
        queue.remove(getConversation(conversationType));
        send();
    }

    public void clear() {
        queue.clear();
    }

    public void send() {
        if (!queue.isEmpty()) {
            Conversation nextInLine = queue.peek();
            if (nextInLine!= null) {
                Message request = nextInLine.getRequest();
                sendToDevice(request);
            }
        }
    }

    public void addConversation(Conversation conversation) {
        if (conversation == null) {
            return;
        }

        if (conversation.getRequest().needsResponse() || conversation.getRequest().needsEOT()) {
            queue.add(conversation);
            conversation.registerObserver(this);
        } else {
            sendToDevice(conversation.getRequest());
        }
    }

    public void processResponse(Message response) {
        Conversation conversation = getConversation(response.getType());
        if (conversation != null) {
            conversation.handleResponse(response);
        }
    }

    private void sendToDevice(Message message) {
        if (message == null) {
            return;
        }

        TransactionBuilder builder = support.createTransactionBuilder("conversation");
        builder.setGattCallback(support);
        BluetoothGattCharacteristic characteristic = support.getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        byte[] rawData = message.getRawData();
        if (rawData.length > 110) {
            builder.write(characteristic, Arrays.copyOfRange(rawData, 0 , 110));
            builder.write(characteristic, Arrays.copyOfRange(rawData, 110 , rawData.length ));
        } else {
            builder.write(characteristic, rawData);
        }
        builder.queue(support.getQueue());
    }

    private Conversation getConversation(short requestType) {
        for (Conversation conversation : queue) {
            if (conversation.getRequest() != null && conversation.getRequest().getType() == requestType) {
                return conversation;
            }
        }

        return null;
    }
}

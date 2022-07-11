package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public class ConversationQueue {

    private static final Queue<Message> queue = new LinkedBlockingQueue<>();

    private WithingsSteelHRDeviceSupport support;

    public ConversationQueue(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    public void send() {
        if (!queue.isEmpty()) {
            Message firstMessage = queue.peek();
            sendToDevice(firstMessage);
        }
    }

    public void addMessage(Message message) {
        if (message == null) {
            return;
        }

        if (message.needsResponse()) {
            queue.add(message);
        } else {
            sendToDevice(message);
        }
    }

    public void onResponseReceived(short responsetype) {
        Message messageWaitingForResponse = queue.peek();
        if (messageWaitingForResponse != null && messageWaitingForResponse.getType() == responsetype) {
            queue.poll();
            Message nextInLine = queue.peek();
            sendToDevice(nextInLine);
        }
    }

    private void sendToDevice(Message message) {
        if (message == null) {
            return;
        }

        TransactionBuilder builder = support.createTransactionBuilder("conversation");
        builder.setGattCallback(support);
        BluetoothGattCharacteristic characteristic = support.getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.write(characteristic, message.getRawData());
        builder.queue(support.getQueue());
    }
}

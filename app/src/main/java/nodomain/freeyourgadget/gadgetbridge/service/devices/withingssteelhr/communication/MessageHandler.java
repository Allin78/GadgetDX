package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication;

import java.nio.ByteBuffer;
import java.util.Arrays;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.BatteryValues;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;

public class MessageHandler {

    private GBDevice device;

    public MessageHandler(GBDevice device) {
        this.device = device;
    }

    public boolean handleMessage(byte[] rawData) {
        Message message = deserializeMessage(rawData);
        if (message.getType() == WithingsMessageTypes.GET_BATTERY_STATUS) {
            BatteryValues battery = (BatteryValues) message.getDataStructures().get(0);
            device.setBatteryLevel(battery.getPercent());
            switch (battery.getStatus()) {
                case 0:
                    device.setBatteryState(BatteryState.BATTERY_CHARGING);
                    break;
                case 1:
                    device.setBatteryState(BatteryState.BATTERY_LOW);
                    break;
                default:
                    device.setBatteryState(BatteryState.BATTERY_NORMAL);
            }

            device.setBatteryVoltage(battery.getVolt());
        }

        return false;
    }

    public byte[] serializeMessage(Message msg) {
        return null;
    }

    public Message deserializeMessage(byte[] rawMsg) {
        short messageTypeFromResponse = (short) BLETypeConversions.toInt16(rawMsg[2], rawMsg[1]);
        short totalDataLength = (short) BLETypeConversions.toInt16(rawMsg[4], rawMsg[3]);
        Message message = new WithingsMessage(messageTypeFromResponse);
        byte[] rawStructureData = Arrays.copyOfRange(rawMsg, 7, rawMsg.length - 1);
        if (messageTypeFromResponse == WithingsMessageTypes.GET_BATTERY_STATUS) {
            WithingsStructure battery = new BatteryValues();
            battery.fillFromRawData(rawStructureData);
            message.addDataStructure(battery);
        }

        return message;
    }
}

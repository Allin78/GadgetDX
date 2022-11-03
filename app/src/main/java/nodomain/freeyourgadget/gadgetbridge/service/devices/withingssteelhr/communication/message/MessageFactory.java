package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.DataStructureFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;


public class MessageFactory {
    private static final Logger logger = LoggerFactory.getLogger(MessageFactory.class);
    private DataStructureFactory dataStructureFactory;

    public MessageFactory(DataStructureFactory dataStructureFactory) {
        this.dataStructureFactory = new DataStructureFactory();
    }

    public Message createMessageFromRawData(byte[] rawData) {
        if (rawData.length < 5 || rawData[0] != 0x01) {
            return null;
        }

        short messageTypeFromResponse = (short) (BLETypeConversions.toInt16(rawData[2], rawData[1]) & 16383);
        short totalDataLength = (short) BLETypeConversions.toInt16(rawData[4], rawData[3]);
        boolean isIncoming = rawData[1] == 65 || rawData[1] == -127;
        Message message = new WithingsMessage(messageTypeFromResponse, isIncoming);
        byte[] rawStructureData = Arrays.copyOfRange(rawData, 5, rawData.length);
        List<WithingsStructure> structures = dataStructureFactory.createStructuresFromRawData(rawStructureData);
        for (WithingsStructure structure : structures) {
            message.addDataStructure(structure);
        }

        return message;
    }
}

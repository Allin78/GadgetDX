package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class DataStructureFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataStructureFactory.class);
    private static final int HEADER_SIZE = 4;

    public List<WithingsStructure> createStructuresFromRawData(byte[] rawData) {
        List<WithingsStructure> structures = new ArrayList<>();
        if (rawData == null) {
            return structures;
        }

        List<byte[]> rawDataStructures = splitRawData(rawData);
        for (byte[] rawDataStructure : rawDataStructures) {
            WithingsStructure structure = null;

            short structureTypeFromResponse = (short) BLETypeConversions.toInt16(rawDataStructure[1], rawDataStructure[0]);

            switch (structureTypeFromResponse) {
                case WithingsStructureType.HR:
                    structure = new HeartRate();
                    break;
                case WithingsStructureType.LIVE_HR:
                    structure = new HeartRate();
                    break;
                case WithingsStructureType.BATTERY_STATUS:
                    structure = new BatteryValues();
                    break;
                case WithingsStructureType.WORKOUT_ENTRY:
                    structure = new BatteryValues();
                    break;
                case WithingsStructureType.ANCS_STATUS:
                    structure = new AncsStatus();
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_TIME:
                    structure = new ActivitySampleTime();
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_DURATION:
                    structure = new ActivitySampleDuration();
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_MOVEMENT:
                    structure = new ActivitySampleMovement();
                    break;
                default:
                    logger.warn("Received unknown structure type: " + structureTypeFromResponse);
            }

            if (structure != null) {
                structure.fillFromRawData(removeHeaderBytes(rawDataStructure));
                structures.add(structure);
            }
        }

        return structures;
    }

    private List<byte[]> splitRawData(byte[] rawData) {
        int remainingBytes = rawData.length;
        List<byte[]> result = new ArrayList<>();

        while(remainingBytes > 3) {
            short structureLength = (short) BLETypeConversions.toInt16(rawData[3], rawData[2]);
            remainingBytes -= (structureLength + HEADER_SIZE);
            result.add(Arrays.copyOfRange(rawData, 0, structureLength + HEADER_SIZE));
            if (remainingBytes > 0) {
                rawData = Arrays.copyOfRange(rawData, structureLength, rawData.length);
            }
        }

        return result;
    }

    private byte[] removeHeaderBytes(byte[] data) {
        return Arrays.copyOfRange(data, HEADER_SIZE, data.length);
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import static org.junit.Assert.*;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class DataStructureFactoryTest {

    private DataStructureFactory factory2Test;

    @Before
    public void setUp() {
        factory2Test = new DataStructureFactory();
    }

    @Test
    public void testEmptyData() {
        // arrange

        // act
        List<WithingsStructure> result = factory2Test.createStructuresFromRawData(new byte[0]);

        // assert
        assertTrue(result.isEmpty());
    }


    @Test
    public void testNullData() {
        // arrange

        // act
        List<WithingsStructure> result = factory2Test.createStructuresFromRawData(null);

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testOneStructure() {
        // arrange
        String dataString = "0504000a470200000f0100000000";
        byte[] data = Hex.decode(dataString);

        // act
        List<WithingsStructure> result = factory2Test.createStructuresFromRawData(data);

        // assert
        assertEquals(1, result.size());
        BatteryValues batteryValues = (BatteryValues)result.get(0);
        assertEquals(2, batteryValues.getStatus());
        assertEquals(71, batteryValues.getPercent());
        assertEquals(3841, batteryValues.getVolt());
    }

    @Test
    public void testTwoStructures() {
        // arrange
        String dataString = "0504000a470200000f01000000000504000a350100000e0200000000";
        byte[] data = Hex.decode(dataString);

        // act
        List<WithingsStructure> result = factory2Test.createStructuresFromRawData(data);

        // assert
        assertEquals(2, result.size());
        BatteryValues batteryValues = (BatteryValues)result.get(0);
        assertEquals(2, batteryValues.getStatus());
        assertEquals(71, batteryValues.getPercent());
        assertEquals(3841, batteryValues.getVolt());
        batteryValues = (BatteryValues)result.get(0);
        assertEquals(1, batteryValues.getStatus());
        assertEquals(53, batteryValues.getPercent());
        assertEquals(3586, batteryValues.getVolt());
    }

    @Test
    public void testTwoStructuresWithAdditionalBytes() {
        // arrange
        String dataString = "0504000a470200000f01000000000504000a350100000e0200000000abcdef1234";
        byte[] data = Hex.decode(dataString);

        // act
        List<WithingsStructure> result = factory2Test.createStructuresFromRawData(data);

        // assert
        assertEquals(2, result.size());
        BatteryValues batteryValues = (BatteryValues)result.get(0);
        assertEquals(2, batteryValues.getStatus());
        assertEquals(71, batteryValues.getPercent());
        assertEquals(3841, batteryValues.getVolt());
        batteryValues = (BatteryValues)result.get(0);
        assertEquals(1, batteryValues.getStatus());
        assertEquals(53, batteryValues.getPercent());
        assertEquals(3586, batteryValues.getVolt());
    }

}
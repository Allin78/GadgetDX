package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;

import java.time.LocalDateTime;

import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class TimeTest {

    @Test
    public void testGetRawDataWithEmptyValues() {
        // arrange
        Time time = new Time();

        // act
        byte[] rawData = time.getRawData();

        // assert
        assertEquals("0501001000000000000000000000000000000000", StringUtils.bytesToHex(rawData));
    }

    @Test
    public void testGetRawDataWithAllValues() {
        // arrange
        Time time = new Time();
        time.setNow(LocalDateTime.of(2000, 4, 1, 12, 0, 0));
        time.setTimeOffsetInSeconds(7200);
        time.setNextDaylightSavingTransition(LocalDateTime.of(2000, 7, 1, 0, 0, 0));
        time.setNextDaylightSavingTransitionOffsetInSeconds(2400);

        // act
        byte[] rawData = time.getRawData();

        // assert
        assertEquals("0501001038e5c8a000001c20395d186000000960", StringUtils.bytesToHex(rawData).toLowerCase());
    }
}
package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.test.TestHelperUtils;

public class ChallengeTest {

    @Test
    public void testFillFromRawData() {
        // arrange
        byte[] rawData = TestHelperUtils.hexToBytes("1130303a32343a65343a36653a34633a38611082f3d9e121f16a5a3cf0ba94261e8ff6");
        byte[] expectedChallengeBytes = TestHelperUtils.hexToBytes("82f3d9e121f16a5a3cf0ba94261e8ff6");
        Challenge challenge2Test = new Challenge();

        // act
        challenge2Test.fillFromRawData(rawData);

        // assert
        assertEquals("00:24:e4:6e:4c:8a", challenge2Test.getMacAddress());
        assertArrayEquals(expectedChallengeBytes, challenge2Test.getChallenge());
    }

    @Test
    public void testToRawData() {
        // arrange
        Challenge challenge2Test = new Challenge();
        challenge2Test.setMacAddress("00:24:e4:6e:4c:8a");
        challenge2Test.setChallenge(TestHelperUtils.hexToBytes("82f3d9e121f16a5a3cf0ba94261e8ff6"));

        // act
        byte[] result = challenge2Test.getRawData();

        // assert
        assertArrayEquals(TestHelperUtils.hexToBytes("012200231130303a32343a65343a36653a34633a38611082f3d9e121f16a5a3cf0ba94261e8ff6"), result);
    }

    @Test
    public void testToRawDataNoMacAddress() {
        // arrange
        Challenge challenge2Test = new Challenge();
        challenge2Test.setChallenge(TestHelperUtils.hexToBytes("82f3d9e121f16a5a3cf0ba94261e8ff6"));

        // act
        byte[] result = challenge2Test.getRawData();

        // assert
        assertArrayEquals(TestHelperUtils.hexToBytes("01220012001082f3d9e121f16a5a3cf0ba94261e8ff6"), result);
    }

    @Test
    public void testToRawDataNoChallengeBytes() {
        // arrange
        Challenge challenge2Test = new Challenge();
        challenge2Test.setMacAddress("00:24:e4:6e:4c:8a");

        // act
        byte[] result = challenge2Test.getRawData();

        // assert
        assertArrayEquals(TestHelperUtils.hexToBytes("012200131130303a32343a65343a36653a34633a386100"), result);
    }

}
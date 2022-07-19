package nodomain.freeyourgadget.gadgetbridge.test;

import org.mockito.Mock;

public class TestHelperUtils {

    public static byte[] hexToBytes(String str) {
        byte[] val = new byte[str.length() / 2];
        for (int i = 0; i < val.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(str.substring(index, index + 2), 16);
            val[i] = (byte) j;
        }

        return val;
    }
}

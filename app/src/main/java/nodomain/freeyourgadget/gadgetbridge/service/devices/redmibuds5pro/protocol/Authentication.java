package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Authentication {

    private static byte[] step1(byte[] in) {

        byte[] out = new byte[0x110];
        in[0xf] ^= 6;
        System.arraycopy(in, 0, out, 0, in.length);

        List<Byte> input = new ArrayList<>();
        for (byte b : in) {
            input.add(b);
        }
        byte xor = input.stream().reduce((byte) 0x0, (cum, e) -> (byte) (cum ^ e));
        input.add(xor);

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 17; j++) {
                byte rot = (byte) (((input.get(j) & 0xff) >>> 5) | ((input.get(j) & 0xff) << (8 - 5)));
                input.set(j, rot);
            }
            int inIdx = i + 1;
            for (int k = 0; k < 16; k++) {
                byte res = (byte) (AuthData.MAP_1[(i + 1) * 16 - k] + input.get(inIdx));
                out[(i + 1) * 16 + k] = res;
                inIdx += 1;

                if (inIdx > 16) {
                    inIdx = 0;
                }
            }
        }
        return out;
    }

    private static byte[] step2(byte[] out, byte[] in) {
        byte[] outCopy = out.clone();
        int inOffset = 0;
        for (int i = 0; i < 8; i++) {
            if (i == 2) {
                for (int j = 0; j < 16; j++) {
                    byte res;
                    if ((1 << j & 0x9999) != 0) {
                        res = (byte) (out[j] ^ outCopy[j]);
                    } else {
                        res = (byte) (out[j] + outCopy[j]);
                    }
                    out[j] = res;
                }
            }
            for (int j = 0; j < 16; j++) {
                byte res;
                if ((1 << j & 0x9999) != 0) {
                    res = (byte) (out[j] ^ in[inOffset + j]);
                } else {
                    res = (byte) (out[j] + in[inOffset + j]);
                }
                out[j] = res;
            }
            for (int j = 0; j < 16; j++) {
                byte res;
                if ((1 << j & 0x9999) != 0) {
                    res = AuthData.MAP_2[out[j] & 0xff];
                } else {
                    res = AuthData.MAP_3[out[j] & 0xff];
                }
                out[j] = res;
            }
            for (int j = 0; j < 16; j++) {
                byte res;
                if ((1 << j & 0x9999) != 0) {
                    res = (byte) (in[inOffset + j + 16] + out[j]);
                } else {
                    res = (byte) (in[inOffset + j + 16] ^ out[j]);
                }
                out[j] = res;
            }

            byte[] o = out.clone();
            for (int j = 0; j < 16; j++) {
                byte res = 0x0;
                for (int v = 0; v < 16; v++) {
                    res += (byte) (AuthData.COEFFICIENTS[j][v] * o[v]);
                }
                out[j] = res;
            }

            inOffset += 0x20;
        }

        for (int j = 0; j < 16; j++) {
            byte res;
            if ((1 << j & 0x9999) != 0) {
                res = (byte) (in[j + 256] ^ out[j]);
            } else {
                res = (byte) (in[j + 256] + out[j]);
            }
            out[j] = res;
        }
        return out;
    }

    public static byte[] getRandomChallenge() {
        byte[] res = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(res);
        return res;
    }

    public static byte[] computeChallengeResponse(byte[] input) {
        byte[] res1 = step1(input);
        return step2(AuthData.SEQ.clone(), res1);
    }

}

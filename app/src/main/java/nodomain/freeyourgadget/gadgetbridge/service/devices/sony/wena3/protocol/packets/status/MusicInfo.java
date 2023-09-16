package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class MusicInfo implements Wena3Packetable {
    public String musicInfoText;

    public MusicInfo(String musicInfoText) {
        this.musicInfoText = musicInfoText;
    }

    @Override
    public byte[] toByteArray() {
        byte[] encodedString = StringUtils.truncate(musicInfoText.trim(), 99).getBytes(StandardCharsets.UTF_8);
        return ByteBuffer
                .allocate(2 + encodedString.length)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte)0x05)
                .put((byte)encodedString.length)
                .put(encodedString)
                .array();
    }
}

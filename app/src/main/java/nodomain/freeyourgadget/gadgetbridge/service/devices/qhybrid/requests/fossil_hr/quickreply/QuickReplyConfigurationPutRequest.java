/*  Copyright (C) 2019-2021 Arjan Schrijver

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil_hr.quickreply;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.adapter.fossil_hr.FossilHRWatchAdapter;
import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.file.FileHandle;
import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil.file.FilePutRequest;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class QuickReplyConfigurationPutRequest extends FilePutRequest {
    public QuickReplyConfigurationPutRequest(String reply1, String reply2, String reply3, FossilHRWatchAdapter adapter) {
        super(FileHandle.REPLY_MESSAGES, createFile(reply1, reply2, reply3), adapter);
    }

    private static byte[] createFile(String reply1, String reply2, String reply3) {
        byte[] mysteryHeader = new byte[]{(byte) 0x02, (byte) 0x0b, (byte) 0x46, (byte) 0x00, (byte) 0x03, (byte) 0x19, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        Charset charsetUTF8 = Charset.forName("UTF-8");
        String iconName = StringUtils.terminateNull("icMessage.icon");
        byte[] iconNameBytes = iconName.getBytes(charsetUTF8);

        if (reply1.length() > 50) {
            reply1 = reply1.substring(0, 50);
        }
        String message1 = StringUtils.terminateNull(reply1);
        byte[] msg1Bytes = message1.getBytes(charsetUTF8);
        if (reply2.length() > 50) {
            reply2 = reply2.substring(0, 50);
        }
        String message2 = StringUtils.terminateNull(reply2);
        byte[] msg2Bytes = message2.getBytes(charsetUTF8);
        if (reply3.length() > 50) {
            reply3 = reply3.substring(0, 50);
        }
        String message3 = StringUtils.terminateNull(reply3);
        byte[] msg3Bytes = message3.getBytes(charsetUTF8);

        int fileLength = 8 + msg1Bytes.length + iconNameBytes.length;
        fileLength += 8 + msg2Bytes.length + iconNameBytes.length;
        fileLength += 8 + msg3Bytes.length + iconNameBytes.length;

        ByteBuffer mainBuffer = ByteBuffer.allocate(mysteryHeader.length + 4 + fileLength);
        mainBuffer.order(ByteOrder.LITTLE_ENDIAN);

        mainBuffer.put(mysteryHeader);
        mainBuffer.putInt(fileLength);

        mainBuffer.putShort((short) (8 + msg1Bytes.length + iconNameBytes.length));
        mainBuffer.put((byte) 0x08);
        mainBuffer.put((byte) 0x01);
        mainBuffer.putShort((short) msg1Bytes.length);
        mainBuffer.putShort((short) iconNameBytes.length);
        mainBuffer.put(msg1Bytes);
        mainBuffer.put(iconNameBytes);

        mainBuffer.putShort((short) (8 + msg2Bytes.length + iconNameBytes.length));
        mainBuffer.put((byte) 0x08);
        mainBuffer.put((byte) 0x02);
        mainBuffer.putShort((short) msg2Bytes.length);
        mainBuffer.putShort((short) iconNameBytes.length);
        mainBuffer.put(msg2Bytes);
        mainBuffer.put(iconNameBytes);

        mainBuffer.putShort((short) (8 + msg3Bytes.length + iconNameBytes.length));
        mainBuffer.put((byte) 0x08);
        mainBuffer.put((byte) 0x03);
        mainBuffer.putShort((short) msg3Bytes.length);
        mainBuffer.putShort((short) iconNameBytes.length);
        mainBuffer.put(msg3Bytes);
        mainBuffer.put(iconNameBytes);

        return mainBuffer.array();
    }
}

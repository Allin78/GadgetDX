/*  Copyright (C) 2021 Frank Ertl

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ImageData extends WithingsStructure {

    byte [] imageData;

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public short getLength() {
        return imageData != null ? (short)(imageData.length + 1 + HEADER_SIZE) : 1 + HEADER_SIZE;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        if (imageData != null) {
            addByteArrayWithLengthByte(buffer, imageData);
        } else {
            addByteArrayWithLengthByte(buffer, new byte[0]);
        }
    }

    @Override
    public short getType() {
        return WithingsStructureType.IMAGE_DATA;
    }
}

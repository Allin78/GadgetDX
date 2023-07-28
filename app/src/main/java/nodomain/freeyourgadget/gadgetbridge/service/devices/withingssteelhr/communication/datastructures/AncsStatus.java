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

public class AncsStatus extends WithingsStructure {

    private boolean isOn;

    public AncsStatus() {}

    public AncsStatus(boolean isOn) {
        this.isOn = isOn;
    }

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(isOn? (byte)0x01 : 0x00);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ANCS_STATUS;
    }
}

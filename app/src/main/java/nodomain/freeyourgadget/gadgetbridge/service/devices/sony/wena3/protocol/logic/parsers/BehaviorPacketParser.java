/*
 *   Copyright (C) 2023 akasaka / Genjitsu Labs
 *
 *     This file is part of Gadgetbridge.
 *
 *     Gadgetbridge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gadgetbridge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.logic.parsers;

import java.nio.ByteBuffer;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity.BehaviorSample;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;

public class BehaviorPacketParser extends SamplePacketParser<BehaviorSample>  {
    public BehaviorPacketParser() {
        super(0x02);
    }

    @Override
    boolean tryExtractingMetadataFromHeaderBuffer(ByteBuffer buffer) {
        return true;
    }

    @Override
    BehaviorSample takeSampleFromBuffer(ByteBuffer buffer) {
        // Entry structure:
        // - 1b type
        // - 4b padding?
        // - 4b start date
        // - 4b end date
        int id = (buffer.get() & 0xFF);
        if(id < BehaviorSample.Type.LUT.length) {
            BehaviorSample.Type type = BehaviorSample.Type.LUT[id];
            buffer.position(buffer.position()+4);
            Date start = TimeUtil.wenaTimeToDate(buffer.getInt());
            Date end = TimeUtil.wenaTimeToDate(buffer.getInt());
            return new BehaviorSample(start, end, type);
        }
        return null;
    }

    @Override
    boolean canTakeSampleFromBuffer(ByteBuffer buffer) {
        return buffer.remaining() >= 13;
    }
}

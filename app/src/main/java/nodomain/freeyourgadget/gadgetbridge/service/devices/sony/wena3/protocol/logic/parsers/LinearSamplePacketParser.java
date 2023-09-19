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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.logic.ActivityPacketParser;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity.ActivitySyncDataPacket;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;

abstract class LinearSamplePacketParser<Sample> extends SamplePacketParser<Sample> {
    private static final Logger LOG = LoggerFactory.getLogger(LinearSamplePacketParser.class);
    private int msBetweenSamples = 0;
    public Date startDate = null;

    public LinearSamplePacketParser(int headerMarker, int sampleDistanceInMs) {
        super(headerMarker);
        msBetweenSamples = sampleDistanceInMs;
        reset();
    }

    @Override
    public void finishReceiving() {
        Date estimatedEndDate = new Date(startDate.getTime() + ((long) accumulator.size() * msBetweenSamples));
        LOG.info("Finished collecting "+accumulator.size()+" samples over "+startDate+" ~ "+estimatedEndDate);
        super.finishReceiving();
    }

    @Override
    boolean tryExtractingMetadataFromHeaderBuffer(ByteBuffer buffer) {
        if(buffer.remaining() < 4) {
            LOG.error("Received ASDP header is too short");
            return false;
        }

        startDate = TimeUtil.wenaTimeToDate(buffer.getInt());
        return true;
    }

    abstract Sample takeSampleFromBuffer(ByteBuffer buffer);
    abstract boolean canTakeSampleFromBuffer(ByteBuffer buffer);
}

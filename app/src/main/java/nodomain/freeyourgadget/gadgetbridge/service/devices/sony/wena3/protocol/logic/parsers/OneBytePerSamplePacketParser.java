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

abstract class OneBytePerSamplePacketParser implements ActivityPacketParser {
    private static final Logger LOG = LoggerFactory.getLogger(OneBytePerSamplePacketParser.class);
    public static final int ONE_MINUTE_IN_MS = 60_000; // 1 sample per minute it seems
    private int msBetweenSamples = 0;
    private int headerMarker;
    private Date startDate = null;
    private List<Integer> accumulator = new ArrayList<>();
    private enum State {
        READY, RECEIVING, FINISHED
    }
    private State currentState = State.READY;

    public OneBytePerSamplePacketParser(int headerMarker, int sampleDistanceInMs) {
        this.headerMarker = headerMarker;
        msBetweenSamples = sampleDistanceInMs;
        reset();
    }

    @Override
    public void reset() {
        accumulator = new ArrayList<>();
        startDate = null;
        currentState = State.READY;
    }
    @Override
    public boolean parseHeader(ActivitySyncDataPacket packet) {
        assert packet.isCrcValid;
        assert packet.type == ActivitySyncDataPacket.PacketType.HEADER;

        ByteBuffer buf = packet.dataBuffer();

        int type = buf.get();
        if(type != this.headerMarker) {
            LOG.debug("Received ASDP with marker "+type+", not expected type");
            return false;
        }
        if(currentState != State.READY)
            return false;
        if(buf.remaining() < 4) {
            LOG.error("Received ASDP header is too short");
            return false;
        }

        startDate = TimeUtil.wenaTimeToDate(buf.getInt());
        currentState = State.RECEIVING;
        LOG.info("Ready to receive packets starting at "+startDate);

        return true;
    }

    @Override
    public void parsePacket(ActivitySyncDataPacket packet) {
        assert currentState == State.RECEIVING;
        assert packet.isCrcValid;
        assert packet.type == ActivitySyncDataPacket.PacketType.DATA;

        for(byte b: packet.data) {
            accumulator.add((int) b);
        }
        LOG.info("Accumulated "+accumulator.size()+" samples");
    }

    @Override
    public void finishReceiving() {
        assert currentState == State.RECEIVING;
        currentState = State.FINISHED;

        Date estimatedEndDate = new Date(startDate.getTime() + ((long) accumulator.size() * msBetweenSamples));
        LOG.info("Finished collecting "+accumulator.size()+" samples over "+startDate+" ~ "+estimatedEndDate);

        // TODO: Output result
        reset();
    }
}

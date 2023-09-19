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

package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity.ActivitySyncDataPacket;

public class ActivitySyncPacketProcessor {
    private static final int RESET_SEQ_NO = 0xFF;
    private static final int MAX_SEQ_NO = 0x7F;
    private static final Logger LOG = LoggerFactory.getLogger(ActivitySyncPacketProcessor.class);

    private ActivityPacketParser currentParser = null;
    private List<ActivityPacketParser> parsers = new ArrayList<>();
    private int currentSeqNo = RESET_SEQ_NO;

    public ActivitySyncPacketProcessor() {}

    public void registerParser(ActivityPacketParser parser) {
        parsers.add(parser);
    }

    public void receivePacket(ActivitySyncDataPacket packet) {
        if(packet.sequenceNo != currentSeqNo) {
            LOG.error("There was packet loss (skip "+currentSeqNo+" to "+packet.sequenceNo+")");
            finalizeCurrentParserIfNeeded();
            return;
        } else {
            if(currentSeqNo == RESET_SEQ_NO) {
                LOG.info("Initial packet received, off we go with a sync!");
                currentSeqNo = 0;
                resetAll();
                return;
            } else {
                if(currentSeqNo == MAX_SEQ_NO) {
                    currentSeqNo = 0;
                } else {
                    currentSeqNo ++;
                }
            }
        }

        if(!packet.isCrcValid) {
            LOG.error("Received packet has invalid CRC");
            return;
        }

        switch(packet.type) {
            case HEADER:
                finalizeCurrentParserIfNeeded();
                for(ActivityPacketParser parser: parsers) {
                    if(parser.parseHeader(packet)) {
                        currentParser = parser;
                        break;
                    }
                }
                LOG.warn("No parsers can understand " + packet.toString());
                break;

            case DATA:
                if(currentParser != null) {
                    currentParser.parsePacket(packet);
                } else {
                    LOG.warn("No parser known: dropped data packet " + packet.toString());
                }
                break;

            case FINISH:
                LOG.info("End of transmission received");
                finalizeCurrentParserIfNeeded();
                break;
        }
    }

    public void resetAll() {
        currentParser = null;
        for(ActivityPacketParser parser: parsers) parser.reset();
    }

    private void finalizeCurrentParserIfNeeded() {
        if(currentParser != null) {
            currentParser.finishReceiving();
            currentParser = null;
        }
    }
}

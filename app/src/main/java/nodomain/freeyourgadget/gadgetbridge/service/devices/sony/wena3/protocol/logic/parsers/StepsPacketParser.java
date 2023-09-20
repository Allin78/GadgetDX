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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3ActivitySampleCombiner;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3ActivitySampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3BehaviorSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.Wena3HeartRateSample;
import nodomain.freeyourgadget.gadgetbridge.entities.Wena3StepsSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class StepsPacketParser extends OneBytePerSamplePacketParser {
    private static final Logger LOG = LoggerFactory.getLogger(StepsPacketParser.class);
    private static final int STEPS_PKT_MARKER = 0x00;
    public StepsPacketParser() {
        super(STEPS_PKT_MARKER, ONE_MINUTE_IN_MS);
    }

    @Override
    public void finishReceiving(GBDevice device) {
        try (DBHandler db = GBApplication.acquireDB()) {
            SonyWena3ActivitySampleProvider sampleProvider = new SonyWena3ActivitySampleProvider(device, db.getDaoSession());
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(device, db.getDaoSession()).getId();

            Date currentSampleDate = startDate;
            int i = 0;
            for(int rawSample: accumulator) {
                Wena3StepsSample gbSample = new Wena3StepsSample();
                gbSample.setDeviceId(deviceId);
                gbSample.setUserId(userId);
                gbSample.setTimestamp((int)(currentSampleDate.getTime() / 1000L));
                gbSample.setSteps(rawSample);
                sampleProvider.addGBActivitySample(gbSample);

                i++;
                currentSampleDate = timestampOfSampleAtIndex(i);
            }

            SonyWena3BehaviorSampleProvider behaviorSampleProvider = new SonyWena3BehaviorSampleProvider(device, db.getDaoSession());
            SonyWena3ActivitySampleCombiner combiner = new SonyWena3ActivitySampleCombiner(behaviorSampleProvider, sampleProvider);
            combiner.overlayBehaviorStartingAt(startDate);
        } catch (Exception e) {
            LOG.error("Error acquiring database for recording steps samples", e);
        }

        // Finally clean up the parser
        super.finishReceiving(device);
    }
}

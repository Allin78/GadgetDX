/*  Copyright (C) 2022 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitgts4;

import android.content.Context;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiFWHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitgts4.AmazfitGTS4FWHelper;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.Huami2021Support;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.operations.FetchStressHandler;

public class AmazfitGTS4Support extends Huami2021Support {

    private static final Logger LOG = LoggerFactory.getLogger(AmazfitGTS4Support.class);

    public AmazfitGTS4Support() {
        this(LOG);
    }

    public AmazfitGTS4Support(final Logger logger) {
        super(logger);
    }

    @Override
    protected void initFetchHandlers() {
        super.initFetchHandlers();
        handlers.add(new FetchStressHandler(true, getDevice(), getContext()));
    }
    @Override
    public HuamiFWHelper createFWHelper(final Uri uri, final Context context) throws IOException {
        return new AmazfitGTS4FWHelper(uri, context);
    }
}

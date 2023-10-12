/*  Copyright (C) 2023 Johannes Krude

    based on code from BlueWatcher, https://github.com/masterjc/bluewatcher

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
package nodomain.freeyourgadget.gadgetbridge.devices.casio.gwb5600;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;

public class CasioGMWB5000DeviceCoordinator extends CasioGWB5600DeviceCoordinator {
    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("CASIO GMW-B5000");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_casiogmwb5000;
    }
}

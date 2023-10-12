/*  Copyright (C) 2017-2021 Daniele Gobbetti, João Paulo Barraca, Quallenauge

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
package nodomain.freeyourgadget.gadgetbridge.devices.hplus;

/*
* @author Quallenauge &lt;Hamsi2k@freenet.de&gt;
*/


import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;

/**
 * Pseudo Coordinator for the EXRIZU K8, a sub type of the HPLUS devices
 */
public class EXRIZUK8Coordinator extends HPlusCoordinator {
    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("iRun .*");
    }

    @Override
    public String getManufacturer() {
        return "EXRIZU";
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_exrizu_k8;
    }
}

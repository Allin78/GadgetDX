/*  Copyright (C) 2023 Marc Nause

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
package nodomain.freeyourgadget.gadgetbridge.devices.card10;

import java.util.UUID;

public class Card10Constants {

    /*
     * See https://firmware.card10.badge.events.ccc.de/bluetooth/overview.html for BLE documentation.
     */

    public static final UUID UUID_SERVICE_FILE_TRANSFER = UUID.fromString("42230100-2342-2342-2342-234223422342");
    public static final UUID UUID_SERVICE_CARD10 = UUID.fromString("42230200-2342-2342-2342-234223422342");
    public static final UUID UUID_SERVICE_ECG = UUID.fromString("42230300-2342-2342-2342-234223422342");

    // file transfer
    public static final UUID UUID_CHARACTERISTIC_CENTRAL_TX = UUID.fromString("42230101-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_CENTRAL_RX = UUID.fromString("42230102-2342-2342-2342-234223422342");

    // card10
    public static final UUID UUID_CHARACTERISTIC_TIME_UPDATE = UUID.fromString("42230201-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_VIBRA = UUID.fromString("4223020f-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_ROCKETS = UUID.fromString("42230210-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_BL = UUID.fromString("42230211-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_BR = UUID.fromString("42230212-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_TR = UUID.fromString("42230213-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_TL = UUID.fromString("42230214-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LEDS_DIM_BOTTOM = UUID.fromString("42230215-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LEDS_DIM_TOP = UUID.fromString("42230216-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_POWERSAVE = UUID.fromString("42230217-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LED_FLASHLIGHT = UUID.fromString("42230218-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_PERSONAL_STATE = UUID.fromString("42230219-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LEDS_ABOVE = UUID.fromString("42230220-2342-2342-2342-234223422342");
    public static final UUID UUID_CHARACTERISTIC_LIGHT_SENSOR = UUID.fromString("422302f0-2342-2342-2342-234223422342");

    // ECG
    public static final UUID UUID_CHARACTERISTIC_ECG_SAMPLES = UUID.fromString("42230301-2342-2342-2342-234223422342");
}

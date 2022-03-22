/*  Copyright (C) 2019-2021 Gordon Williams

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
package nodomain.freeyourgadget.gadgetbridge.devices.banglejs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;

public final class BangleJSConstants {


    public static final UUID UUID_SERVICE_NORDIC_UART = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UUID_CHARACTERISTIC_NORDIC_UART_TX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID UUID_CHARACTERISTIC_NORDIC_UART_RX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    // these correspond to CallSpec.CALL_*
    public static final String CMD_UNDEFINED = "undefined";
    public static final String CMD_ACCEPT = "accept";
    public static final String CMD_INCOMING = "incoming";
    public static final String CMD_OUTGOING = "outgoing";
    public static final String CMD_REJECT = "reject";
    public static final String CMD_START = "start";
    public static final String CMD_END = "end";

    public static final Map<Integer, String> CALL_CMDS = initCallCommands();
    private static Map<Integer, String> initCallCommands() {
        Map<Integer, String> map = new HashMap<>();
        map.put(CallSpec.CALL_UNDEFINED, CMD_UNDEFINED);
        map.put(CallSpec.CALL_ACCEPT, CMD_ACCEPT);
        map.put(CallSpec.CALL_INCOMING, CMD_INCOMING);
        map.put(CallSpec.CALL_OUTGOING, CMD_OUTGOING);
        map.put(CallSpec.CALL_REJECT, CMD_REJECT);
        map.put(CallSpec.CALL_START, CMD_START);
        map.put(CallSpec.CALL_END, CMD_END);
        return Collections.unmodifiableMap(map);
    }
}

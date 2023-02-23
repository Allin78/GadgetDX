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
package nodomain.freeyourgadget.gadgetbridge.service.devices.card10;

public enum Card10PersonalState {

    NONE(new byte[]{(byte) 0, (byte) 0}),
    NO_CONTACT(new byte[]{(byte) 1, (byte) 0}),
    CHAOS(new byte[]{(byte) 2, (byte) 0}),
    COMMUNICATION(new byte[]{(byte) 3, (byte) 0}),
    CAMP(new byte[]{(byte) 4, (byte) 0});

    private final byte[] command;

    Card10PersonalState(byte[] command) {
        this.command = command;
    }

    byte[] getCommand() {
        return command;
    }
}

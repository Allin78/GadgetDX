/*  Copyright (C) 2023 akasaka / Genjitsu Labs

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

package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines;

public enum VibrationKind {
    NONE(0),
    CONTINUOUS(1),
    BASIC(2),
    RAPID(3),
    TRIPLE(4),
    STEP_UP(5),
    STEP_DOWN(6),
    WARNING(7),
    SIREN(8),
    SHORT(9);

    public byte value;

    VibrationKind(int value) {
        this.value = (byte) value;
    }
}


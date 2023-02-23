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

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.UUID;

class CDPair {

    private final UUID characteristic;
    private final byte[] data;

    public CDPair(@NonNull UUID characteristic, @NonNull byte[] data) {
        this.characteristic = Objects.requireNonNull(characteristic);
        this.data = Objects.requireNonNull(data);
    }

    public UUID getCharacteristic() {
        return characteristic;
    }

    public byte[] getData() {
        return data;
    }
}

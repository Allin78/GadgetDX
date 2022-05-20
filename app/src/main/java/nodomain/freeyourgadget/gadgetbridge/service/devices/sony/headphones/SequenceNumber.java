/*  Copyright (C) 2022 Ngô Minh Quang

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 1-bit sequence number.
 */
public class SequenceNumber {

    public final int value;

    SequenceNumber(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value;
    }

    public SequenceNumber next() {
        int nextValue = 0x1 & (value + 1);
        return new SequenceNumber(nextValue);
    }

    public static SequenceNumber parse(int value) {
        if (value == 0 || value == 1) {
            return new SequenceNumber(value);
        }
        throw new IllegalArgumentException();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceNumber that = (SequenceNumber) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public static final SequenceNumber DEFAULT = new SequenceNumber(0);

}

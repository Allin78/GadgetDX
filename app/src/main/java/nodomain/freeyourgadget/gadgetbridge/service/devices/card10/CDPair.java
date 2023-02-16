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

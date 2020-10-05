/*  Copyright (C) 2016-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti
    Copyright (C) 2020 Yukai Li

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.requests;

import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.devices.lefun.LefunConstants;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEOperation;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.LefunDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.operations.OperationStatus;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

// Ripped from nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.Request
public abstract class Request extends AbstractBTLEOperation<LefunDeviceSupport> {
    protected TransactionBuilder builder;
    protected boolean removeAfterHandling = true;
    private Logger logger = (Logger) LoggerFactory.getLogger(getName());

    protected Request(LefunDeviceSupport support, TransactionBuilder builder) {
        super(support);
        this.builder = builder;
    }

    public TransactionBuilder getTransactionBuilder() {
        return builder;
    }

    @Override
    protected void doPerform() throws IOException {
        BluetoothGattCharacteristic characteristic = getSupport()
                .getCharacteristic(LefunConstants.UUID_CHARACTERISTIC_LEFUN_WRITE);
        builder.write(characteristic, createRequest());
        if (isSelfQueue())
            getSupport().performConnected(builder.getTransaction());
    }

    public abstract byte[] createRequest();

    public void handleResponse(byte[] data) {
        operationStatus = OperationStatus.FINISHED;
    }

    public String getName() {
        Class thisClass = getClass();
        while (thisClass.isAnonymousClass()) thisClass = thisClass.getSuperclass();
        return thisClass.getSimpleName();
    }

    protected void log(String message) {
        logger.debug(message);
    }

    public abstract int getCommandId();

    public boolean isSelfQueue() {
        return false;
    }

    public boolean expectsResponse() {
        return true;
    }

    public boolean shouldRemoveAfterHandling() {
        return removeAfterHandling;
    }

    protected void reportFailure(String message) {
        GB.toast(getContext(), message, Toast.LENGTH_SHORT, GB.ERROR);
    }
}

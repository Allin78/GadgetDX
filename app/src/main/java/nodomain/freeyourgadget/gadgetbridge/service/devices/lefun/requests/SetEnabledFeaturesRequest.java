/*  Copyright (C) 2020-2024 Yukai Li

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.requests;

import nodomain.freeyourgadget.gadgetbridge.devices.lefun.LefunConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.lefun.commands.BaseCommand;
import nodomain.freeyourgadget.gadgetbridge.devices.lefun.commands.FeaturesCommand;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.LefunDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.operations.OperationStatus;

public class SetEnabledFeaturesRequest extends Request {
    private FeaturesCommand cmd;

    public SetEnabledFeaturesRequest(LefunDeviceSupport support, TransactionBuilder builder) {
        super(support, builder);
    }

    public FeaturesCommand getCmd() {
        return cmd;
    }

    public void setCmd(FeaturesCommand cmd) {
        this.cmd = cmd;
    }

    @Override
    public byte[] createRequest() {
        cmd.setOp(BaseCommand.OP_SET);
        return cmd.serialize();
    }

    @Override
    public void handleResponse(byte[] data) {
        FeaturesCommand cmd = new FeaturesCommand();
        cmd.deserialize(data);
        if (cmd.getOp() == BaseCommand.OP_SET && !cmd.isSetSuccess())
            reportFailure("Could not set enabled features");

        operationStatus = OperationStatus.FINISHED;
    }

    @Override
    public int getCommandId() {
        return LefunConstants.CMD_FEATURES;
    }
}

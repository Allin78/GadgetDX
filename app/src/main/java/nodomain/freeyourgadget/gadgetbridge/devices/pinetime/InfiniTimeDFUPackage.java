/*  Copyright (C) 2021 Taavi Eomäe

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
package nodomain.freeyourgadget.gadgetbridge.devices.pinetime;

import java.math.BigInteger;
import java.util.List;

public class InfiniTimeDFUPackage {
    InfiniTimeDFUPackageManifest manifest;
}

class InfiniTimeDFUPackageManifest {
    InfiniTimeDFUPackageApplication application;
    Float dfu_version;
}

class InfiniTimeDFUPackageApplication {
    String bin_file;
    String dat_file;
    InfiniTimeDFUPackagePacketData init_packet_data;
}

class InfiniTimeDFUPackagePacketData {
    BigInteger application_version;
    BigInteger device_revision;
    BigInteger device_type;
    BigInteger firmware_crc16;
    List<Integer> softdevice_req;
}

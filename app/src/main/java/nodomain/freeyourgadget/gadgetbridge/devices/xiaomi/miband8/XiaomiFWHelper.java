/*  Copyright (C) 2023 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.miband8;

import android.content.Context;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;

public class XiaomiFWHelper {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiFWHelper.class);

    private final Uri uri;
    private byte[] fw;
    private boolean valid;

    private String id;
    private String name;

    public XiaomiFWHelper(final Uri uri, final Context context) {
        this.uri = uri;

        final UriHelper uriHelper;
        try {
            uriHelper = UriHelper.get(uri, context);
        } catch (final IOException e) {
            LOG.error("Failed to get uri helper for {}", uri, e);
            return;
        }

        final int maxExpectedFileSize = 1024 * 1024 * 128; // 64MB

        if (uriHelper.getFileSize() > maxExpectedFileSize) {
            LOG.warn("Firmware size is larger than the maximum expected file size of {}", maxExpectedFileSize);
            return;
        }

        try (final InputStream in = new BufferedInputStream(uriHelper.openInputStream())) {
            this.fw = FileUtils.readAll(in, maxExpectedFileSize);
        } catch (final IOException e) {
            LOG.error("Failed to read bytes from {}", uri, e);
            return;
        }

        valid = parseFirmware();
    }

    public boolean isValid() {
        return valid;
    }

    public String getDetails() {
        return name != null ? name : "UNKNOWN WATCHFACE";
    }

    public byte[] getBytes() {
        return fw;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void unsetFwBytes() {
        this.fw = null;
    }

    private boolean parseFirmware() {
        if (fw[0] != (byte) 0x5A || fw[1] != (byte) 0xA5) {
            LOG.warn("File header not a watchface");
            return false;
        }

        id = StringUtils.untilNullTerminator(fw, 0x28);
        name = StringUtils.untilNullTerminator(fw, 0x68);

        if (id == null) {
            LOG.warn("id not found in {}", uri);
            return false;
        }

        if (name == null) {
            LOG.warn("name not found in {}", uri);
            return false;
        }

        try {
            Integer.parseInt(id);
        } catch (final Exception e) {
            LOG.warn("Id {} not a number", id);
            return false;
        }

        return true;
    }
}

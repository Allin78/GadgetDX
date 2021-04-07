/*  Copyright (C) 2019-2021 Daniel Dakhno

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil_hr.notification;

import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil_hr.file.AssetFile;

public class NotificationImage extends AssetFile {
    private String fileName;
    private byte[] imageData;
    private int imageWidth;
    private int imageHeight;

    public NotificationImage(String fileName, byte[] imageData) {
        super(fileName, imageData);
        this.fileName = fileName;
        this.imageData = imageData;
        this.imageWidth = 24;
        this.imageHeight = 24;
    }

    public byte[] getImageData() {
        return imageData;
    }
    
    public String getFileName() { return fileName; }
    
    public int getImageWidth() { return imageWidth; }

    public int getImageHeight() { return imageHeight; }
}

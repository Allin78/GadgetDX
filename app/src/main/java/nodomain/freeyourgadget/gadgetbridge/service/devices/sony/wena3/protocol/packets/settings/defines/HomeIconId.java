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
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines;

// This is done via a class to be able to init the value from an arbitrary int (e.g. from prefs)
// Whether listing all cases is needed is a good concern, they are essentially unused because
// the settings dialog uses a string-array instead...
public class HomeIconId {
    public static final HomeIconId TIMER = new HomeIconId(256);
    public static final HomeIconId ALARM = new HomeIconId(512);
    public static final HomeIconId CLOCK = new HomeIconId(768);
    public static final HomeIconId ALEXA = new HomeIconId(1024);
    public static final HomeIconId WENA_PAY = new HomeIconId(1280);
    public static final HomeIconId QRIO_LOCK = new HomeIconId(1536);
    public static final HomeIconId EDY = new HomeIconId(1792);
    public static final HomeIconId NOTIFICATION_COUNT = new HomeIconId(2048);
    public static final HomeIconId SCHEDULE = new HomeIconId(2304);
    public static final HomeIconId PEDOMETER = new HomeIconId(2560);
    public static final HomeIconId SLEEP = new HomeIconId(2816);
    public static final HomeIconId HEART_RATE = new HomeIconId(3072);
    public static final HomeIconId VO2MAX = new HomeIconId(3328);
    public static final HomeIconId STRESS = new HomeIconId(3584);
    public static final HomeIconId ENERGY = new HomeIconId(3840);

    public static final HomeIconId SUICA = new HomeIconId(4096);
    public static final HomeIconId CALORIES = new HomeIconId(4352);
    public static final HomeIconId RIIIVER = new HomeIconId(4608);
    public static final HomeIconId MUSIC = new HomeIconId(4864);
    public static final HomeIconId CAMERA = new HomeIconId(5120);

    public short value;

    public HomeIconId(int value) {
        this.value = (short) value;
    }
}

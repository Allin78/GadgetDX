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

public class MenuIconId {
    public static final MenuIconId TIMER = new MenuIconId(1);
    public static final MenuIconId ALARM = new MenuIconId(2);
    public static final MenuIconId FIND_PHONE = new MenuIconId(3);
    public static final MenuIconId ALEXA = new MenuIconId(4);
    public static final MenuIconId PAYMENT = new MenuIconId(5);
    public static final MenuIconId QRIO = new MenuIconId(6);
    public static final MenuIconId WEATHER = new MenuIconId(7);
    public static final MenuIconId MUSIC = new MenuIconId(8);
    public static final MenuIconId CAMERA = new MenuIconId(9);

    public byte value;

    public MenuIconId(int value) {
        this.value = (byte) value;
    }
}

package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class CameraAppTypeSetting implements Wena3Packetable {
    private static final String PHOTOPRO_APP_ID = "com.sonymobile.photopro";
    public boolean hasXperiaApp;

    public CameraAppTypeSetting(boolean isXperia) {
        this.hasXperiaApp = isXperia;
    }

    public static CameraAppTypeSetting findOut(PackageManager pm) {
        try {
            pm.getPackageInfo(PHOTOPRO_APP_ID, 0);
            return new CameraAppTypeSetting(true);
        } catch (PackageManager.NameNotFoundException e) {
            return new CameraAppTypeSetting(false);
        }
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer
                .allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte)0x21)
                .put((byte) (hasXperiaApp ? 0x1 : 0x0))
                .array();
    }
}

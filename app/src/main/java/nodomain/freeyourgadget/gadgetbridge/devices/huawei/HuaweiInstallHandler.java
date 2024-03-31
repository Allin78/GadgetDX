package nodomain.freeyourgadget.gadgetbridge.devices.huawei;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.InstallActivity;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.GenericItem;
import nodomain.freeyourgadget.gadgetbridge.util.GBZipFile;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;
import nodomain.freeyourgadget.gadgetbridge.util.ZipFileException;


public class HuaweiInstallHandler implements InstallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiInstallHandler.class);

    private final Context context;
    Bitmap previewbMap;
    byte[] watchfaceBin;
    byte[] watchfaceSHA256;
    public HuaweiInstallHandler(Uri uri, Context context) {
        this.context = context;

        UriHelper uriHelper;

        try {
            uriHelper = UriHelper.get(uri, this.context);

            GBZipFile watchfacePackage = new GBZipFile(uriHelper.openInputStream());
            String watchfaceDescription = new String(watchfacePackage.getFileFromZip("description.xml"));
            final byte[] preview = watchfacePackage.getFileFromZip("preview/cover.jpg");
            previewbMap = BitmapFactory.decodeByteArray(preview, 0, preview.length);

        } catch (ZipFileException e) {
            LOG.error("Unable to read watchface file.", e);
            return;
        } catch (FileNotFoundException e) {
            LOG.error("The watchface file was not found.", e);
            return;
        } catch (IOException e) {
            LOG.error("General IO error occurred.", e);
            return;
        } catch (Exception e) {
            LOG.error("Unknown error occurred.", e);
            return;
        }

    }

    @Override
    public void validateInstallation(InstallActivity installActivity, GBDevice device) {
        installActivity.setInstallEnabled(true);

        GenericItem installItem = new GenericItem();


        if (previewbMap != null) {
            installItem.setPreview(previewbMap);
        }

        if (device.isBusy()) {
            LOG.error("Firmware cannot be installed (device busy)");
            installActivity.setInfoText("Firmware cannot be installed (device busy)");
            installActivity.setInfoText(device.getBusyTask());
            installActivity.setInstallEnabled(false);
            return;
        }

        if ( !device.isConnected()) { //FIXME: Add all tested huawei devices?
            LOG.error("Firmware cannot be installed (not connected or wrong device)");
            installActivity.setInfoText("Firmware cannot be installed (not connected or wrong device)");
            installActivity.setInstallEnabled(false);
            return;
        }

        if (!isValid()) {
            LOG.error("Firmware cannot be installed (not valid)");
            installActivity.setInfoText("Firmware cannot be installed (not valid)");
            installActivity.setInstallEnabled(false);
        }

        installItem.setName("Huawei watchface preview");
        //installItem.setDetails(getVersion());

        installActivity.setInfoText(context.getString(R.string.firmware_install_warning, "(unknown)"));
        installActivity.setInstallItem(installItem);
        LOG.debug("Initialized HuaweiInstallHandler");
    }

    @Override
    public boolean isValid() {
        return true; //FIXME implement real check
    }

    @Override
    public void onStartInstall(GBDevice device) {
    }
}

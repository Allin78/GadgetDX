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
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.GenericItem;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWatchfaceManager;
import nodomain.freeyourgadget.gadgetbridge.util.GBZipFile;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;
import nodomain.freeyourgadget.gadgetbridge.util.ZipFileException;


public class HuaweiInstallHandler implements InstallHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiInstallHandler.class);

    private final Context context;
    Bitmap previewbMap;
    String watchfaceDescription;
    boolean valid = false;

    public HuaweiInstallHandler(Uri uri, Context context) {
        this.context = context;

        UriHelper uriHelper;

        try {
            uriHelper = UriHelper.get(uri, this.context);

            GBZipFile watchfacePackage = new GBZipFile(uriHelper.openInputStream());
            watchfaceDescription = new String(watchfacePackage.getFileFromZip("description.xml"));
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

        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        if (!(coordinator instanceof HuaweiCoordinatorSupplier)) {
            LOG.warn("Coordinator is not a HuaweiCoordinatorSupplier: {}", coordinator.getClass());
            installActivity.setInstallEnabled(false);
            return;
        }

        final HuaweiCoordinatorSupplier huaweiCoordinatorSupplier = (HuaweiCoordinatorSupplier) coordinator;

        HuaweiWatchfaceManager.WatchfaceDescription description = new HuaweiWatchfaceManager.WatchfaceDescription(watchfaceDescription);

        HuaweiWatchfaceManager.Resolution resolution = new HuaweiWatchfaceManager.Resolution();
        String deviceScreen =  String.format("%d*%d",huaweiCoordinatorSupplier.getHuaweiCoordinator().getHuaweiWatchfaceManager().getHeight(),
                huaweiCoordinatorSupplier.getHuaweiCoordinator().getHuaweiWatchfaceManager().getWidth());
        this.valid = resolution.isValid(description.screen, deviceScreen);

        installActivity.setInstallEnabled(true);

        GenericItem installItem = new GenericItem();


        if (previewbMap != null) {
            installItem.setPreview(previewbMap);
        }

        installItem.setName(description.title);
        installActivity.setInstallItem(installItem);
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

        if (!this.valid) {
            LOG.error("Watchface cannot be installed");
            installActivity.setInfoText("Watchface resolution doesnt match device screen. Watchface is "
                    + resolution.screenByThemeVersion(description.screen) + " device screen is " + deviceScreen);
            installActivity.setInstallEnabled(false);
            return;
        }


        //installItem.setDetails(getVersion());

        installItem.setIcon(R.drawable.ic_watchface);
        installActivity.setInfoText(context.getString(R.string.watchface_install_info, installItem.getName(), description.version, description.author));

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

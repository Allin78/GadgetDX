package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.util.GBZipFile;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;
import nodomain.freeyourgadget.gadgetbridge.util.ZipFileException;

public class HuaweiFwHelper {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiFwHelper.class);

    private final Uri uri;

    private byte[] fw;
    private int fileSize = 0;
    private boolean typeWatchface;

    Bitmap watchfacePreviewBitmap;
    HuaweiWatchfaceManager.WatchfaceDescription watchfaceDescription;
    Context mContext;

    public HuaweiFwHelper(final Uri uri, final Context context) {

        this.uri = uri;
        final UriHelper uriHelper;
        this.mContext = context;
        try {
            uriHelper = UriHelper.get(uri, context);
        } catch (final IOException e) {
            LOG.error("Failed to get uri helper for {}", uri, e);
            return;
        }

        parseFile();
    }

    private void parseFile() {
        if (parseAsWatchFace()) {
            assert watchfaceDescription.screen != null;
            assert watchfaceDescription.title != null;
            typeWatchface = true;
        }
    }

    public byte[] getBytes() {
        return fw;
    }

    public void unsetFwBytes() {
        this.fw = null;
    }

    boolean parseAsWatchFace() {
        try {
            final UriHelper uriHelper = UriHelper.get(uri, this.mContext);

            GBZipFile watchfacePackage = new GBZipFile(uriHelper.openInputStream());
            String xmlDescription = new String(watchfacePackage.getFileFromZip("description.xml"));
            watchfaceDescription = new HuaweiWatchfaceManager.WatchfaceDescription(xmlDescription);
            if (watchfacePackage.fileExists("preview/cover.jpg")) {
                final byte[] preview = watchfacePackage.getFileFromZip("preview/cover.jpg");
                watchfacePreviewBitmap = BitmapFactory.decodeByteArray(preview, 0, preview.length);
            }
            fw = watchfacePackage.getFileFromZip("com.huawei.watchface");
            fileSize = fw.length;

        } catch (ZipFileException e) {
            LOG.error("Unable to read watchface file.", e);
            return false;
        } catch (FileNotFoundException e) {
            LOG.error("The watchface file was not found.", e);
            return false;
        } catch (IOException e) {
            LOG.error("General IO error occurred.", e);
            return false;
        } catch (Exception e) {
            LOG.error("Unknown error occurred.", e);
            return false;
        }

        return true;
    }

    public boolean isWatchface() {
        return typeWatchface;
    }
    public boolean isValid() {
        return isWatchface();
    }

    public Bitmap getWatchfacePreviewBitmap() {
        return watchfacePreviewBitmap;
    }

    public HuaweiWatchfaceManager.WatchfaceDescription getWatchfaceDescription() {
        return watchfaceDescription;
    }


}

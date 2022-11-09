package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.IconHelper;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.GlyphId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageMetaData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming.IncomingMessageHandler;

public class GlyphRequestHandler implements IncomingMessageHandler {
    private final WithingsSteelHRDeviceSupport support;

    public GlyphRequestHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    @Override
    public void handleMessage(Message message) {
        GlyphId glyphId = message.getStructureByType(GlyphId.class);
        ImageMetaData imageMetaData = message.getStructureByType(ImageMetaData.class);
        Message reply = new WithingsMessage(WithingsMessageType.GET_UNICODE_GLYPH);
        reply.addDataStructure(glyphId);
        reply.addDataStructure(imageMetaData);
        ImageData imageData = new ImageData();
        imageData.setImageData(createUnicodeImage(glyphId.getUnicode(), imageMetaData));
        reply.addDataStructure(imageData);
        support.sendToDevice(reply);
    }

    private byte[] createUnicodeImage(long unicode, ImageMetaData metaData) {
        String str = new String(Character.toChars((int)unicode));
        Paint paint = new Paint();
        paint.setTypeface(null);
        Rect rect = new Rect();
        paint.setTextSize(calculateTextsize(paint, metaData.getHeight()));
        paint.setAntiAlias(true);
        paint.getTextBounds(str, 0, str.length(), rect);
        paint.setColor(-1);
        Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
        int width = rect.width();
        if (width <= 0) {
            return new byte[0];
        }
        Bitmap createBitmap = Bitmap.createBitmap(width, metaData.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(createBitmap).drawText(str, -rect.left, -fontMetricsInt.top, paint);
        return IconHelper.toByteArray(createBitmap);
    }

    private int calculateTextsize(Paint paint, int height) {
        Paint.FontMetricsInt fontMetricsInt;
        int textsize = 0;
        do {
            textsize++;
            paint.setTextSize(textsize);
            fontMetricsInt = paint.getFontMetricsInt();
        } while (fontMetricsInt.bottom - fontMetricsInt.top < height);
        return textsize - 1;
    }
}

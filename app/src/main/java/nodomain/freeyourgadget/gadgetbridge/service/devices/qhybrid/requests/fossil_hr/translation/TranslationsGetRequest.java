package nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil_hr.translation;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.adapter.fossil.FossilWatchAdapter;
import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.requests.fossil.file.FileGetRequest;

public abstract class TranslationsGetRequest extends FileGetRequest {
    public TranslationsGetRequest(FossilWatchAdapter adapter) {
        super((short) 0x0702, adapter);
    }

    @Override
    public void handleFileData(byte[] fileData) {
        ByteBuffer buffer = ByteBuffer.wrap(fileData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.position(12);

        byte[] localeBytes = new byte[5];
        buffer.get(localeBytes);
        String locale = new String(localeBytes);

        buffer.get(); // locale null byte

        ArrayList<TranslationItem> translations = new ArrayList<>();

        while(buffer.remaining() > 4){
            int originalLength = buffer.getShort() - 1; // subtracting null terminator
            byte[] originalBytes = new byte[originalLength];
            buffer.get(originalBytes);
            buffer.get(); // should always return null terminator
            int translatedLength = buffer.getShort() - 1;
            byte[] translatedBytes = new byte[translatedLength];
            buffer.get(translatedBytes);
            buffer.get(); // should always return null terminator

            String original = new String(originalBytes);
            String translated = new String(translatedBytes);

            translations.add(new TranslationItem(original, translated));
        }

        handleTranslations(
                new TranslationData(
                        locale,
                        translations.toArray(new TranslationItem[0])
                )
        );

    }

    public abstract void handleTranslations(TranslationData translationDate);
}

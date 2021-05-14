package nodomain.freeyourgadget.gadgetbridge.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mozilla.deepspeech.libdeepspeech.CandidateTranscript;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState;
import org.mozilla.deepspeech.libdeepspeech.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiph.speex.SpeexDecoder;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.service.devices.pebble.PebbleProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

import static nodomain.freeyourgadget.gadgetbridge.GBApplication.app;

public class SpeechToText {

    public static final boolean defaultSttEnable = false;
    public static final String defaultSttTflite = null;
    public static final String defaultSttScorer = null;
    public static final String defaultSttBeamWidth = "500";

    private boolean sttEnable;
    private String sttTflite;
    private String sttScorer;
    private String sttBeamWidth;

    public static final String PREF_STT_ENABLE = "stt_enable";
    public static final String PREF_STT_TFLITE = "stt_tflite";
    public static final String PREF_STT_SCORER = "stt_scorer";
    public static final String PREF_STT_BEAM_WIDTH = "stt_beam_width";

    private int mSampleRate;
    private boolean mSpeex;
    private SpeexDecoder decoder;
    private DeepSpeechModel model = null;
    private DeepSpeechStreamingState streamContext;


    public SpeechToText(boolean speex, int sampleRate){
        fetchPreferences();
        mSampleRate = sampleRate;
        mSpeex = speex;
        if (mSpeex) {
            decoder = new SpeexDecoder();
            short mode = 0;
            if (mSampleRate == 16000)
                mode = 1;
            decoder.init(mode, mSampleRate, 1, true);
        }
        model = app().getModel();
        streamContext = model.createStream();
    }

    private void fetchPreferences() {
        Prefs prefs = GBApplication.getPrefs();
        sttEnable = prefs.getBoolean(PREF_STT_ENABLE, defaultSttEnable);
        sttTflite = prefs.getString(PREF_STT_TFLITE, defaultSttTflite);
        sttScorer = prefs.getString(PREF_STT_SCORER, defaultSttScorer);
        sttBeamWidth = prefs.getString(PREF_STT_BEAM_WIDTH, defaultSttBeamWidth);
    }

    public void addFrame(byte[] frame) {
        byte[] encoded_segments = new byte[0];
        if (mSpeex) {
            try {
                decoder.processData(frame, 0, frame.length);
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            }
            encoded_segments = new byte[decoder.getProcessedDataByteSize()];
            decoder.getProcessedData(encoded_segments, 0);
        } else {
            encoded_segments = frame;
        }
        short[] shortArray = new short[encoded_segments.length/2];
        ByteBuffer.wrap(encoded_segments).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortArray);
        model.feedAudioContent(streamContext, shortArray, shortArray.length);
    }

    public CandidateTranscript[] getResults() {
        Metadata metadata = model.finishStreamWithMetadata(streamContext, 1);
        CandidateTranscript[] transcripts = new CandidateTranscript[(int) metadata.getNumTranscripts()];
        for (int i = 0; i < metadata.getNumTranscripts(); i++) {
            transcripts[i] = metadata.getTranscript(i);
        }
        return transcripts;
    }
}

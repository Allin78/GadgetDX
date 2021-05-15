package nodomain.freeyourgadget.gadgetbridge.model;

import org.mozilla.deepspeech.libdeepspeech.CandidateTranscript;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechModel;
import org.mozilla.deepspeech.libdeepspeech.DeepSpeechStreamingState;
import org.mozilla.deepspeech.libdeepspeech.Metadata;
import org.xiph.speex.SpeexDecoder;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
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
    private boolean mSpeex = false;
    private SpeexDecoder decoder = null;
    private DeepSpeechModel model = null;
    private DeepSpeechStreamingState streamContext;
    private short mSpeexMode = 0;

    public SpeechToText(){
        fetchPreferences();
    }

    public void initModel() {
        model = app().getModel();
        streamContext = model.createStream();
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public void setSpeex() {
        decoder = new SpeexDecoder();
        if (mSampleRate == 16000)
            mSpeexMode = 1;
        decoder.init(mSpeexMode, mSampleRate, 1, true);
        mSpeex = true;
    }

    private void fetchPreferences() {
        Prefs prefs = GBApplication.getPrefs();
        sttEnable = prefs.getBoolean(PREF_STT_ENABLE, defaultSttEnable);
        sttTflite = prefs.getString(PREF_STT_TFLITE, defaultSttTflite);
        sttScorer = prefs.getString(PREF_STT_SCORER, defaultSttScorer);
        sttBeamWidth = prefs.getString(PREF_STT_BEAM_WIDTH, defaultSttBeamWidth);
    }

    public boolean getEnabled() {
        return sttEnable;
    }

    public String getTflite() {
        return sttTflite;
    }

    public String getScorer() {
        return sttScorer;
    }

    public long getBeamWidth() {
        return Long.parseLong(sttBeamWidth);
    }

    public void addFrame(byte[] frame) {
        short[] encoded_segments;
        if (mSpeex) {
            try {
                decoder.processData(frame, 0, frame.length);
            } catch (StreamCorruptedException | ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            encoded_segments = new short[decoder.getProcessedDataByteSize() / 2];
            decoder.getProcessedData(encoded_segments, 0);
        } else {
            encoded_segments = new short[frame.length / 2];
            ByteBuffer.wrap(frame).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(encoded_segments);
        }
        model.feedAudioContent(streamContext, encoded_segments, encoded_segments.length);
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

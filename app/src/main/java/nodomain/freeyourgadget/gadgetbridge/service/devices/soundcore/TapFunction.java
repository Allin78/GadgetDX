package nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore;
enum TapFunction {
    VOLUMEDOWN(1),
    VOLUMEUP(0),
    NEXT( 3),
    PREVIOUS(2),
    PLAYPAUSE(6),
    VOICE_ASSISTANT(5),
    AMBIENT_SOUND_CONTROL(4)
    ;

    private final int code;

    TapFunction(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
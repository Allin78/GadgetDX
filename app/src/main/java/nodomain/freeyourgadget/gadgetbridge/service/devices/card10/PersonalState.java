package nodomain.freeyourgadget.gadgetbridge.service.devices.card10;

enum PersonalState {

    NONE(new byte[]{(byte) 0, (byte) 0}),
    NO_CONTACT(new byte[]{(byte) 1, (byte) 0}),
    CHAOS(new byte[]{(byte) 2, (byte) 0}),
    COMMUNICATION(new byte[]{(byte) 3, (byte) 0}),
    CAMP(new byte[]{(byte) 4, (byte) 0});

    private final byte[] command;

    PersonalState(byte[] command) {
        this.command = command;
    }

    byte[] getCommand() {
        return command;
    }
}

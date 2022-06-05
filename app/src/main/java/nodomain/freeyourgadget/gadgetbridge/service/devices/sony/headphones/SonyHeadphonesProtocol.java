/*  Copyright (C) 2021 - 2022 José Rebelo, Ngô Minh Quang

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceState;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AmbientSoundControl;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AudioUpsampling;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AutomaticPowerOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.ButtonModes;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerCustomBands;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerPreset;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.PauseWhenTakenOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SoundPosition;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SurroundMode;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.TouchSensor;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.VoiceNotifications;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.MessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.AbstractSonyProtocolImpl;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.PayloadType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.SonyProtocolImplV1;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class SonyHeadphonesProtocol extends GBDeviceProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(SonyHeadphonesProtocol.class);

    private SonyHeadphonesIoThread ioThread = null;

    private static final int MAX_SEND_RETRIES = 10;
    private static final long SEND_RETRY_DELAY_MS = 1_000L;

    private ProtocolImplInitializationState protocolImplInitializationState = ProtocolImplInitializationState.NOT_INITIALIZED;
    private AbstractSonyProtocolImpl protocolImpl = null;

    // Used to track device reply/notify
    private volatile SequenceNumber receiveSequenceNumber = null;
    private final BlockingQueue<SequenceNumber> receivedAckSequenceNumbers = new LinkedBlockingQueue<>();

    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();

    private ExecutorService requestQueueProcessingExecutorService = null;

    public SonyHeadphonesProtocol(GBDevice device) {
        super(device);
    }

    public synchronized void initialize(SonyHeadphonesIoThread ioThread) {
        this.ioThread = ioThread;
        this.protocolImplInitializationState = ProtocolImplInitializationState.INITIALIZING;

        // Start requests processing thread
        requestQueueProcessingExecutorService = Executors.newSingleThreadExecutor();
        requestQueueProcessingExecutorService.execute(createRequestQueueProcessingThread());

        // Queue first init request
        enqueueRequest(createInitRequest());
    }

    public synchronized void quit() {
        requestQueue.clear();
        if (requestQueueProcessingExecutorService != null) {
            requestQueueProcessingExecutorService.shutdownNow();
        }
    }

    public boolean enqueueRequest(Request request) {
        try {
            requestQueue.put(request);
            return true;
        } catch (InterruptedException e) {
            LOG.error("Failed to enqueue request", e);
        }
        return false;
    }

    public void enqueueRequests(final List<Request> requests) {
        try {
            for (Request request : requests) {
                requestQueue.put(request);
            }
        } catch (InterruptedException e) {
            LOG.error("Failed to enqueue request", e);
        }
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] res) {
        final Message message = Message.fromBytes(res);

        if (message == null) return null;
        LOG.error("< {}", message);

        final SequenceNumber messageSequence;
        try {
            messageSequence = SequenceNumber.parse(message.getSequenceNumber());
        } catch (IllegalArgumentException e) {
            LOG.error("Reveived invalid sqeuence number: {}", message.getSequenceNumber());
            return null;
        }

        return handleMessage(message, messageSequence).toArray(new GBDeviceEvent[0]);
    }

    private List<? extends GBDeviceEvent> handleMessage(
            final Message message,
            final SequenceNumber messageSequenceNumber
    ) {
        // Filter ACK messages and store it for use later by `requestQueueProcessingTask`.
        if (MessageType.ACK == message.getType()) {
            try {
                receivedAckSequenceNumbers.put(messageSequenceNumber);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }

        // Check for unknown message types
        switch (message.getType()) {
            case COMMAND_1:
            case COMMAND_2:
                break;
            default:
                LOG.warn("Unknown message type for {}", message);
                return Collections.emptyList();
        }

        if (receiveSequenceNumber == null) {
            // Remember first received sequence number
            receiveSequenceNumber = messageSequenceNumber;
        } else if (receiveSequenceNumber.equals(messageSequenceNumber)) {
            LOG.debug("Ignore dupplicated messages");
            return Collections.emptyList();
        }
        // Remember received sequence number
        receiveSequenceNumber = messageSequenceNumber;

        final ArrayList<GBDeviceEvent> events = new ArrayList<>();

        // Send ACK to the device
        SequenceNumber ackSequenceNumber = messageSequenceNumber.next();
        Message ackMessage = createAckMessage(ackSequenceNumber);
        events.add(new GBDeviceEventSendBytes(ackMessage.encode()));

        // Now handle supported message types, depend on the protocol impl init state.
        try {
            switch (protocolImplInitializationState) {
                case NOT_INITIALIZED:
                    break;
                case INITIALIZING:
                    events.addAll(handleProtocolImplInitializing(message));
                    break;
                case INITIALIZED:
                    events.addAll(handleInitializedProtocolImpl(message));
            }
        } catch (final Exception e) {
            // Don't crash the app if we somehow fail to handle the payload
            LOG.error("Error handling payload", e);
        }

        return events;
    }

    @NonNull
    private List<? extends GBDeviceEvent> handleProtocolImplInitializing(final Message message) {
        final MessageType messageType = message.getType();

        // Check if we got an init response, which should indicate the protocol version
        if (MessageType.COMMAND_1.equals(messageType)
                && message.getPayload()[0] == PayloadType.INIT_REPLY.getCode()) {
            final ArrayList<GBDeviceEvent> events = new ArrayList<>();

            // Init reply, set the protocol version
            if (message.getPayload().length == 4) {
                protocolImpl = new SonyProtocolImplV1(getDevice());
                protocolImplInitializationState = ProtocolImplInitializationState.INITIALIZED;

                // Update device state
                events.add(new GBDeviceEventUpdateDeviceState(GBDevice.State.INITIALIZED));
                // Forward INIT_REPLY to protocol impl
                events.addAll(protocolImpl.handlePayload(messageType, message.getPayload()));
            } else if (message.getPayload().length == 6) {
                LOG.error("Sony Headphones protocol v2 is not yet supported");
            } else {
                LOG.error("Unexpected init response payload length: {}", message.getPayload().length);
            }

            return events;
        }

        // We can do nothing at this point. Just ignore the message. The device will try to
        // resend it anyway.
        return Collections.emptyList();
    }

    @NonNull
    private List<? extends GBDeviceEvent> handleInitializedProtocolImpl(final Message message) {
        // Forward messages to protocol impl
        return protocolImpl.handlePayload(message.getType(), message.getPayload());
    }

    public boolean onSendConfiguration(String config) {
        if (protocolImpl == null) {
            LOG.error("No protocol implementation, ignoring config {}", config);
            return false;
        }

        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());

        final Request configRequest;

        switch (config) {
            case DeviceSettingsPreferenceConst.PREF_SONY_AMBIENT_SOUND_CONTROL:
            case DeviceSettingsPreferenceConst.PREF_SONY_FOCUS_VOICE:
            case DeviceSettingsPreferenceConst.PREF_SONY_AMBIENT_SOUND_LEVEL:
                configRequest = protocolImpl.setAmbientSoundControl(AmbientSoundControl.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_NOISE_OPTIMIZER_START:
                configRequest = protocolImpl.startNoiseCancellingOptimizer(true);
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_NOISE_OPTIMIZER_CANCEL:
                configRequest = protocolImpl.startNoiseCancellingOptimizer(false);
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_SOUND_POSITION:
                configRequest = protocolImpl.setSoundPosition(SoundPosition.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_SURROUND_MODE:
                configRequest = protocolImpl.setSurroundMode(SurroundMode.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_MODE:
                configRequest = protocolImpl.setEqualizerPreset(EqualizerPreset.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BAND_400:
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BAND_1000:
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BAND_2500:
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BAND_6300:
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BAND_16000:
            case DeviceSettingsPreferenceConst.PREF_SONY_EQUALIZER_BASS:
                configRequest = protocolImpl.setEqualizerCustomBands(EqualizerCustomBands.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_AUDIO_UPSAMPLING:
                configRequest = protocolImpl.setAudioUpsampling(AudioUpsampling.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_TOUCH_SENSOR:
                configRequest = protocolImpl.setTouchSensor(TouchSensor.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_AUTOMATIC_POWER_OFF:
                configRequest = protocolImpl.setAutomaticPowerOff(AutomaticPowerOff.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_BUTTON_MODE_LEFT:
            case DeviceSettingsPreferenceConst.PREF_SONY_BUTTON_MODE_RIGHT:
                configRequest = protocolImpl.setButtonModes(ButtonModes.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_PAUSE_WHEN_TAKEN_OFF:
                configRequest = protocolImpl.setPauseWhenTakenOff(PauseWhenTakenOff.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_NOTIFICATION_VOICE_GUIDE:
                configRequest = protocolImpl.setVoiceNotifications(VoiceNotifications.fromPreferences(prefs));
                break;
            case DeviceSettingsPreferenceConst.PREF_SONY_CONNECT_TWO_DEVICES:
                LOG.warn("Connection to two devices not implemented ('{}')", config);
                return false;
            case DeviceSettingsPreferenceConst.PREF_SONY_SPEAK_TO_CHAT:
            case DeviceSettingsPreferenceConst.PREF_SONY_SPEAK_TO_CHAT_SENSITIVITY:
            case DeviceSettingsPreferenceConst.PREF_SONY_SPEAK_TO_CHAT_FOCUS_ON_VOICE:
            case DeviceSettingsPreferenceConst.PREF_SONY_SPEAK_TO_CHAT_TIMEOUT:
                LOG.warn("Speak-to-chat is not implemented ('{}')", config);
                return false;
            default:
                LOG.warn("Unknown config '{}'", config);
                return false;
        }

        return enqueueRequest(configRequest);
    }

    @Override
    public byte[] encodeTestNewFunction() {
        //return Request.fromHex(MessageType.COMMAND_1, "c40100").encode(sequenceNumber);

        return null;
    }

    public boolean onPowerOff() {
        if (protocolImpl == null) {
            LOG.error("No protocol implementation. Cannot power off!");
            return false;
        }
        return enqueueRequest(protocolImpl.powerOff());
    }

    private Message createAckMessage(SequenceNumber ackSequenceNumber) {
        return new Message(
                MessageType.ACK,
                ackSequenceNumber.getValue(),
                new byte[0]
        );
    }

    private Request createInitRequest() {
        return new Request(
                MessageType.COMMAND_1,
                new byte[] {
                        PayloadType.INIT_REQUEST.getCode(),
                        0
                }
        );
    }


    private Runnable createRequestQueueProcessingThread() {
        return new Runnable() {
            @Override
            public void run() {
                // Used to handle request send timeout.
                final ExecutorService sendRequestAndRetryExecutorService = Executors.newSingleThreadExecutor();
                // Used to track our sent requests
                SequenceNumber sendSequenceNumber = SequenceNumber.DEFAULT;

                do {
                    try {
                        final Request request = requestQueue.take();
                        sendRequestAndRetry(
                                sendRequestAndRetryExecutorService,
                                request,
                                sendSequenceNumber
                        );
                        // Should ONLY modify send sequence number here!
                        sendSequenceNumber = sendSequenceNumber.next();
                    } catch (InterruptedException e) {
                        // shutdownNow() is called on `requestQueueProcessingThread`
                        sendRequestAndRetryExecutorService.shutdownNow();
                        break;
                    }
                } while (true);

                // Cleanup
                sendRequestAndRetryExecutorService.shutdownNow();
            }
        };
    }

    private final void sendRequestAndRetry(
            final ExecutorService executorService,
            final Request request,
            final SequenceNumber sequenceNumber
    ) {
        final Runnable task = new Runnable() {
            final Message message = request.toMessage(sequenceNumber.getValue());
            final SequenceNumber expectedAckSequenceNumber = sequenceNumber.next();

            @Override
            public void run() {
                try {
                    ioThread.write(message.encode());

                    // Wait for ACK. Ignore invalid sequence number.
                    do {
                        SequenceNumber ackSequenceNumber = receivedAckSequenceNumbers.take();
                        // Received ACK's sequence number should be flipped.
                        if (expectedAckSequenceNumber.equals(ackSequenceNumber)) {
                            break;
                        }
                        LOG.error(
                                "Receive unexpected sequence number. Expected: {}, got: {}.",
                                expectedAckSequenceNumber.getValue(),
                                ackSequenceNumber.getValue()
                        );
                    } while (true);
                } catch (InterruptedException e) {
                    // Timed-out
                }
            }
        };

        boolean sendSuccess = false;

        for (int tried = 0; tried < MAX_SEND_RETRIES; tried += 1) {
            final Future<?> future = executorService.submit(task);

            try {
                future.get(SEND_RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
            } catch (ExecutionException | InterruptedException e) {
                break;
            } catch (TimeoutException e) {
                LOG.error("Send request timed-out. Retry.");
                continue;
            } finally {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }

            sendSuccess = true;
            break;
        }

        if (!sendSuccess) {
            LOG.error("Cannot send request: {}.", request);
        }
    }

    private enum ProtocolImplInitializationState {
        NOT_INITIALIZED,
        INITIALIZING,
        INITIALIZED,
    }
}

/*  Copyright (C) 2021-2024 José Rebelo

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1;

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SONY_NOISE_OPTIMIZER_STATE_PRESSURE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SONY_NOISE_OPTIMIZER_STATUS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdatePreferences;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.SonyHeadphonesCapabilities;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AdaptiveVolumeControl;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AmbientSoundControl;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AmbientSoundControlButtonMode;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AudioUpsampling;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.AutomaticPowerOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.ButtonModes;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerCustomBands;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.EqualizerPreset;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.PauseWhenTakenOff;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.QuickAccess;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SoundPosition;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SpeakToChatConfig;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SpeakToChatEnabled;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.SurroundMode;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.TouchSensor;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.VoiceNotifications;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.headphones.prefs.WideAreaTap;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.deviceevents.SonyHeadphonesEnqueueRequestEvent;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.Request;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.MessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.AbstractSonyProtocolImpl;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.params.AudioCodec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.params.BatteryType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.params.NoiseCancellingOptimizerStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.impl.v1.params.VirtualSoundParam;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class SonyProtocolImplV1 extends AbstractSonyProtocolImpl {
    private static final Logger LOG = LoggerFactory.getLogger(SonyProtocolImplV1.class);

    public SonyProtocolImplV1(GBDevice device) {
        super(device);
    }

    @Override
    public Request getAmbientSoundControl() {
        return new Request(
                PayloadTypeV1.AMBIENT_SOUND_CONTROL_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AMBIENT_SOUND_CONTROL_GET.getCode(),
                        (byte) 0x02
                }
        );
    }

    @Override
    public Request setAmbientSoundControl(final AmbientSoundControl ambientSoundControl) {
        final ByteBuffer buf = ByteBuffer.allocate(8);

        buf.put(PayloadTypeV1.AMBIENT_SOUND_CONTROL_SET.getCode());
        buf.put((byte) 0x02);

        if (AmbientSoundControl.Mode.OFF.equals(ambientSoundControl.getMode())) {
            buf.put((byte) 0x00);
        } else {
            buf.put((byte) 0x11);
        }

        if (supportsWindNoiseCancelling()) {
            buf.put((byte) 0x02);

            switch (ambientSoundControl.getMode()) {
                case NOISE_CANCELLING:
                    buf.put((byte) 2);
                    break;
                case WIND_NOISE_REDUCTION:
                    buf.put((byte) 1);
                    break;
                case OFF:
                case AMBIENT_SOUND:
                default:
                    buf.put((byte) 0);
                    break;
            }
        } else {
            buf.put((byte) 0x00);

            if (AmbientSoundControl.Mode.NOISE_CANCELLING.equals(ambientSoundControl.getMode())) {
                buf.put((byte) 1);
            } else {
                buf.put((byte) 0);
            }
        }

        buf.put((byte) 0x01);  // ?
        buf.put((byte) (ambientSoundControl.isFocusOnVoice() ? 0x01 : 0x00));

        switch (ambientSoundControl.getMode()) {
            case OFF:
            case AMBIENT_SOUND:
                buf.put((byte) (ambientSoundControl.getAmbientSound()));
                break;
            case WIND_NOISE_REDUCTION:
            case NOISE_CANCELLING:
                buf.put((byte) 0);
                break;
        }

        return new Request(PayloadTypeV1.AMBIENT_SOUND_CONTROL_SET.getMessageType(), buf.array());
    }

    @Override
    public Request setAdaptiveVolumeControl(final AdaptiveVolumeControl config) {
        LOG.warn("Adaptive volume control not implemented for V1");
        return null;
    }

    @Override
    public Request getAdaptiveVolumeControl() {
        LOG.warn("Adaptive volume control not implemented for V1");
        return null;
    }

    @Override
    public Request setSpeakToChatEnabled(final SpeakToChatEnabled config) {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getCode(),
                        (byte) 0x05,
                        (byte) 0x01,
                        (byte) (config.isEnabled() ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request getSpeakToChatEnabled() {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getCode(),
                        (byte) 0x05
                }
        );
    }

    @Override
    public Request setSpeakToChatConfig(final SpeakToChatConfig config) {
        return new Request(
                PayloadTypeV1.SPEAK_TO_CHAT_CONFIG_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SPEAK_TO_CHAT_CONFIG_SET.getCode(),
                        (byte) 0x05,
                        (byte) 0x00,
                        config.getSensitivity().getCode(),
                        (byte) (config.isFocusOnVoice() ? 0x01 : 0x00),
                        config.getTimeout().getCode()
                }
        );
    }

    @Override
    public Request getSpeakToChatConfig() {
        return new Request(
                PayloadTypeV1.SPEAK_TO_CHAT_CONFIG_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SPEAK_TO_CHAT_CONFIG_GET.getCode(),
                        (byte) 0x05
                }
        );
    }

    @Override
    public Request getNoiseCancellingOptimizerState() {
        return new Request(
                PayloadTypeV1.NOISE_CANCELLING_OPTIMIZER_STATE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.NOISE_CANCELLING_OPTIMIZER_STATE_GET.getCode(),
                        (byte) 0x01
                }
        );
    }

    @Override
    public Request getAudioCodec() {
        return new Request(
                PayloadTypeV1.AUDIO_CODEC_REQUEST.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUDIO_CODEC_REQUEST.getCode(),
                        (byte) 0x00
                }
        );
    }

    @Override
    public Request getBattery(final BatteryType batteryType) {
        return new Request(
                PayloadTypeV1.BATTERY_LEVEL_REQUEST.getMessageType(),
                new byte[]{
                        PayloadTypeV1.BATTERY_LEVEL_REQUEST.getCode(),
                        encodeBatteryType(batteryType)
                }
        );
    }

    @Override
    public Request getFirmwareVersion() {
        return new Request(
                PayloadTypeV1.FW_VERSION_REQUEST.getMessageType(),
                new byte[]{
                        PayloadTypeV1.FW_VERSION_REQUEST.getCode(),
                        (byte) 0x02
                }
        );
    }

    @Override
    public Request getAudioUpsampling() {
        return new Request(
                PayloadTypeV1.AUDIO_UPSAMPLING_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUDIO_UPSAMPLING_GET.getCode(),
                        (byte) 0x02
                }
        );
    }

    @Override
    public Request setAudioUpsampling(final AudioUpsampling config) {
        return new Request(
                PayloadTypeV1.AUDIO_UPSAMPLING_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUDIO_UPSAMPLING_SET.getCode(),
                        (byte) 0x02,
                        (byte) 0x00,
                        (byte) (config.isEnabled() ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request getAutomaticPowerOff() {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getCode(),
                        (byte) 0x04
                }
        );
    }

    @Override
    public Request setAutomaticPowerOff(final AutomaticPowerOff config) {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getCode(),
                        (byte) 0x04,
                        (byte) 0x01,
                        config.getCode()[0],
                        config.getCode()[1]
                }
        );
    }

    @Override
    public Request setWideAreaTap(final WideAreaTap config) {
        LOG.warn("Adaptive volume control not implemented for V1");
        return null;
    }

    @Override
    public Request getWideAreaTap() {
        LOG.warn("Adaptive volume control not implemented for V1");
        return null;
    }

    public Request getButtonModes() {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getCode(),
                        (byte) 0x06
                }
        );
    }

    public Request setButtonModes(final ButtonModes config) {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getCode(),
                        (byte) 0x06,
                        (byte) 0x02,
                        encodeButtonMode(config.getModeLeft()),
                        encodeButtonMode(config.getModeRight())
                }
        );
    }

    @Override
    public Request getQuickAccess() {
        LOG.warn("Quick access not implemented for V1");
        return null;
    }

    @Override
    public Request setQuickAccess(final QuickAccess quickAccess) {
        LOG.warn("Quick access not implemented for V1");
        return null;
    }

    @Override
    public Request getAmbientSoundControlButtonMode() {
        LOG.warn("Ambient sound control button modes not implemented for V1");
        return null;
    }

    @Override
    public Request setAmbientSoundControlButtonMode(final AmbientSoundControlButtonMode ambientSoundControlButtonMode) {
        LOG.warn("Ambient sound control button modes not implemented for V1");
        return null;
    }

    @Override
    public Request getPauseWhenTakenOff() {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_GET.getCode(),
                        (byte) 0x03
                }
        );
    }

    @Override
    public Request setPauseWhenTakenOff(final PauseWhenTakenOff config) {
        return new Request(
                PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.AUTOMATIC_POWER_OFF_BUTTON_MODE_SET.getCode(),
                        (byte) 0x03,
                        (byte) 0x00,
                        (byte) (config.isEnabled() ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request getEqualizer() {
        return new Request(
                PayloadTypeV1.EQUALIZER_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.EQUALIZER_GET.getCode(),
                        (byte) 0x01
                }
        );
    }

    @Override
    public Request setEqualizerPreset(final EqualizerPreset config) {
        return new Request(
                PayloadTypeV1.EQUALIZER_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.EQUALIZER_SET.getCode(),
                        (byte) 0x01,
                        config.getCode(),
                        (byte) 0x00
                }
        );
    }

    @Override
    public Request setEqualizerCustomBands(final EqualizerCustomBands config) {
        final ByteBuffer buf = ByteBuffer.allocate(10);

        buf.put(PayloadTypeV1.EQUALIZER_SET.getCode());
        buf.put((byte) 0x01);
        buf.put((byte) 0xff);
        buf.put((byte) 0x06);

        buf.put((byte) (config.getBass() + 10));
        for (final Integer band : config.getBands()) {
            buf.put((byte) (band + 10));
        }

        return new Request(
                PayloadTypeV1.EQUALIZER_SET.getMessageType(),
                buf.array()
        );
    }

    @Override
    public Request getSoundPosition() {
        return new Request(
                PayloadTypeV1.SOUND_POSITION_OR_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SOUND_POSITION_OR_MODE_GET.getCode(),
                        (byte) 0x02
                }
        );
    }

    @Override
    public Request setSoundPosition(final SoundPosition config) {
        return new Request(
                PayloadTypeV1.SOUND_POSITION_OR_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SOUND_POSITION_OR_MODE_SET.getCode(),
                        VirtualSoundParam.SOUND_POSITION.getCode(),
                        config.getCode()
                }
        );
    }

    @Override
    public Request getSurroundMode() {
        return new Request(
                PayloadTypeV1.SOUND_POSITION_OR_MODE_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SOUND_POSITION_OR_MODE_GET.getCode(),
                        VirtualSoundParam.SURROUND_MODE.getCode()
                }
        );
    }

    @Override
    public Request setSurroundMode(final SurroundMode config) {
        return new Request(
                PayloadTypeV1.SOUND_POSITION_OR_MODE_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.SOUND_POSITION_OR_MODE_SET.getCode(),
                        VirtualSoundParam.SURROUND_MODE.getCode(),
                        config.getCode()
                }
        );
    }

    @Override
    public Request getTouchSensor() {
        return new Request(
                PayloadTypeV1.TOUCH_SENSOR_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.TOUCH_SENSOR_GET.getCode(),
                        (byte) 0xd2
                }
        );
    }

    @Override
    public Request setTouchSensor(final TouchSensor config) {
        return new Request(
                PayloadTypeV1.TOUCH_SENSOR_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.TOUCH_SENSOR_SET.getCode(),
                        (byte) 0xd2,
                        (byte) 0x01,
                        (byte) (config.isEnabled() ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request getVoiceNotifications() {
        return new Request(
                PayloadTypeV1.VOICE_NOTIFICATIONS_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.VOICE_NOTIFICATIONS_GET.getCode(),
                        (byte) 0x01,
                        (byte) 0x01
                }
        );
    }

    @Override
    public Request setVoiceNotifications(final VoiceNotifications config) {
        return new Request(
                PayloadTypeV1.VOICE_NOTIFICATIONS_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.VOICE_NOTIFICATIONS_SET.getCode(),
                        (byte) 0x01,
                        (byte) 0x01,
                        (byte) (config.isEnabled() ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request startNoiseCancellingOptimizer(final boolean start) {
        return new Request(
                PayloadTypeV1.NOISE_CANCELLING_OPTIMIZER_START.getMessageType(),
                new byte[]{
                        PayloadTypeV1.NOISE_CANCELLING_OPTIMIZER_START.getCode(),
                        (byte) 0x01,
                        (byte) 0x00,
                        (byte) (start ? 0x01 : 0x00)
                }
        );
    }

    @Override
    public Request powerOff() {
        return new Request(
                PayloadTypeV1.POWER_OFF.getMessageType(),
                new byte[]{
                        PayloadTypeV1.POWER_OFF.getCode(),
                        (byte) 0x00,
                        (byte) 0x01
                }
        );
    }

    @Override
    public Request getVolume() {
        return new Request(
                PayloadTypeV1.VOLUME_GET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.VOLUME_GET.getCode(),
                        (byte) 0x01,
                        (byte) 0x20
                }
        );
    }

    @Override
    public Request setVolume(final int volume) {
        return new Request(
                PayloadTypeV1.VOLUME_SET.getMessageType(),
                new byte[]{
                        PayloadTypeV1.VOLUME_SET.getCode(),
                        (byte) 0x01,
                        (byte) 0x20,
                        (byte) volume
                }
        );
    }

    @Override
    public List<? extends GBDeviceEvent> handlePayload(final MessageType messageType, final byte[] payload) {
        final PayloadTypeV1 payloadType = PayloadTypeV1.fromCode(messageType, payload[0]);

        switch (payloadType) {
            case INIT_REPLY:
                return handleInitResponse(payload);
            case FW_VERSION_REPLY:
                return handleFirmwareVersion(payload);
            case BATTERY_LEVEL_REPLY:
            case BATTERY_LEVEL_NOTIFY:
                return handleBattery(payload);
            case AUDIO_CODEC_REPLY:
            case AUDIO_CODEC_NOTIFY:
                return handleAudioCodec(payload);
            case SOUND_POSITION_OR_MODE_RET:
            case SOUND_POSITION_OR_MODE_NOTIFY:
                return handleVirtualSound(payload);
            case EQUALIZER_RET:
            case EQUALIZER_NOTIFY:
                return handleEqualizer(payload);
            case AMBIENT_SOUND_CONTROL_RET:
            case AMBIENT_SOUND_CONTROL_NOTIFY:
                return handleAmbientSoundControl(payload);
            case VOLUME_RET:
            case VOLUME_NOTIFY:
                return handleVolume(payload);
            case NOISE_CANCELLING_OPTIMIZER_STATUS:
                return handleNoiseCancellingOptimizerStatus(payload);
            case NOISE_CANCELLING_OPTIMIZER_STATE_RET:
            case NOISE_CANCELLING_OPTIMIZER_STATE_NOTIFY:
                return handleNoiseCancellingOptimizerState(payload);
            case TOUCH_SENSOR_RET:
            case TOUCH_SENSOR_NOTIFY:
                return handleTouchSensor(payload);
            case AUDIO_UPSAMPLING_RET:
            case AUDIO_UPSAMPLING_NOTIFY:
                return handleAudioUpsampling(payload);
            case AUTOMATIC_POWER_OFF_BUTTON_MODE_RET:
            case AUTOMATIC_POWER_OFF_BUTTON_MODE_NOTIFY:
                return handleAutomaticPowerOffButtonMode(payload);
            case SPEAK_TO_CHAT_CONFIG_RET:
            case SPEAK_TO_CHAT_CONFIG_NOTIFY:
                return handleSpeakToChatConfig(payload);
            case VOICE_NOTIFICATIONS_RET:
            case VOICE_NOTIFICATIONS_NOTIFY:
                return handleVoiceNotifications(payload);
            case JSON_RET:
                return handleJson(payload);
        }

        LOG.warn("Unhandled payload type code {}", String.format("%02x", payload[0]));

        return Collections.emptyList();
    }

    public List<? extends GBDeviceEvent> handleInitResponse(final byte[] payload) {
        // Populate the init requests
        final List<Request> capabilityRequests = new ArrayList<>();

        capabilityRequests.add(getFirmwareVersion());
        capabilityRequests.add(getAudioCodec());

        final Map<SonyHeadphonesCapabilities, Request> capabilityRequestMap = new LinkedHashMap<SonyHeadphonesCapabilities, Request>() {{
            put(SonyHeadphonesCapabilities.BatterySingle, getBattery(BatteryType.SINGLE));
            put(SonyHeadphonesCapabilities.BatteryDual, getBattery(BatteryType.DUAL));
            put(SonyHeadphonesCapabilities.BatteryDual2, getBattery(BatteryType.DUAL2));
            put(SonyHeadphonesCapabilities.BatteryCase, getBattery(BatteryType.CASE));
            put(SonyHeadphonesCapabilities.AmbientSoundControl, getAmbientSoundControl());
            put(SonyHeadphonesCapabilities.AmbientSoundControl2, getAmbientSoundControl());
            put(SonyHeadphonesCapabilities.AncOptimizer, getNoiseCancellingOptimizerState());
            put(SonyHeadphonesCapabilities.AudioUpsampling, getAudioUpsampling());
            put(SonyHeadphonesCapabilities.ButtonModesLeftRight, getButtonModes());
            put(SonyHeadphonesCapabilities.VoiceNotifications, getVoiceNotifications());
            put(SonyHeadphonesCapabilities.AutomaticPowerOffWhenTakenOff, getAutomaticPowerOff());
            put(SonyHeadphonesCapabilities.AutomaticPowerOffByTime, getAutomaticPowerOff());
            put(SonyHeadphonesCapabilities.TouchSensorSingle, getTouchSensor());
            put(SonyHeadphonesCapabilities.EqualizerSimple, getEqualizer());
            put(SonyHeadphonesCapabilities.EqualizerWithCustomBands, getEqualizer());
            put(SonyHeadphonesCapabilities.SoundPosition, getSoundPosition());
            put(SonyHeadphonesCapabilities.SurroundMode, getSurroundMode());
            put(SonyHeadphonesCapabilities.SpeakToChatEnabled, getSpeakToChatEnabled());
            put(SonyHeadphonesCapabilities.SpeakToChatConfig, getSpeakToChatConfig());
            put(SonyHeadphonesCapabilities.PauseWhenTakenOff, getPauseWhenTakenOff());
            put(SonyHeadphonesCapabilities.AmbientSoundControlButtonMode, getAmbientSoundControlButtonMode());
            put(SonyHeadphonesCapabilities.QuickAccess, getQuickAccess());
            put(SonyHeadphonesCapabilities.Volume, getVolume());
            put(SonyHeadphonesCapabilities.AdaptiveVolumeControl, getAdaptiveVolumeControl());
            put(SonyHeadphonesCapabilities.WideAreaTap, getWideAreaTap());
        }};

        for (Map.Entry<SonyHeadphonesCapabilities, Request> capabilityEntry : capabilityRequestMap.entrySet()) {
            if (supports(capabilityEntry.getKey())) {
                capabilityRequests.add(capabilityEntry.getValue());
            }
        }

        // Remove any requests that are not supported by other protocol version
        capabilityRequests.removeAll(Collections.singleton(null));

        return Collections.singletonList(new SonyHeadphonesEnqueueRequestEvent(capabilityRequests));
    }

    public List<? extends GBDeviceEvent> handleAmbientSoundControl(final byte[] payload) {
        if (payload.length != 8) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        AmbientSoundControl.Mode mode = null;

        if (payload[2] == (byte) 0x00) {
            mode = AmbientSoundControl.Mode.OFF;
        } else if (payload[2] == (byte) 0x01) {
            // Enabled, determine mode

            if (payload[3] == 0x00) {
                // Only ANC  and Ambient Sound supported?
                if (payload[4] == (byte) 0x00) {
                    mode = AmbientSoundControl.Mode.AMBIENT_SOUND;
                } else if (payload[4] == (byte) 0x01) {
                    mode = AmbientSoundControl.Mode.NOISE_CANCELLING;
                }
            } else if (payload[3] == 0x02) {
                // Supports wind noise reduction
                if (payload[4] == (byte) 0x00) {
                    mode = AmbientSoundControl.Mode.AMBIENT_SOUND;
                } else if (payload[4] == (byte) 0x01) {
                    mode = AmbientSoundControl.Mode.WIND_NOISE_REDUCTION;
                } else if (payload[4] == (byte) 0x02) {
                    mode = AmbientSoundControl.Mode.NOISE_CANCELLING;
                }
            }
        }

        if (mode == null) {
            LOG.warn("Unable to determine ambient sound control mode from {}", GB.hexdump(payload));
            return Collections.emptyList();
        }

        final Boolean focusOnVoice = booleanFromByte(payload[6]);
        if (focusOnVoice == null) {
            LOG.warn("Unknown focus on voice mode {}", String.format("%02x", payload[6]));
            return Collections.emptyList();
        }

        int ambientSound = payload[7];
        if (ambientSound < 0 || ambientSound > 20) {
            LOG.warn("Ambient sound level {} is out of range", String.format("%02x", payload[7]));
            return Collections.emptyList();
        }

        final AmbientSoundControl ambientSoundControl = new AmbientSoundControl(mode, focusOnVoice, ambientSound);

        LOG.debug("Ambient sound control: {}", ambientSoundControl);

        final GBDeviceEventUpdatePreferences eventUpdatePreferences = new GBDeviceEventUpdatePreferences()
                .withPreferences(ambientSoundControl.toPreferences());

        return Collections.singletonList(eventUpdatePreferences);
    }

    public List<? extends GBDeviceEvent> handleVolume(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        AmbientSoundControl.Mode mode = null;

        if (payload[1] != (byte) 0x01) {
            LOG.warn("Unexpected byte at position 1 for volume: {}", payload[1]);
            return Collections.emptyList();
        }
        if (payload[2] != (byte) 0x20) {
            LOG.warn("Unexpected byte at position 2 for volume: {}", payload[1]);
            return Collections.emptyList();
        }

        final int volume = payload[3];
        if (volume < 0 || volume > 30) {
            LOG.warn("Volume {} is out of range", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        LOG.debug("Volume: {}", volume);

        final GBDeviceEventUpdatePreferences eventUpdatePreferences = new GBDeviceEventUpdatePreferences()
                .withPreference(DeviceSettingsPreferenceConst.PREF_VOLUME, volume);

        return Collections.singletonList(eventUpdatePreferences);
    }

    public List<? extends GBDeviceEvent> handleNoiseCancellingOptimizerStatus(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final NoiseCancellingOptimizerStatus status = NoiseCancellingOptimizerStatus.fromCode(payload[3]);
        if (status == null) {
            LOG.warn("Unable to determine noise cancelling opptimizer status from {}", GB.hexdump(payload));
            return Collections.emptyList();
        }

        LOG.info("Noise Cancelling Optimizer status: {}", status);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreference(PREF_SONY_NOISE_OPTIMIZER_STATUS, status.name().toLowerCase(Locale.ROOT));

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleNoiseCancellingOptimizerState(final byte[] payload) {
        // 89 01 01 01 01 0A
        if (payload.length != 6) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final float pressure = payload[5] / 10.0f;

        if (pressure <= 0 || pressure > 1.0f) {
            LOG.warn("Invalid Noise Cancelling Optimizer pressure: {} atm, ignoring", pressure);
            return Collections.emptyList();
        }

        LOG.info("Noise Cancelling Optimizer pressure: {} atm", pressure);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreference(PREF_SONY_NOISE_OPTIMIZER_STATE_PRESSURE, String.format(Locale.getDefault(), "%.2f atm", pressure));

        return Collections.singletonList(event);
    }


    public List<? extends GBDeviceEvent> handleAudioUpsampling(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final Boolean enabled = booleanFromByte(payload[3]);
        if (enabled == null) {
            LOG.warn("Unknown audio upsampling code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        LOG.debug("Audio Upsampling: {}", enabled);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new AudioUpsampling(enabled).toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleAutomaticPowerOff(final byte[] payload) {
        if (payload.length != 5) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        if (payload[1] != 0x04) {
            // TODO: Handle these (Button Mode uses the same payload type?)
            LOG.warn("Not automatic power off config, ignoring");
            return Collections.emptyList();
        }

        final AutomaticPowerOff mode = AutomaticPowerOff.fromCode(payload[3], payload[4]);
        if (mode == null) {
            LOG.warn("Unknown automatic power off codes {}", String.format("%02x %02x", payload[3], payload[4]));
            return Collections.emptyList();
        }

        LOG.debug("Automatic Power Off: {}", mode);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(mode.toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleSpeakToChatEnabled(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        if (payload[2] != 0x01) {
            // TODO: Handle these, setting speak to chat sends a 0x02 back
            LOG.warn("Not speak to chat enabled, ignoring");
            return Collections.emptyList();
        }

        final Boolean enabled = booleanFromByte(payload[3]);
        if (enabled == null) {
            LOG.warn("Unknown speak to chat code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        LOG.debug("Speak to chat: {}", enabled);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new SpeakToChatEnabled(enabled).toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleButtonModes(final byte[] payload) {
        if (payload.length != 5) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final ButtonModes.Mode modeLeft = decodeButtonMode(payload[3]);
        final ButtonModes.Mode modeRight = decodeButtonMode(payload[4]);
        if (modeLeft == null || modeRight == null) {
            LOG.warn("Unknown button mode codes {}", String.format("%02x %02x", payload[3], payload[4]));
            return Collections.emptyList();
        }

        LOG.debug("Button Modes: L: {}, R: {}", modeLeft, modeRight);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new ButtonModes(modeLeft, modeRight).toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handlePauseWhenTakenOff(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final Boolean enabled = booleanFromByte(payload[3]);
        if (enabled == null) {
            LOG.warn("Unknown pause when taken off code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        LOG.debug("Pause when taken off: {}", enabled);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new PauseWhenTakenOff(enabled).toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleBattery(final byte[] payload) {
        final BatteryType batteryType = decodeBatteryType(payload[1]);

        if (batteryType == null) {
            LOG.warn("Unknown battery type code {}", String.format("%02x", payload[1]));
            return Collections.emptyList();
        }

        final List<GBDeviceEventBatteryInfo> batteryEvents = new ArrayList<>();

        if (BatteryType.SINGLE.equals(batteryType) || BatteryType.CASE.equals(batteryType)) {
            // Single battery / Case battery
            LOG.debug("Battery Level: {}: {}", batteryType, payload[2]);

            final GBDeviceEventBatteryInfo singleBatteryInfo = new GBDeviceEventBatteryInfo();
            singleBatteryInfo.batteryIndex = 0;
            singleBatteryInfo.level = payload[2];
            singleBatteryInfo.state = payload[3] == 1 ? BatteryState.BATTERY_CHARGING : BatteryState.BATTERY_NORMAL;

            batteryEvents.add(singleBatteryInfo);
        } else if (BatteryType.DUAL.equals(batteryType) || BatteryType.DUAL2.equals(batteryType)) {
            // Dual Battery (L / R)
            LOG.debug("Battery Level: {}: L: {}, R: {}", batteryType, payload[2], payload[4]);

            boolean hasCaseBattery = supports(SonyHeadphonesCapabilities.BatteryCase);

            if (payload[2] != 0) {
                final GBDeviceEventBatteryInfo gbDeviceEventBatteryInfoLeft = new GBDeviceEventBatteryInfo();

                gbDeviceEventBatteryInfoLeft.batteryIndex = hasCaseBattery ? 1 : 0;
                gbDeviceEventBatteryInfoLeft.level = payload[2];
                gbDeviceEventBatteryInfoLeft.state = payload[3] == 1 ? BatteryState.BATTERY_CHARGING : BatteryState.BATTERY_NORMAL;

                batteryEvents.add(gbDeviceEventBatteryInfoLeft);
            }

            if (payload[4] != 0) {
                final GBDeviceEventBatteryInfo gbDeviceEventBatteryInfoRight = new GBDeviceEventBatteryInfo();

                gbDeviceEventBatteryInfoRight.batteryIndex = hasCaseBattery ? 2 : 1;
                gbDeviceEventBatteryInfoRight.level = payload[4];
                gbDeviceEventBatteryInfoRight.state = payload[5] == 1 ? BatteryState.BATTERY_CHARGING : BatteryState.BATTERY_NORMAL;

                batteryEvents.add(gbDeviceEventBatteryInfoRight);
            }
        }

        return batteryEvents;
    }

    public List<? extends GBDeviceEvent> handleAudioCodec(final byte[] payload) {
        if (payload.length != 3) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        if (payload[1] != 0x00) {
            LOG.warn("Not audio codec, ignoring");
            return Collections.emptyList();
        }

        final AudioCodec audioCodec = AudioCodec.fromCode(payload[2]);
        if (audioCodec == null) {
            LOG.warn("Unable to determine audio codec from {}", GB.hexdump(payload));
            return Collections.emptyList();
        }

        final GBDeviceEventUpdateDeviceInfo gbDeviceEventUpdateDeviceInfo = new GBDeviceEventUpdateDeviceInfo("AUDIO_CODEC: ", audioCodec.name());

        final GBDeviceEventUpdatePreferences gbDeviceEventUpdatePreferences = new GBDeviceEventUpdatePreferences()
                .withPreference(DeviceSettingsPreferenceConst.PREF_SONY_AUDIO_CODEC, audioCodec.name().toLowerCase(Locale.getDefault()));

        return Arrays.asList(gbDeviceEventUpdateDeviceInfo, gbDeviceEventUpdatePreferences);
    }

    public List<? extends GBDeviceEvent> handleEqualizer(final byte[] payload) {
        if (payload.length != 10) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final EqualizerPreset mode = EqualizerPreset.fromCode(payload[2]);
        if (mode == null) {
            LOG.warn("Unknown equalizer preset code {}", String.format("%02x", payload[2]));
            return Collections.emptyList();
        }

        LOG.debug("Equalizer Preset: {}", mode);

        final int clearBass = payload[4] - 10;
        final List<Integer> bands = new ArrayList<>(5);

        for (int i = 0; i < 5; i++) {
            bands.add(payload[5 + i] - 10);
        }

        final EqualizerCustomBands customBands = new EqualizerCustomBands(bands, clearBass);

        LOG.info("Equalizer Custom Bands: {}", customBands);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(mode.toPreferences())
                .withPreferences(customBands.toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleFirmwareVersion(final byte[] payload) {
        final Pattern VERSION_REGEX = Pattern.compile("^[0-9.\\-a-zA-Z_]+$");

        if (payload[1] != 0x02) {
            LOG.warn("Not firmware version, ignoring");
            return Collections.emptyList();
        }

        if (payload.length < 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final String firmwareVersion = new String(Arrays.copyOfRange(payload, 3, payload.length));

        final int expectedLength = payload[2] & 0xff;
        if (firmwareVersion.length() != expectedLength) {
            LOG.warn("Unexpected firmware version length {}, expected {}", firmwareVersion.length(), expectedLength);
            return Collections.emptyList();
        }

        if (!VERSION_REGEX.matcher(firmwareVersion).find()) {
            LOG.warn("Unexpected characters in version '{}'", firmwareVersion);
            return Collections.emptyList();
        }

        LOG.debug("Firmware Version: {}", firmwareVersion);

        final GBDeviceEventVersionInfo gbDeviceEventVersionInfo = new GBDeviceEventVersionInfo();
        gbDeviceEventVersionInfo.fwVersion = firmwareVersion;
        return Collections.singletonList(gbDeviceEventVersionInfo);
    }

    public List<? extends GBDeviceEvent> handleJson(final byte[] payload) {
        // TODO analyze json?

        if (payload.length < 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final String jsonString = new String(Arrays.copyOfRange(payload, 4, payload.length));

        LOG.debug("Got json: {}", jsonString);

        return Collections.emptyList();
    }

    public List<? extends GBDeviceEvent> handleAutomaticPowerOffButtonMode(final byte[] payload) {
        switch (payload[1]) {
            case 0x04:
                return handleAutomaticPowerOff(payload);
            case 0x03:
                return handlePauseWhenTakenOff(payload);
            case 0x05:
                return handleSpeakToChatEnabled(payload);
            case 0x06:
                return handleButtonModes(payload);
        }

        return Collections.emptyList();
    }

    public List<? extends GBDeviceEvent> handleVirtualSound(final byte[] payload) {
        if (payload.length != 3) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        final VirtualSoundParam virtualSoundParam = VirtualSoundParam.fromCode(payload[1]);
        if (virtualSoundParam == null) {
            LOG.warn("Unknown payload subtype code {}", String.format("%02x", payload[1]));
            return Collections.emptyList();
        }

        switch (virtualSoundParam) {
            case SURROUND_MODE:
                return handleSurroundMode(payload);
            case SOUND_POSITION:
                return handleSoundPosition(payload);
        }

        return Collections.emptyList();
    }

    public List<? extends GBDeviceEvent> handleSoundPosition(final byte[] payload) {
        if (payload.length != 3) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        SoundPosition mode = null;

        for (SoundPosition value : SoundPosition.values()) {
            if (value.getCode() == payload[2]) {
                mode = value;
                break;
            }
        }

        if (mode == null) {
            LOG.warn("Unknown sound position code {}", String.format("%02x", payload[2]));
            return Collections.emptyList();
        }

        LOG.debug("Sound Position: {}", mode);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(mode.toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleSurroundMode(final byte[] payload) {
        if (payload.length != 3) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        SurroundMode mode = null;

        for (SurroundMode value : SurroundMode.values()) {
            if (value.getCode() == payload[2]) {
                mode = value;
                break;
            }
        }

        if (mode == null) {
            LOG.warn("Unknown surround mode code {}", String.format("%02x", payload[2]));
            return Collections.emptyList();
        }

        LOG.debug("Surround Mode: {}", mode);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(mode.toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleTouchSensor(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        boolean enabled;

        switch (payload[3]) {
            case 0x00:
                enabled = false;
                break;
            case 0x01:
                enabled = true;
                break;
            default:
                LOG.warn("Unknown touch sensor code {}", String.format("%02x", payload[3]));
                return Collections.emptyList();
        }

        LOG.debug("Touch Sensor: {}", enabled);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new TouchSensor(enabled).toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleSpeakToChatConfig(final byte[] payload) {
        if (payload.length != 6) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        if (payload[1] != 0x05) {
            LOG.warn("Not speak to chat config, ignoring");
            return Collections.emptyList();
        }

        final SpeakToChatConfig.Sensitivity sensitivity = SpeakToChatConfig.Sensitivity.fromCode(payload[3]);
        if (sensitivity == null) {
            LOG.warn("Unknown sensitivity code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        final Boolean focusOnVoice = booleanFromByte(payload[4]);
        if (focusOnVoice == null) {
            LOG.warn("Unknown focus on voice code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        final SpeakToChatConfig.Timeout timeout = SpeakToChatConfig.Timeout.fromCode(payload[5]);
        if (timeout == null) {
            LOG.warn("Unknown timeout code {}", String.format("%02x", payload[3]));
            return Collections.emptyList();
        }

        final SpeakToChatConfig speakToChatConfig = new SpeakToChatConfig(focusOnVoice, sensitivity, timeout);

        LOG.debug("Speak to chat config: {}", speakToChatConfig);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(speakToChatConfig.toPreferences());

        return Collections.singletonList(event);
    }

    public List<? extends GBDeviceEvent> handleVoiceNotifications(final byte[] payload) {
        if (payload.length != 4) {
            LOG.warn("Unexpected payload length {}", payload.length);
            return Collections.emptyList();
        }

        boolean enabled;

        switch (payload[3]) {
            case 0x00:
                enabled = false;
                break;
            case 0x01:
                enabled = true;
                break;
            default:
                LOG.warn("Unknown voice notifications code {}", String.format("%02x", payload[3]));
                return Collections.emptyList();
        }

        LOG.debug("Voice Notifications: {}", enabled);

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreferences(new VoiceNotifications(enabled).toPreferences());

        return Collections.singletonList(event);
    }

    protected static Boolean booleanFromByte(final byte b) {
        switch (b) {
            case 0x00:
                return false;
            case 0x01:
                return true;
            default:
        }

        return null;
    }

    protected boolean supportsWindNoiseCancelling() {
        return supports(SonyHeadphonesCapabilities.WindNoiseReduction);
    }

    protected BatteryType decodeBatteryType(final byte b) {
        switch (b) {
            case 0x00:
                return BatteryType.SINGLE;
            case 0x01:
                return BatteryType.DUAL;
            case 0x02:
                return BatteryType.CASE;
        }

        return null;
    }

    protected byte encodeBatteryType(final BatteryType batteryType) {
        switch (batteryType) {
            case SINGLE:
                return 0x00;
            case DUAL:
            case DUAL2: // FIXME this is a workaround to fix the initialization - DUAL2 is not supported by V1
                return 0x01;
            case CASE:
                return 0x02;
        }

        throw new IllegalArgumentException("Unknown battery type " + batteryType);
    }

    protected ButtonModes.Mode decodeButtonMode(final byte b) {
        switch (b) {
            case (byte) 0xff:
                return ButtonModes.Mode.OFF;
            case (byte) 0x00:
                return ButtonModes.Mode.AMBIENT_SOUND_CONTROL;
            case (byte) 0x20:
                return ButtonModes.Mode.PLAYBACK_CONTROL;
            case (byte) 0x10:
                return ButtonModes.Mode.VOLUME_CONTROL;
        }

        return null;
    }

    protected byte encodeButtonMode(final ButtonModes.Mode buttonMode) {
        switch (buttonMode) {
            case OFF:
                return (byte) 0xff;
            case AMBIENT_SOUND_CONTROL:
                return (byte) 0x00;
            case PLAYBACK_CONTROL:
                return (byte) 0x20;
            case VOLUME_CONTROL:
                return (byte) 0x10;
        }

        throw new IllegalArgumentException("Unknown button mode " + buttonMode);
    }
}

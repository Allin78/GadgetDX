-- Copyright (C) 2021 José Rebelo
--
-- This file is part of Gadgetbridge-tools.
--
-- Gadgetbridge is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published
-- by the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- Gadgetbridge is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.

sony_proto = Proto("Sony",  "Sony Headphones Protocol")

sony_proto.fields.message_type = ProtoField.uint8("sony.message_type", "Message Type", base.HEX)
sony_proto.fields.message_checksum = ProtoField.uint8("sony.message_checksum", "Message Checksum", base.HEX)
sony_proto.fields.sequence_number = ProtoField.uint8("sony.sequence_number", "Sequence Number", base.DEC)
sony_proto.fields.payload_length = ProtoField.int32("sony.payload_length", "Payload Length", base.DEC)
sony_proto.fields.message_payload = ProtoField.bytes("sony.message_payload", "Payload", base.COLON)
sony_proto.fields.message_payload_type = ProtoField.string("sony.message_payload_type", "Payload Type", base.TEXT)
sony_proto.fields.message_payload_subtype = ProtoField.string("sony.message_payload_subtype", "Payload Subtype", base.TEXT)

function handle_init_reply(payload, subtree)
    -- TODO ?
    return
end

function handle_text_subpayload(label)
    return function(payload, subtree)
        subpayload_len = payload(2, 1):uint()
        if subpayload_len + 3 ~= payload:len() then
            subtree:add_expert_info(PI_DEBUG, PI_ERROR, "Unexpected payload size")
        end
        subtree:add(payload(3, subpayload_len), label .. " = " .. payload(3, subpayload_len):string())
    end
end

function handle_boolean_payload(label)
    return function(payload, subtree)
        if payload:len() ~= 4 then
            subtree:add_expert_info(PI_DEBUG, PI_ERROR, "Unexpected payload size")
        end

        if payload(3, 1):uint() == 0 then
            subtree:add(payload(3, 1), label .. " = false")
        elseif payload(3, 1):uint() == 1 then
            subtree:add(payload(3, 1), label .. " = true")
        else
            subtree:add_expert_info(PI_DEBUG, PI_ERROR, "Unknown value for Boolean")
            subtree:add(payload(3, 1), label .. " = " .. payload(3, 1):uint() .. ' (Unknown)')
        end
    end
end

function handle_battery_single(payload, subtree)
    if payload:len() ~= 4 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end
    subtree:add(payload(2, 2), "Battery = " ..payload(2, 1):uint() .. "%")

    if payload(3, 1):uint() == 0 then
        subtree:add(payload(3, 1), "Charging = false")
    elseif payload(3, 1):uint() == 1 then
        subtree:add(payload(3, 1), "Charging = true")
    else
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected value for boolean")
        subtree:add(payload(3, 1), "Charging = true")
    end
end

function handle_battery_multi(payload, subtree)
    if payload:len() ~= 6 or payload(3, 1):uint() ~= 0 or payload(5, 1):uint() ~= 0 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end
    subtree:add(payload(2, 1), "Battery L = " .. payload(2, 1):uint() .. "%")
    subtree:add(payload(4, 1), "Battery R = " .. payload(4, 1):uint() .. "%")
end

function handle_button_mode(payload, subtree)
    if payload:len() ~= 5 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length")
    end

    button_modes = {
        [0xff] = "Off",
        [0x00] = "Ambient Sound Control",
        [0x20] = "Playback Control",
        [0x10] = "Volume Control",
    }

    -- TODO Number of buttons in byte 2?
    subtree:add(payload(3, 1), "L = " .. button_modes[payload(3, 1):uint()])
    subtree:add(payload(4, 1), "R = " .. button_modes[payload(4, 1):uint()])
end

function handle_automatic_power_off(payload, subtree)
    if payload:len() ~= 5 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length")
    end

    automatic_power_off_modes = {
        [0x1100] = "Off",
        [0x0000] = "After 3 min",
        [0x0101] = "After 30 min",
        [0x0202] = "After 1 hour",
        [0x0303] = "After 3 hour",
        [0x1000] = "When taken off",
    }

    subtree:add(payload(3, 1), "Mode = " .. automatic_power_off_modes[payload(3, 2):uint()])
end

function handle_audio_codec(payload, subtree)
    if payload:len() ~= 3 or payload(1, 1):uint() ~= 0 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end

    audio_codecs = {
        [0x00] = "UNKNOWN", -- Sometimes it's 0?
        [0x01] = "SBC",
        [0x02] = "AAC",
        [0x10] = "LDAC",
        [0x20] = "APTX",
        [0x21] = "APTX_HD",
    }

    subtree:add(payload(2, 1), "Codec = " .. audio_codecs[payload(2, 1):uint()])
end

function handle_media(payload, subtree)
    if payload:len() ~= 4 or payload(1, 1):uint() ~= 1 or payload(2, 1):uint() ~= 0 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end

    media_commands = {
        [0x07] = "Play",
        [0x01] = "Pause",
        [0x02] = "Next",
        [0x03] = "Prev",
    }

    subtree:add(payload(3, 1), "Command = " .. media_commands[payload(3, 1):uint()])
end

function handle_volume(payload, subtree)
    if payload:len() ~= 4 or payload(1, 1):uint() ~= 0x01 or payload(2, 1):uint() ~= 0x20 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end

    subtree:add(payload(3, 1), "Volume = " .. payload(3, 1):uint())
end

function handle_surround_mode(payload, subtree)
    if payload:len() ~= 3 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length")
    end

    surround_modes = {
        [0x00] = "Off",
        [0x01] = "Outdoor Stage",
        [0x02] = "Arena",
        [0x03] = "Concert Hall",
        [0x04] = "Club",
    }

    subtree:add(payload(2, 1), "Mode = " .. surround_modes[payload(2, 1):uint()])
end

function handle_sound_position(payload, subtree)
    if payload:len() ~= 3 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length")
    end

    sound_positions = {
        [0x00] = "Off",
        [0x01] = "Front Left",
        [0x02] = "Front Right",
        [0x03] = "Front",
        [0x11] = "Rear Left",
        [0x12] = "Rear Right",
    }

    subtree:add(payload(2, 1), "Mode = " .. sound_positions[payload(2, 1):uint()])
end

function handle_equalizer(payload, subtree)
    if payload:len() ~= 4 and payload:len() ~= 10 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length or value")
    end

    equalizer_modes = {
        [0x00] = "Off",
        [0x10] = "Bright",
        [0x11] = "Excited",
        [0x12] = "Mellow",
        [0x13] = "Relaxed",
        [0x14] = "Vocal",
        [0x15] = "Treble Boost",
        [0x16] = "Bass Boost",
        [0x17] = "Speech",
        [0xa0] = "Manual",
        [0xa1] = "Custom 1",
        [0xa2] = "Custom 1",
        [0xff] = "Set Bands", -- Not a mode, sets the bands for the current custom preset
    }

    -- TODO unhandled bytes?
    subtree:add(payload(2, 1), "Mode = " .. equalizer_modes[payload(2, 1):uint()])

    if payload:len() ~= 10 then
        return
    end

    subtree:add(payload(4, 1), "Bass = " .. (payload(4, 1):uint() - 10))

    bands_str = payload(5, 1):uint() - 10
    for i = 1, 4 do
        bands_str = bands_str .. ", " .. (payload(5 + i, 1):uint() - 10)
    end
    subtree:add(payload(5, 5), "Bands = " .. bands_str)
end

function handle_ambient_sound_control(payload, subtree)
    if payload:len() ~= 8 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected payload length")
    end

    local mode = nil
    if payload(2, 1):uint() == 0x00 then
        mode = "OFF"
    elseif payload(2, 1):uint() == 0x01 or payload(2, 1):uint() == 0x10 or payload(2, 1):uint() == 0x11 then
        -- position 2: 0x01 = ON, 0x10 = while dragging slider, 0x11 = actually set, finished
        -- Enabled, determine mode

        if payload(3, 1):uint() == 0x00 then
            -- Only ANC  and Ambient Sound supported?
            if payload(4, 1):uint() == 0x00 then
                mode = "Ambient Sound"
            elseif payload(4, 1):uint() == 0x01 then
                mode = "Noise Cancelling"
            end
        elseif payload(3, 1):uint() == 0x02 then
            -- Supports wind noise reduction
            if payload(4, 1):uint() == 0x00 then
                mode = "Ambient Sound"
            elseif payload(4, 1):uint() == 0x01 then
                mode = "Wind Noise Reduction"
            elseif payload(4, 1):uint() == 0x02 then
                mode = "Noise Cancelling"
            end
        end
    end

    if mode ~= nil then
        subtree:add(payload(2, 1), "Mode = " .. mode)
    else
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unknown Ambient Sound Mode")
    end

    -- TODO: How to bool?
    if payload(6, 1):uint() == 0 then
        subtree:add(payload(6, 1), "Focus on Voice = false")
    elseif payload(6, 1):uint() == 1 then
        subtree:add(payload(6, 1), "Focus on Voice = true")
    else
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected value for boolean")
        subtree:add(payload(6, 1), "Focus on Voice = " .. payload(6, 1):uint() .. ' (Unknown)')
    end

    subtree:add(payload(7, 1), "Ambient Sound = " .. payload(7, 1):uint())
end

payload_types_1 = {
    -- Init
    [0x00] = { "INIT_REQUEST" },
    [0x01] = { "INIT_REPLY", handle_init_reply },

    -- Device Info
    [0x04] = { "DEVICE_INFO_REQUEST", {
        [0x01] = { "DEVICE_MODEL_REQUEST" },
        [0x02] = { "FW_VERSION_REQUEST" },
    } },
    [0x05] = { "DEVICE_INFO_REPLY", {
        [0x01] = { "DEVICE_MODEL_REPLY", handle_text_subpayload("Model") },
        [0x02] = { "FW_VERSION_REPLY", handle_text_subpayload("Firmware Version") },
    } },

    -- Battery Level
    [0x10] = { "BATTERY_LEVEL_GET", {
        [0x00] = { "SINGLE" },
        [0x01] = { "MULTI" },
        [0x02] = { "CASE" },
    } },
    [0x11] = { "BATTERY_LEVEL_RET", {
        [0x00] = { "SINGLE", handle_battery_single },
        [0x01] = { "MULTI", handle_battery_multi },
        [0x02] = { "CASE", handle_battery_single },
    } },
    [0x13] = { "BATTERY_LEVEL_NOTIFY", {
        [0x00] = { "SINGLE", handle_battery_single },
        [0x01] = { "MULTI", handle_battery_multi },
        [0x02] = { "CASE", handle_battery_single },
    } },

    -- Audio Codec
    [0x18] = { "AUDIO_CODEC_GET" },
    [0x19] = { "AUDIO_CODEC_RET", handle_audio_codec },
    [0x1b] = { "AUDIO_CODEC_NOTIFY", handle_audio_codec },

    -- Sound Position / Surround Mode
    -- TODO: There are more subpayloads
    [0x46] = { "SOUND_POSITION_OR_SURROUND_MODE_GET", {
        [0x01] = { "SURROUND" },
        [0x01] = { "POSITION" },
    } },
    [0x47] = { "SOUND_POSITION_OR_SURROUND_MODE_RET", {
        [0x01] = { "SURROUND", handle_surround_mode },
        [0x01] = { "POSITION", handle_sound_position },
    } },
    [0x48] = { "SOUND_POSITION_OR_SURROUND_MODE_SET", {
        [0x01] = { "SURROUND", handle_surround_mode },
        [0x01] = { "POSITION", handle_sound_position },
    } },
    [0x49] = { "SOUND_POSITION_OR_SURROUND_MODE_NOTIFY", {
        [0x01] = { "SURROUND", handle_surround_mode },
        [0x01] = { "POSITION", handle_sound_position },
    } },

    -- Equalizer
    [0x56] = { "EQUALIZER_GET" },
    [0x57] = { "EQUALIZER_RET", handle_equalizer },
    [0x58] = { "EQUALIZER_SET", handle_equalizer },
    [0x59] = { "EQUALIZER_NOTIFY", handle_equalizer },

    -- Ambient Sound Control
    [0x66] = { "AMBIENT_SOUND_CONTROL_GET" },
    [0x67] = { "AMBIENT_SOUND_CONTROL_RET", handle_ambient_sound_control },
    [0x68] = { "AMBIENT_SOUND_CONTROL_SET", handle_ambient_sound_control },
    [0x69] = { "AMBIENT_SOUND_CONTROL_NOTIFY", handle_ambient_sound_control },

    -- Media
    [0xa2] = { "MEDIA_GET" },
    [0xa3] = { "MEDIA_RET", handle_media },
    [0xa4] = { "MEDIA_SET", handle_media },
    [0xa5] = { "MEDIA_NOTIFY", handle_media },

    -- Volume
    -- TODO sub / sub sub payload?
    [0xa6] = { "VOLUME_GET" },
    [0xa7] = { "VOLUME_RET", handle_volume },
    [0xa8] = { "VOLUME_SET", handle_volume },
    [0xa9] = { "VOLUME_NOTIFY", handle_volume },

    -- Json?
    [0xc4] = { "JSON_GET" },
    [0xc9] = { "JSON_RET" },

    -- Touch Sensor
    [0xd6] = { "TOUCH_SENSOR_GET", {
        [0xd2] = { "TOUCH_SENSOR" }
    } },
    [0xd7] = { "TOUCH_SENSOR_RET", {
        [0xd2] = { "TOUCH_SENSOR", handle_boolean_payload("Touch Sensor") }
    } },
    [0xd8] = { "TOUCH_SENSOR_SET", {
        [0xd2] = { "TOUCH_SENSOR", handle_boolean_payload("Touch Sensor") }
    } },
    [0xd9] = { "TOUCH_SENSOR_NOTIFY", {
        [0xd2] = { "TOUCH_SENSOR", handle_boolean_payload("Touch Sensor") }
    } },

    -- Audio Upsampling
    [0xe6] = { "AUDIO_UPSAMPLING_GET", {
        [0x02] = { "AUDIO_UPSAMPLING" }
    } },
    [0xe7] = { "AUDIO_UPSAMPLING_RET", {
        [0x02] = { "AUDIO_UPSAMPLING", handle_boolean_payload("Audio Upsampling") }
    } },
    [0xe8] = { "AUDIO_UPSAMPLING_SET", {
        [0x02] = { "AUDIO_UPSAMPLING", handle_boolean_payload("Audio Upsampling") }
    } },
    [0xe9] = { "AUDIO_UPSAMPLING_NOTIFY", {
        [0x02] = { "AUDIO_UPSAMPLING", handle_boolean_payload("Audio Upsampling") }
    } },

    -- Automatic Power Off / Button Mode
    [0xf6] = { "AUTOMATIC_POWER_OFF_OR_BUTTON_MODE_GET", {
        [0x04] = { "AUTOMATIC_POWER_OFF" },
        [0x03] = { "PAUSE_WHEN_TAKEN_OFF" },
        [0x06] = { "BUTTON_MODE" },
    } },
    [0xf7] = { "AUTOMATIC_POWER_OFF_OR_BUTTON_MODE_RET", {
        [0x04] = { "AUTOMATIC_POWER_OFF", handle_automatic_power_off },
        [0x03] = { "PAUSE_WHEN_TAKEN_OFF", handle_boolean_payload("Pause when taken off") },
        [0x06] = { "BUTTON_MODE", handle_button_mode },
    } },
    [0xf8] = { "AUTOMATIC_POWER_OFF_OR_BUTTON_MODE_SET", {
        [0x04] = { "AUTOMATIC_POWER_OFF", handle_automatic_power_off },
        [0x03] = { "PAUSE_WHEN_TAKEN_OFF", handle_boolean_payload("Pause when taken off") },
        [0x06] = { "BUTTON_MODE", handle_button_mode },
    } },
    [0xf9] = { "AUTOMATIC_POWER_OFF_OR_BUTTON_MODE_NOTIFY", {
        [0x04] = { "AUTOMATIC_POWER_OFF", handle_automatic_power_off },
        [0x03] = { "PAUSE_WHEN_TAKEN_OFF", handle_boolean_payload("Pause when taken off") },
        [0x06] = { "BUTTON_MODE", handle_button_mode },
    } },
}

payload_types_2 = {
    -- Voice Notifications
    -- TODO sub sub payload?
    [0x46] = { "VOICE_NOTIFICATIONS_GET", {
        [0x01] = { "VOICE_NOTIFICATIONS" },
    } },
    [0x47] = { "VOICE_NOTIFICATIONS_RET", {
        [0x01] = { "VOICE_NOTIFICATIONS", handle_boolean_payload("Voice Notifications") },
    } },
    [0x48] = { "VOICE_NOTIFICATIONS_SET", {
        [0x01] = { "VOICE_NOTIFICATIONS", handle_boolean_payload("Voice Notifications") },
    } },
    [0x49] = { "VOICE_NOTIFICATIONS_NOTIFY", {
        [0x01] = { "VOICE_NOTIFICATIONS", handle_boolean_payload("Voice Notifications") },
    } },
}

function parse_payload(payload_types)
    return function(payload, pinfo, subtree)
        local payload_type_code = payload(0,1):uint()

        if payload_types[payload_type_code] == nil then
            pinfo.cols.info = "Unknown Payload Type"

            -- Unknown payload type
            subtree:add(sony_proto.fields.message_payload_type, payload(0,1), "Unknown")
            --subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unknown Payload Type")
            return
        end

        local payload_type = "Unknown"
        payload_type = payload_types[payload_type_code][1]
        payload_handler = payload_types[payload_type_code][2]

        pinfo.cols.info = payload_type

        subtree:add(sony_proto.fields.message_payload_type, payload(0,1), payload_type)

        if (type(payload_handler) == "table") then
            -- There's a subtype
            local payload_subtype_code = payload(1,1):uint()

            if payload_handler[payload_subtype_code] == nil then
                -- Unknown payload subtype
                pinfo.cols.info:append(", unknown subtype")

                subtree:add(sony_proto.fields.message_payload_subtype, payload(1,1), "Unknown")
                --subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unknown Payload Subtype")
                return
            end

            payload_subtype = payload_handler[payload_subtype_code][1]
            payload_handler = payload_handler[payload_subtype_code][2]

            pinfo.cols.info:append(", " .. payload_subtype)

            subtree:add(sony_proto.fields.message_payload_subtype, payload(1,1), payload_subtype)
        end

        if (type(payload_handler) == "function") then
            payload_handler(payload, subtree)
        end
    end
end

function parse_ack(payload, pinfo, subtree)
    if payload:len() ~= 0 then
        subtree:add_expert_info(PI_DEBUG, PI_WARN, "Unexpected ACK payload")
    end

    pinfo.cols.info = "ACK"
end

message_types = {
    [0x01] = { "ACK", parse_ack },
    [0x0c] = { "COMMAND_1", parse_payload(payload_types_1) },
    [0x0e] = { "COMMAND_2", parse_payload(payload_types_2) },
}

function get_message_type(message_type_code)
    local message_type = "Unknown"

    if message_types[message_type_code] ~= nil then
        message_type = message_types[message_type_code][1]
    end

    return message_type
end

function sony_proto.dissector(buffer, pinfo, tree)
    pinfo.cols.protocol = sony_proto.name

    local subtree_data = tree:add(sony_proto, buffer, "Data")

    -- Check header
    if buffer(0, 1):uint() ~= 0x3e then
        subtree_data:add_expert_info(PI_DEBUG, PI_ERROR, "Unexpected packet header " .. buffer(0, 1))
        return
    end

    -- Check trailer
    if buffer(buffer:len() - 1, 1):uint() ~= 0x3c then
        subtree_data:add_expert_info(PI_DEBUG, PI_ERROR, "Unexpected packet trailer " .. buffer(buffer:len() - 1, 1))
        return
    end

    -- Check unescaped
    -- TODO implement unescaping
    for i = 0, buffer:len() - 1 do
        if buffer(i, 1):uint() == 0x3d then
            subtree_data:add_expert_info(PI_DEBUG, PI_ERROR, "Unescaping messages is not yet implemented")
            return
        end
    end

    -- Check checksum
    local chk = 0
    for i = 1, buffer:len() - 3 do
        chk = chk + buffer(i, 1):uint()
    end

    while chk > 255 do
        chk = chk - 256
    end

    if buffer(buffer:len() - 2, 1):uint() ~= chk then
        subtree_data:add_expert_info(PI_DEBUG, PI_ERROR, "Unexpected checksum " .. buffer(buffer:len() - 2, 1) .. " expected " .. chk)
        return
    end

    local payload_len = buffer(3, 4):uint()
    local payload = buffer(7, payload_len)

    subtree_data:add(sony_proto.fields.message_type, buffer(1, 1)):append_text(" (" .. get_message_type(buffer(1, 1):uint()) .. ")")
    subtree_data:add(sony_proto.fields.sequence_number, buffer(2, 1))
    subtree_data:add(sony_proto.fields.payload_length, buffer(3, 4))
    subtree_data:add(sony_proto.fields.message_checksum, buffer(buffer:len() - 2, 1))

    subtree_data:add(sony_proto.fields.message_payload, payload)
    local subtree_payload = tree:add(sony_proto, buffer, "Payload")
    message_types[buffer(1, 1):uint()][2](payload, pinfo, subtree_payload)
end

local btatt_handle = DissectorTable.get("btrfcomm.dlci")
-- WH-1000XM3
--btatt_handle:add(0x1e, sony_proto)
-- WF-SP800N / WF-1000XM4
--btatt_handle:add(0x12, sony_proto)
for i = 0, 255 do
    btatt_handle:add(i, sony_proto)
end
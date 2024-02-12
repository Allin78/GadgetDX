/*  Copyright (C) 2024 Aleksandr Ivanov

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

package nodomain.freeyourgadget.gadgetbridge.externalevents;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;

import androidx.annotation.Nullable;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.util.MediaManager;

public class MediaStateReceiver extends MediaController.Callback {
    private long lastPosition = 0;
    private long lastUpdateTime = 0;

    private MediaController mMediaController;

    public MediaStateReceiver(MediaController mediaController) {
        mMediaController = mediaController;
    }

    public boolean isPlaybackActive() {
        // https://developer.android.com/reference/android/media/session/PlaybackState#isActive()

        switch (mMediaController.getPlaybackState().getState()) {
            case PlaybackState.STATE_BUFFERING:
            case PlaybackState.STATE_CONNECTING:
            case PlaybackState.STATE_FAST_FORWARDING:
            case PlaybackState.STATE_PLAYING:
            case PlaybackState.STATE_REWINDING:
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                return true;
            default:
                return false;
        }
    }

    public void startReceiving() {
        setMetadata(mMediaController.getMetadata());
        setPlaybackState(mMediaController.getPlaybackState());
        mMediaController.registerCallback(this);
    }

    public void stopReceiving() {
        mMediaController.unregisterCallback(this);
    }

    @Override
    public void onMetadataChanged(@Nullable MediaMetadata metadata) {
        super.onMetadataChanged(metadata);
        setMetadata(metadata);
    }

    @Override
    public void onPlaybackStateChanged(@Nullable PlaybackState state) {
        super.onPlaybackStateChanged(state);
        setPlaybackState(state);
    }

    private void setMetadata(@Nullable MediaMetadata metadata) {
        final MusicSpec musicSpec = MediaManager.extractMusicSpec(metadata);

        if (musicSpec != null) {
            GBApplication.deviceService().onSetMusicInfo(musicSpec);
        }
    }

    private void setPlaybackState(@Nullable PlaybackState state) {
        if (state.getState() == PlaybackState.STATE_PLAYING && !doStateUpdate(state)) {
            return;
        }

        final MusicStateSpec stateSpec = MediaManager.extractMusicStateSpec(state);

        if (stateSpec != null) {
            GBApplication.deviceService().onSetMusicState(stateSpec);
        }
    }

    private boolean doStateUpdate(PlaybackState state)
    {
        // To prevent spamming device with state updates

        float speed = state.getPlaybackSpeed();

        long currentTime = System.currentTimeMillis();
        long currentPosition = state.getPosition();

        long timeDiff = (long) (speed * (currentTime - lastUpdateTime));
        long positionDiff = currentPosition - lastPosition;

        long epsilon = (long) Math.abs(speed * 50);

        if (Math.abs(timeDiff - positionDiff) > epsilon)
        {
            lastUpdateTime = currentTime;
            lastPosition = currentPosition;
            return true;
        }

        return false;
    }
}

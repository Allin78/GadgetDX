/*  Copyright (C) 2022 Cody Henthorne, José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nodomain.freeyourgadget.gadgetbridge.activities.NotificationManagementActivity;

/**
 * Some custom ROMs and some Samsung Android 11 devices have quirks around accessing the default ringtone. This attempts to deal
 * with them with progressively worse approaches.
 * <p>
 * Adapted from Signal-Android: https://github.com/signalapp/Signal-Android/blob/5.30.6/app/src/main/java/org/thoughtcrime/securesms/util/RingtoneUtil.java
 */
public final class RingtoneUtils {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationManagementActivity.class);

    private RingtoneUtils() {
    }

    public static @Nullable Ringtone getRingtone(@NonNull Context context, @NonNull Uri uri) {
        Ringtone tone;
        try {
            tone = RingtoneManager.getRingtone(context, uri);
        } catch (SecurityException e) {
            LOG.warn("Unable to get default ringtone due to permission", e);
            tone = RingtoneManager.getRingtone(context, RingtoneUtils.getActualDefaultRingtoneUri(context));
        }
        return tone;
    }

    public static @Nullable Uri getActualDefaultRingtoneUri(@NonNull Context context) {
        LOG.info("Attempting to get default ringtone directly via normal way");
        try {
            return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        } catch (SecurityException e) {
            LOG.warn("Failed to get ringtone with first fallback approach", e);
        }

        LOG.info("Attempting to get default ringtone directly via reflection");
        String uriString = getStringForUser(context.getContentResolver(), getUserId(context));
        Uri ringtoneUri = uriString != null ? Uri.parse(uriString) : null;

        if (ringtoneUri != null && getUserIdFromAuthority(ringtoneUri.getAuthority(), getUserId(context)) == getUserId(context)) {
            ringtoneUri = getUriWithoutUserId(ringtoneUri);
        }

        return ringtoneUri;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    private static @Nullable
    String getStringForUser(@NonNull ContentResolver resolver, int userHandle) {
        try {
            Method getStringForUser = Settings.System.class.getMethod("getStringForUser", ContentResolver.class, String.class, int.class);
            return (String) getStringForUser.invoke(Settings.System.class, resolver, Settings.System.RINGTONE, userHandle);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.warn("Unable to getStringForUser via reflection", e);
        }
        return null;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    private static int getUserId(@NonNull Context context) {
        try {
            Object userId = Context.class.getMethod("getUserId").invoke(context);
            if (userId instanceof Integer) {
                return (Integer) userId;
            } else {
                LOG.warn("getUserId did not return an integer");
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.warn("Unable to getUserId via reflection", e);
        }
        return 0;
    }

    private static @Nullable Uri getUriWithoutUserId(@Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        Uri.Builder builder = uri.buildUpon();
        builder.authority(getAuthorityWithoutUserId(uri.getAuthority()));
        return builder.build();
    }

    private static @Nullable String getAuthorityWithoutUserId(@Nullable String auth) {
        if (auth == null) {
            return null;
        }
        int end = auth.lastIndexOf('@');
        return auth.substring(end + 1);
    }

    private static int getUserIdFromAuthority(@Nullable String authority, int defaultUserId) {
        if (authority == null) {
            return defaultUserId;
        }

        int end = authority.lastIndexOf('@');
        if (end == -1) {
            return defaultUserId;
        }

        String userIdString = authority.substring(0, end);
        try {
            return Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            return defaultUserId;
        }
    }
}

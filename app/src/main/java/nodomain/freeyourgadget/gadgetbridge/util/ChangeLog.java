/*  Copyright (C) 2023 Arjan Schrijver

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

import android.content.Context;
import android.content.DialogInterface;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import nodomain.freeyourgadget.gadgetbridge.R;

public class ChangeLog extends de.cketti.library.changelog.ChangeLog {
    public ChangeLog(Context context, String css) {
        super(context, css);
    }

    public AlertDialog getMaterialLogDialog() {
        return getMaterialDialog(isFirstRunEver());
    }

    public AlertDialog getMaterialFullLogDialog() {
        return getMaterialDialog(true);
    }

    protected AlertDialog getMaterialDialog(boolean full) {
        WebView wv = new WebView(mContext);
        //wv.setBackgroundColor(0); // transparent
        wv.loadDataWithBaseURL(null, getLog(full), "text/html", "UTF-8", null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setTitle(
                        mContext.getResources().getString(
                                full ? R.string.changelog_full_title : R.string.changelog_title))
                .setView(wv)
                .setCancelable(false)
                // OK button
                .setPositiveButton(
                        mContext.getResources().getString(R.string.changelog_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // The user clicked "OK" so save the current version code as
                                // "last version code".
                                updateVersionInPreferences();
                            }
                        });

        if (!full) {
            // Show "More…" button if we're only displaying a partial change log.
            builder.setNegativeButton(R.string.changelog_show_full,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            getMaterialFullLogDialog().show();
                        }
                    });
        }

        return builder.create();
    }
}

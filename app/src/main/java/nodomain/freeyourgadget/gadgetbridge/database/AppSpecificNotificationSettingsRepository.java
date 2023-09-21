/*
 *   Copyright (C) 2023 akasaka / Genjitsu Labs
 *
 *     This file is part of Gadgetbridge.
 *
 *     Gadgetbridge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gadgetbridge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nodomain.freeyourgadget.gadgetbridge.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.entities.AppSpecificNotificationSettingDao;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.AppSpecificNotificationSetting;

// TODO: Distinguish by device ID as well to allow for different settings per device.
// I didn't know how to get a device ID from a settings activity, so I left it as is.
public class AppSpecificNotificationSettingsRepository {
    private final DaoSession session;

    public AppSpecificNotificationSettingsRepository(@NonNull DaoSession session) {
        this.session = session;
    }

    @Nullable
    public AppSpecificNotificationSetting getSettingsForAppId(String appId) {
        QueryBuilder<AppSpecificNotificationSetting> qb = session.getAppSpecificNotificationSettingDao().queryBuilder();
        return qb
                .where(AppSpecificNotificationSettingDao.Properties.PackageId.eq(appId))
                .build().unique();
    }

    private void deleteForAppId(@NonNull String appId) {
        QueryBuilder<AppSpecificNotificationSetting> qb = session.getAppSpecificNotificationSettingDao().queryBuilder();
        qb.where(AppSpecificNotificationSettingDao.Properties.PackageId.eq(appId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void setSettingsForAppId(@NonNull String appId, @Nullable AppSpecificNotificationSetting settings) {
        if (settings == null) {
            deleteForAppId(appId);
        } else {
            settings.setPackageId(appId);
            session.getAppSpecificNotificationSettingDao().insertOrReplace(settings);
        }
    }
}

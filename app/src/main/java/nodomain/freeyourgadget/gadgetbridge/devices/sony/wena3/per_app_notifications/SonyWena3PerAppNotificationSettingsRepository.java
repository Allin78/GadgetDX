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

package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.per_app_notifications;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Wena3PerAppNotificationSetting;
import nodomain.freeyourgadget.gadgetbridge.entities.Wena3PerAppNotificationSettingDao;

public class SonyWena3PerAppNotificationSettingsRepository {
    private final DaoSession session;

    public SonyWena3PerAppNotificationSettingsRepository(@NonNull DaoSession session) {
        this.session = session;
    }

    @Nullable
    public Wena3PerAppNotificationSetting getSettingsForAppId(String appId) {
        QueryBuilder<Wena3PerAppNotificationSetting> qb = session.getWena3PerAppNotificationSettingDao().queryBuilder();
        return qb
                .where(Wena3PerAppNotificationSettingDao.Properties.PackageId.eq(appId))
                .build().unique();
    }

    private void deleteForAppId(@NonNull String appId) {
        QueryBuilder<Wena3PerAppNotificationSetting> qb = session.getWena3PerAppNotificationSettingDao().queryBuilder();
        qb.where(Wena3PerAppNotificationSettingDao.Properties.PackageId.eq(appId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void setSettingsForAppId(@NonNull String appId, @Nullable Wena3PerAppNotificationSetting settings) {
        if (settings == null) {
            deleteForAppId(appId);
        } else {
            settings.setPackageId(appId);
            session.getWena3PerAppNotificationSettingDao().insertOrReplace(settings);
        }
    }
}

/*  Copyright (C) 2017-2020 abettenburg, AndrewBedscastle, Carsten Pfeiffer,
    Daniele Gobbetti

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
package nodomain.freeyourgadget.gadgetbridge.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.NotificationFilterActivity;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

import static nodomain.freeyourgadget.gadgetbridge.GBApplication.appIsNotifBlacklisted;

//import static nodomain.freeyourgadget.gadgetbridge.GBApplication.packageNameToPebbleMsgSender;

public class AppFiltersAdapter extends RecyclerView.Adapter<AppFiltersAdapter.AppBLViewHolder> implements Filterable {

    public static final String STRING_EXTRA_PACKAGE_NAME = "packageName";

    private List<ApplicationInfo> applicationInfoList;
    private final int mLayoutId;
    private final Context mContext;
    private final PackageManager mPm;
    private final IdentityHashMap<ApplicationInfo, String> mNameMap;

    private ApplicationFilter applicationFilter;

    public AppFiltersAdapter(int layoutId, Context context) {
        mLayoutId = layoutId;
        mContext = context;
        mPm = context.getPackageManager();

        applicationInfoList = new ArrayList<>();
        List<ApplicationInfo> fullApplicationInfoList = GBApplication.getInstalledApplications();
        for (ApplicationInfo ai : fullApplicationInfoList) {
            if (!appIsNotifBlacklisted(ai.packageName))
                applicationInfoList.add(ai);
        }

        mNameMap = new IdentityHashMap<>(applicationInfoList.size());
        for (ApplicationInfo ai : applicationInfoList) {
            String name = GBApplication.getApplicationLabel(ai);
            if (name == null) {
                name = ai.packageName;
            }
            mNameMap.put(ai, name);
        }

        Collections.sort(applicationInfoList, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo ai1, ApplicationInfo ai2) {
                final String s1 = mNameMap.get(ai1);
                final String s2 = mNameMap.get(ai2);
                return s1.compareTo(s2);
            }
        });

    }

    @Override
    public AppFiltersAdapter.AppBLViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new AppBLViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AppFiltersAdapter.AppBLViewHolder holder, int position) {
        final ApplicationInfo appInfo = applicationInfoList.get(position);

        holder.deviceAppVersionAuthorLabel.setText(appInfo.packageName);
        holder.deviceAppNameLabel.setText(mNameMap.get(appInfo));
        holder.deviceImageView.setImageDrawable(appInfo.loadIcon(mPm));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentStartNotificationFilterActivity = new Intent(mContext, NotificationFilterActivity.class);
                intentStartNotificationFilterActivity.putExtra(STRING_EXTRA_PACKAGE_NAME, appInfo.packageName);
                mContext.startActivity(intentStartNotificationFilterActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return applicationInfoList.size();
    }

    @Override
    public Filter getFilter() {
        if (applicationFilter == null)
            applicationFilter = new ApplicationFilter(this, applicationInfoList);
        return applicationFilter;
    }

    class AppBLViewHolder extends RecyclerView.ViewHolder {
        final ImageView deviceImageView;
        final TextView deviceAppVersionAuthorLabel;
        final TextView deviceAppNameLabel;

        AppBLViewHolder(View itemView) {
            super(itemView);

            deviceImageView = itemView.findViewById(R.id.item_image);
            deviceAppVersionAuthorLabel = itemView.findViewById(R.id.item_details);
            deviceAppNameLabel = itemView.findViewById(R.id.item_name);
        }

    }

    private class ApplicationFilter extends Filter {

        private final AppFiltersAdapter adapter;
        private final List<ApplicationInfo> originalList;
        private final List<ApplicationInfo> filteredList;

        private ApplicationFilter(AppFiltersAdapter adapter, List<ApplicationInfo> originalList) {
            super();
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
            this.adapter = adapter;
        }

        @Override
        protected Filter.FilterResults performFiltering(CharSequence filter) {
            filteredList.clear();
            final Filter.FilterResults results = new Filter.FilterResults();

            if (filter == null || filter.length() == 0)
            {
                filteredList.addAll(originalList);
            }
            else
            {
                final String filterPattern = filter.toString().toLowerCase().trim();

                for (ApplicationInfo ai : originalList) {
                    String label = GBApplication.getApplicationLabel(ai);
                    if (label.toLowerCase().contains(filterPattern) || ai.packageName.contains(filterPattern))
                        filteredList.add(ai);
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
            adapter.applicationInfoList.clear();
            adapter.applicationInfoList.addAll((List<ApplicationInfo>) filterResults.values);
            adapter.notifyDataSetChanged();
        }
    }
}

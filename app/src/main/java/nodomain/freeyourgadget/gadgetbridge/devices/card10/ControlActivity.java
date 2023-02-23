/*  Copyright (C) 2023 Marc Nause

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
package nodomain.freeyourgadget.gadgetbridge.devices.card10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.card10.Card10DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.card10.Card10PersonalState;

public class ControlActivity extends AbstractGBActivity {

    private static final Logger LOG = LoggerFactory.getLogger(ControlActivity.class);

    LocalBroadcastManager localBroadcastManager;

    private GBDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card10_control);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            device = bundle.getParcelable(GBDevice.EXTRA_DEVICE);
        } else {
            throw new IllegalArgumentException("Must provide a device when invoking this activity");
        }

        Spinner personalStateSpinner = findViewById(R.id.personal_state_select);
        SpinnerAdapter personalStateAdapter =
                new ArrayAdapter<>(this, R.layout.simple_spinner_item_themed, Card10PersonalState.values());
        personalStateSpinner.setAdapter(personalStateAdapter);
        // TODO: set item to current state

        personalStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LOG.info("Selected: " + personalStateAdapter.getItem(position));

                Intent intent = new Intent(Card10DeviceSupport.COMMAND_CONFIGURE);
                intent.putExtra(Card10DeviceSupport.PERSONAL_STATE, position);
                sendLocalBroadcast(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(ControlActivity.this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(GBDevice.ACTION_DEVICE_CHANGED);
        localBroadcastManager.registerReceiver(commandReceiver, filter);
    }

    BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.debug("device receiver received " + intent.getAction());
            if (intent.getAction().equals(GBDevice.ACTION_DEVICE_CHANGED)) {
                GBDevice newDevice = intent.getParcelableExtra(GBDevice.EXTRA_DEVICE);
                if (!newDevice.equals(device)) {
                    device = newDevice;
                    // TODO: Do we need this?
                }
            }
        }
    };

    private void sendLocalBroadcast(Intent intent) {
        localBroadcastManager.sendBroadcast(intent);
    }

    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commandReceiver);
    }

}


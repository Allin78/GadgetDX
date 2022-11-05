package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity.WithingsActivityType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutScreenList;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public class WorkoutScreenListHandler extends AbstractResponseHandler {

    public WorkoutScreenListHandler(WithingsSteelHRDeviceSupport support) {
        super(support);
    }

    @Override
    public void handleResponse(Message response) {
        List<WithingsStructure> data = response.getDataStructures();
        if (data != null && !data.isEmpty()) {
            WorkoutScreenList screenList = (WorkoutScreenList) data.get(0);
            saveScreenList(screenList);
        }
    }

    private void saveScreenList(WorkoutScreenList screenList) {
        int[] workoutIds = screenList.getWorkoutIds();
        List<String> prefValues = new ArrayList<>();
        for (int i = 0; i < workoutIds.length; i++) {
            int currentId = workoutIds[i];
            if (currentId > 0) {
                WithingsActivityType type = WithingsActivityType.fromCode(currentId);
                prefValues.add(type.name().toLowerCase(Locale.ROOT));
            }
         }

        String workoutActivityTypes = String.join(",", prefValues);
        GBDevice device = support.getDevice();
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(device.getAddress());
        prefs.edit().putString("workout_activity_types_sortable", workoutActivityTypes).apply();
    }
}

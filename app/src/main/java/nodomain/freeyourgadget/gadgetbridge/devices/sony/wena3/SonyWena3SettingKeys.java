package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3;

public class SonyWena3SettingKeys {
    // ------ Booleans
    public static final String RICH_DESIGN_MODE = "pref_wena3_rich_design_mode";
    public static final String LARGE_FONT_SIZE = "pref_wena3_large_font_size";
    public static final String WEATHER_IN_STATUSBAR = "pref_wena3_weather_in_statusbar";
    public static final String SMART_VIBRATION = "pref_wena3_vibration_smart";

    public static final String AUTO_POWER_SCHEDULE_KIND = "pref_wena3_power_schedule_kind";
    public static final String AUTO_POWER_SCHEDULE_START_HHMM = "pref_wena3_power_schedule_start";
    public static final String AUTO_POWER_SCHEDULE_END_HHMM = "pref_wena3_power_schedule_end";

    // ------ Ints
    public static final String SMART_WAKEUP_MARGIN_MINUTES = "pref_wena3_smart_wakeup_margin";
    public static final String VIBRATION_STRENGTH = "pref_wena3_vibration_strength";
    public static final String LEFT_HOME_ICON = "pref_wena3_home_icon_left";
    public static final String CENTER_HOME_ICON = "pref_wena3_home_icon_center";
    public static final String RIGHT_HOME_ICON = "pref_wena3_home_icon_right";
    public static final String DAY_START_HOUR = "pref_wena3_day_start_hour";
    public static final String BUTTON_LONG_PRESS_ACTION = "pref_wena3_button_long_action";
    public static final String BUTTON_DOUBLE_PRESS_ACTION = "pref_wena3_button_double_action";
    public static final String MENU_ICON_KEY_PREFIX = "pref_wena3_menu_icon_";
    public static final int MAX_MENU_ICONS = 9;
    public static final String menuIconKeyFor(int number) {
        assert number < MAX_MENU_ICONS;
        return MENU_ICON_KEY_PREFIX + number;
    }

    public static final String STATUS_PAGE_KEY_PREFIX = "pref_wena3_status_page_";
    public static final int MAX_STATUS_PAGES = 7;
    public static final String statusPageKeyFor(int number) {
        assert number < MAX_STATUS_PAGES;
        return STATUS_PAGE_KEY_PREFIX + number;
    }
}

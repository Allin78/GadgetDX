package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3;

public class SonyWena3SettingKeys {
    // ------ Booleans
    public static final String RICH_DESIGN_MODE = "pref_wena3_rich_design_mode";
    public static final String LARGE_FONT_SIZE = "pref_wena3_large_font_size";
    public static final String WEATHER_IN_STATUSBAR = "pref_wena3_weather_in_statusbar";
    public static final String SMART_VIBRATION = "pref_wena3_vibration_smart";

    // ------ Ints
    public static final String SMART_WAKEUP_MARGIN_MINUTES = "pref_wena3_smart_wakeup_margin";
    public static final String VIBRATION_STRENGTH = "pref_wena3_vibration_strength";
    public static final String LEFT_HOME_ICON = "pref_wena3_home_icon_left";
    public static final String CENTER_HOME_ICON = "pref_wena3_home_icon_center";
    public static final String RIGHT_HOME_ICON = "pref_wena3_home_icon_right";
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

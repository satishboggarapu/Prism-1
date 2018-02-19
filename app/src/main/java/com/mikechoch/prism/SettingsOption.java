package com.mikechoch.prism;

import com.mikechoch.prism.constants.Default;

/**
 * Created by mikechoch on 2/16/18.
 */

public enum SettingsOption {

    APP("App Settings", R.drawable.ic_settings_white_36dp, Default.SETTINGS_OPTION_APP),
    NOTIFICATION("Notification Settings",  R.drawable.ic_bell_white_36dp, Default.SETTINGS_OPTION_NOTIFICATION),
    ACCOUNT("Account Settings",  R.drawable.ic_account_settings_variant_white_36dp, Default.SETTINGS_OPTION_ACCOUNT),
    HELP("Help",  R.drawable.ic_help_white_36dp, Default.SETTINGS_OPTION_HELP),
    ABOUT("About",  R.drawable.ic_information_outline_white_36dp, Default.SETTINGS_OPTION_ABOUT),
    LOGOUT("Logout",  R.drawable.ic_logout_white_36dp, Default.SETTINGS_OPTION_LOGOUT);

    private final String optionTitle;
    private final int optionIcon;
    private final int optionId;

    SettingsOption(String title, int icon, int id) {
        this.optionTitle = title;
        this.optionIcon = icon;
        this.optionId = id;
    }

    public String getOptionTitle() {
        return optionTitle;
    }

    public int getOptionIcon() {
        return optionIcon;
    }

    public int getOptionId() {
        return optionId;
    }
}

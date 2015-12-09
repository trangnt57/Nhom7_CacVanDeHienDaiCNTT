package g7.bluesky.launcher3.setting;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import g7.bluesky.launcher3.Launcher;
import g7.bluesky.launcher3.R;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);

            bindPreferenceSummaryToValue(findPreference(SettingConstants.LAYOUT_PREF_KEY));
            bindPreferenceSummaryToValue(findPreference(SettingConstants.THEME_PREF_KEY));
            bindPreferenceSummaryToValue(findPreference(SettingConstants.TEXT_COLOR_PREF_KEY));
            bindPreferenceSummaryToValue(findPreference(SettingConstants.THEME_OPACITY_PREF_KEY));

            // List to select icon pack
            final ListPreference lp = (ListPreference) findPreference(SettingConstants.ICON_THEME_PREF_KEY);

            // Value to save to preference
            final List<String> iconPacksPackageName = new ArrayList<>();
            // Value to display lable
            final List<String> iconPacksName = new ArrayList<>();
            // Value to check if icon is installed
            final List<Boolean> iconPacksIsInstalled = new ArrayList<>();

            // Default icon
            iconPacksName.add("Default");
            iconPacksPackageName.add(Launcher.class.getPackage().getName());
            iconPacksIsInstalled.add(true);

            // Get icon pack used by Apex launcher
            PackageManager pm = getActivity().getPackageManager();
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory("com.anddoes.launcher.THEME");
            List<ResolveInfo> installPacks = pm.queryIntentActivities(mainIntent, 0);
            if (installPacks != null && installPacks.size() > 0) {
                for (ResolveInfo resolveInfo: installPacks) {
                    iconPacksPackageName.add(resolveInfo.activityInfo.packageName);
                    iconPacksName.add(resolveInfo.loadLabel(pm).toString());
                    iconPacksIsInstalled.add(true);
                }
            }

            // Add some default icon if they are not installed
            String[] someIconsName = {"nexbit.icons.moonshine===Moonshine", "com.numix.icons_circle===Numix Circle", "com.numix.icons_fold===Numix Fold"};
            for (int i = 0; i < someIconsName.length; ++i) {
                String tmp[] = someIconsName[i].split("===");
                if (! iconPacksPackageName.contains(tmp[0])) {
                    iconPacksPackageName.add(tmp[0]);
                    iconPacksName.add("[Install] " + tmp[1]);
                    iconPacksIsInstalled.add(false);
                }
            }

            // Set default value is system icons
            if (lp.getValue() == null) {
                lp.setValue(Launcher.class.getPackage().getName());
            }
            lp.setEntries(iconPacksName.toArray(new String[iconPacksPackageName.size()]));
            lp.setEntryValues(iconPacksPackageName.toArray(new String[iconPacksPackageName.size()]));
            String selectedIconTheme = getPreferenceManager().getSharedPreferences().getString(SettingConstants.ICON_THEME_PREF_KEY, Launcher.class.getPackage().getName());
            int idxOfSelectedIconTheme = iconPacksPackageName.indexOf(selectedIconTheme);
            if (idxOfSelectedIconTheme != -1 && idxOfSelectedIconTheme < iconPacksName.size()) {
                lp.setSummary(iconPacksName.get(idxOfSelectedIconTheme));
            }

            lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String iconTheme = newValue.toString();
                    int idxOfIconTheme = iconPacksPackageName.indexOf(iconTheme);

                    if (idxOfIconTheme != -1 && idxOfIconTheme < iconPacksIsInstalled.size()) {
                        // If icon pack is installed then set to preference else go to Play store
                        if (iconPacksIsInstalled.get(idxOfIconTheme)) {
                            lp.setSummary(iconPacksName.get(idxOfIconTheme));
                            getActivity().finish();
                            return true;
                        } else {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + iconTheme)));
                            getActivity().finish();
                        }
                    }
                    return false;
                }
            });
        }
    }
}

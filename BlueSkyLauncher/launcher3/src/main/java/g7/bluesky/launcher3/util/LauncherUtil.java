package g7.bluesky.launcher3.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.listview.AppComparator;
import g7.bluesky.launcher3.setting.SettingConstants;

/**
 * Created by tuanpt on 11/27/2015.
 */
public class LauncherUtil {
    public static void sortAppsByLaunchTimes(SharedPreferences sharedPreferences, List<AppInfo> listApps) {
        if (listApps != null) {
            for (AppInfo appInfo : listApps) {
                String prefKey = appInfo.getComponentName().getClassName();
                int count = sharedPreferences.getInt(prefKey, 0);
                appInfo.launchTimes = count;
            }
            Collections.sort(listApps, Collections.reverseOrder(new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo lhs, AppInfo rhs) {
                    return lhs.launchTimes.compareTo(rhs.launchTimes);
                }
            }));
        }
    }

    public static void increaseAppLaunchTimes(SharedPreferences sharedPreferences, String appClassName) {
        // Count app launch times
        int count = sharedPreferences.getInt(appClassName, 0) + 1;
        sharedPreferences.edit().putInt(appClassName, count).commit();
    }

    public static void sortListApps(SharedPreferences sharedPreferences, List<AppInfo> listApps, int sortOption) {
        if (listApps != null) {
            if (sortOption == SettingConstants.SORT_A_Z) {
                // Sort by name A-Z
                Collections.sort(listApps, new AppComparator());
            } else if (sortOption == SettingConstants.SORT_Z_A) {
                Collections.sort(listApps, Collections.reverseOrder(new AppComparator()));
            } else {
                Collections.sort(listApps, new AppComparator());
                LauncherUtil.sortAppsByLaunchTimes(sharedPreferences, listApps);
            }
        }
    }
}

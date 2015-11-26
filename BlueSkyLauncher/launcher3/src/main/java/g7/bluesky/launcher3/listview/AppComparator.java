package g7.bluesky.launcher3.listview;

import java.util.Comparator;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.util.StringUtil;

/**
 * Created by tuanpt on 10/14/2015.
 */
public class AppComparator implements Comparator<AppInfo> {
    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        String str1 = StringUtil.convertVNString(app1.getTitle().toString().trim());
        String str2 = StringUtil.convertVNString(app2.getTitle().toString().trim());
        int result = str1.compareTo(str2);

        // If names are equals then compare package name
        if (result == 0) {
            result = app1.getComponentName().compareTo(app2.getComponentName());
        }
        return result;
    }
}

package g7.bluesky.launcher3.listview;

import java.util.Comparator;

import g7.bluesky.launcher3.AppInfo;

/**
 * Created by fsi on 10/14/2015.
 */
public class AppComparator implements Comparator<AppInfo> {
    @Override
    public int compare(AppInfo app1, AppInfo app2) {
        return app1.getTitle().toString().compareTo(app2.getTitle().toString());
    }
}

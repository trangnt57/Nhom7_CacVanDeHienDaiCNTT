package g7.bluesky.launcher3.ExTraMenu;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppInfo extends g7.bluesky.launcher3.AppInfo{
	private Drawable icon;
	private String name;
	private String packageName;
	private String label;


	public void setInfo(ResolveInfo info, PackageManager pm){
		icon=info.loadIcon(pm);
		packageName=info.activityInfo.packageName;
		name=info.activityInfo.name;
		label=info.loadLabel(pm).toString();

		setComponentName(new ComponentName(packageName, info.getClass().toString()));
		setTitle(label);
		setIconDrawable(icon);
	}
	public String getName(){return name;}
}

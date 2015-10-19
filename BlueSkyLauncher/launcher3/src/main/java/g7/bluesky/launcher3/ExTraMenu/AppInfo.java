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

		setComponentName(new ComponentName(packageName, name));
		setTitle(label);
		setIconDrawable(icon);
	}

	public AppInfo(){
		super();
	}
	public AppInfo(Drawable ic, String pkn, String n, String lb){
		super();
		icon=ic;
		packageName=pkn;
		name=n;
		label=lb;

		setComponentName(new ComponentName(packageName, name));
		setTitle(label);
		setIconDrawable(icon);
	}
	public String getName(){return name;}

	public boolean equal(AppInfo app){
		if(packageName.compareTo(app.getComponentName().getPackageName())==0) return true;
		return false;
	}
	public AppInfo clone(){
		return new AppInfo(icon, packageName, name, label);
	}
}

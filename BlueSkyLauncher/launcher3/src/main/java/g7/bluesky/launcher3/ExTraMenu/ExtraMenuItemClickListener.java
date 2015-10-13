package g7.bluesky.launcher3.ExTraMenu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

public class ExtraMenuItemClickListener implements OnItemClickListener{

	Context mContext;
	ArrayList<AppInfo> mPacs;
	PackageManager pm;
	
	public ExtraMenuItemClickListener(Context context, ArrayList<AppInfo> pacs) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mPacs = pacs;
		pm = mContext.getPackageManager();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent launchIntent = new Intent(Intent.ACTION_MAIN);
		launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName cp = new ComponentName(mPacs.get(position).packageName, mPacs.get(position).name);
		launchIntent.setComponent(cp);
		mContext.startActivity(launchIntent);
	}

}

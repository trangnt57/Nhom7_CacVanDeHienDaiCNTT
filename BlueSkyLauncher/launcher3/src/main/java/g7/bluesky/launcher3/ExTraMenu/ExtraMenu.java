package g7.bluesky.launcher3.ExTraMenu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import g7.bluesky.launcher3.DynamicGrid;
import g7.bluesky.launcher3.ExTraMenu.dynamicgrid.DynamicGridView;
import g7.bluesky.launcher3.Launcher;
import g7.bluesky.launcher3.R;

public class ExtraMenu extends LinearLayout{

	private final String FILE_NAME = "ExTraMenu";
	private final String SAVE_KEY = "App";

	private final String PACKAGE = "package";
	private final String NAME = "name";

	final String LOG_TAG = "LongDT";

	private final String BTN_EDIT_SUA = "Sửa";
	private final String BTN_EDIT_XONG = "Xong";
	final long TIME_CLOSE = 5000;
	private long time_start=0;

	public ExtraMenu(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		mPacs = new ArrayList<AppInfo>();

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.extra_menu, this, true);

		mExtraMenu = (SlidingDrawer) this.findViewById(R.id.extra_menu);
		im_hide_show = (ImageView) mExtraMenu.findViewById(R.id.handle);
		gr_content = (DynamicGridView) mExtraMenu.findViewById(R.id.gv_ls_app);
		//ll_content = (LinearLayout) mExtraMenu.findViewById(R.id.gv_ls_app);
		btn_etra_edit = (Button) mExtraMenu.findViewById(R.id.btn_extra_edit);
		llExtraMenu = (LinearLayout) this.findViewById(R.id.ll_outside_extramenu);


		//load applist from filnOnUiThread(new Runnable() { e sharedferenced
		//readFromFile();
		setApplistDefault();
		exmiAdapter = new ExtraMenuItemAdapter(mContext, mPacs, 1);
		gr_content.setAdapter(exmiAdapter);
		gr_content.setOnItemClickListener(new ExtraMenuItemClickListener(mContext, mPacs));
		gr_content.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				time_start = System.currentTimeMillis();
			}
		});

		//change arrow icon when open menu
		mExtraMenu.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				im_hide_show.setBackgroundResource(R.drawable.arrow_hide);
				autoclose();
			}
		});
		mExtraMenu.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				im_hide_show.setBackgroundResource(R.drawable.arrow_show);
				setReady();
			}
		});
		//disable all delete icon
		gr_content.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				gr_content.startEditMode();
				btn_etra_edit.setText(BTN_EDIT_XONG);
				is_autoclose = false;

				//show the delete icon
				GridView gr_applist = (GridView) view.getParent();
				for (int i = 0; i < gr_applist.getChildCount(); i++) {
					View v = gr_applist.getChildAt(i);
					ImageView img_delete = (ImageView) v.findViewById(R.id.img_extramenu_app_delete);
					img_delete.setImageResource(R.drawable.icon_exit_mini);
					img_delete.setEnabled(true);
				}
				return true;
			}
		});

		// bắt sự kiện cho nút sửa
		btn_etra_edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (btn_etra_edit.getText().toString().compareTo(BTN_EDIT_XONG) == 0) {
					btn_etra_edit.setText(BTN_EDIT_SUA);
					update_applist(exmiAdapter.getItems());
					saveToFile();
					gr_content.stopEditMode();
					is_autoclose = true;

					GridView gr_applist = gr_content;
					for (int i = 0; i < gr_applist.getChildCount(); i++) {
						View v = gr_applist.getChildAt(i);
						ImageView img_delete = (ImageView) v.findViewById(R.id.img_extramenu_app_delete);
						img_delete.setImageResource(R.drawable.blank);
						img_delete.setEnabled(false);
					}
				}
			}
		});
		setReady();

	}

	private void setReady(){
		btn_etra_edit.setText(BTN_EDIT_SUA);
		update_applist(exmiAdapter.getItems());
		saveToFile();
		gr_content.stopEditMode();

		GridView gr_applist = gr_content;
		for(int i=0; i< gr_applist.getChildCount(); i++){
			View v = gr_applist.getChildAt(i);
			ImageView img_delete = (ImageView) v.findViewById(R.id.img_extramenu_app_delete);
			img_delete.setImageResource(R.drawable.blank);
			img_delete.setEnabled(false);
		}
	}
	private void autoclose(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				is_autoclose = true;
				time_start = System.currentTimeMillis();
				while (mExtraMenu.isOpened()) {
					if(!is_autoclose)
						time_start = System.currentTimeMillis();
					else
						if (System.currentTimeMillis() - time_start >= TIME_CLOSE) {
							Log.i(LOG_TAG, "extramenu close");
							((Launcher) mContext).runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mExtraMenu.close();
									is_autoclose = false;
								}
							});
						}
				}
			}
		}).start();
	}
	private void setApplistDefault(){
		PackageManager pm = mContext.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> pacsList = pm.queryIntentActivities(mainIntent, 0);
		mPacs.clear();

		for(int j=0; j<10; j++){
			AppInfo p = new AppInfo();
			p.icon=pacsList.get(j).loadIcon(pm);
			p.packageName=pacsList.get(j).activityInfo.packageName;
			p.name=pacsList.get(j).activityInfo.name;
			p.label=pacsList.get(j).loadLabel(pm).toString();
			mPacs.add(p);
		}
	}
	private void saveToFile(){
		JSONArray js_arr_app = new JSONArray();
		for(int i=0; i<mPacs.size(); i++){
			JSONObject tmp = new JSONObject();
			try {
				tmp.put(PACKAGE, mPacs.get(i).packageName);
				tmp.put(NAME, mPacs.get(i).name);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			js_arr_app.put(tmp);
		}
		//Log.i("LongDT", js_arr_app.toString());

		SharedPreferences pre = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = pre.edit();
		edit.clear();
		edit.putString(SAVE_KEY, js_arr_app.toString());
		edit.commit();
	}

	private void update_applist(List<Object> items){
		mPacs.clear();
		for(int i=0; i<items.size(); i++){

			mPacs.add(i, ((AppInfo)(items.get(i))));
		}
	}


	private void readFromFile(){
		//Log.i(LOG_TAG, "Start read from file");
		SharedPreferences pre = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		String str_app = pre.getString(SAVE_KEY, null);

		//Log.i(LOG_TAG, str_app);
		if(str_app == null) return;
		try {
			JSONArray js_arr_app = new JSONArray(str_app);

			PackageManager pm = mContext.getPackageManager();
			Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> pacsList = pm.queryIntentActivities(mainIntent, 0);


			for(int i=0; i < js_arr_app.length(); i++){
				JSONObject tmp = js_arr_app.getJSONObject(i);
				for(int j= 0; j < pacsList.size(); j++){
					if(tmp.getString(PACKAGE).compareTo(pacsList.get(j).activityInfo.packageName) == 0
							|| tmp.getString(NAME).compareTo(pacsList.get(j).activityInfo.name) == 0){
						AppInfo p = new AppInfo();
						p.icon=pacsList.get(j).loadIcon(pm);
						p.packageName=pacsList.get(j).activityInfo.packageName;
						p.name=pacsList.get(j).activityInfo.name;
						p.label=pacsList.get(j).loadLabel(pm).toString();
						mPacs.add(p);
						//ll_content.addView(new AppLauncher(mContext, p, (AttributeSet)null));
						break;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void hide(){
		setVisibility(LinearLayout.GONE);
	}
	public void unhide(){
		setVisibility(LinearLayout.VISIBLE);
	}


	Context mContext;
	SlidingDrawer mExtraMenu;
	ImageView im_hide_show;
	DynamicGridView gr_content;
	//LinearLayout ll_content;
	LinearLayout llExtraMenu;
	Button btn_etra_edit;

	
	ArrayList<AppInfo> mPacs;
	final ExtraMenuItemAdapter exmiAdapter;
	boolean is_autoclose;
}

package g7.bluesky.launcher3.ExTraMenu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
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
		gr_add_app = (GridView) this.findViewById(R.id.extramenu_gr_add_app);
		btn_etra_edit = (Button) mExtraMenu.findViewById(R.id.btn_extra_edit);
		llExtraMenu = (LinearLayout) this.findViewById(R.id.ll_extramenu);
		llAppAdd = (LinearLayout) this.findViewById(R.id.ll_extramenu_add_app);
		rl_background = (RelativeLayout) this.findViewById(R.id.rl_background);


		//load applist from a xml file or 10 top app if this is the first time
		File f = new File("/data/data/g7.bluesky.launcher3/shared_prefs/"+FILE_NAME+".xml");
		if(f.exists())
			readFromFile();
		else
			mPacs = getApplistFromSystem(mContext.getPackageManager(), 10);

		exmiAdapter = new ExtraMenuItemAdapter(mContext, mPacs, 1);
		gr_content.setAdapter(exmiAdapter);
		gr_content.setOnItemClickListener(new ExtraMenuItemClickListener(mContext, mPacs));

		//delay autoclose
		gr_content.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				time_start = System.currentTimeMillis();
				return false;
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
				editExtraMenu();
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

					//dissappear add app
					gr_add_app.setVisibility(View.GONE);
					rl_background.setBackgroundResource(R.drawable.blank);
				}else{
					editExtraMenu();

					//show addview
					gr_add_app.setVisibility(View.VISIBLE);
					rl_background.setBackgroundColor(getResources().getColor(android.R.color.white));
				}
			}
		});

		//set appadd gridview
		setAppAddGridview();
		//set extramenu on ready state
		setReady();

	}

	//set extramenu ready to use
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
		//gr_content.setSelection(0);

		//dissappear add app
		gr_add_app.setVisibility(View.GONE);
		rl_background.setBackgroundResource(R.drawable.blank);
	}

	//auto close extramenu
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

	//save extramenu applist to file
	private void saveToFile(){
		JSONArray js_arr_app = new JSONArray();
		for(int i=0; i<mPacs.size(); i++){
			JSONObject tmp = new JSONObject();
			try {
				tmp.put(PACKAGE, mPacs.get(i).getComponentName().getPackageName());
				tmp.put(NAME, mPacs.get(i).getName());
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

	//read applist for extramenu from file
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
						p.setInfo(pacsList.get(j), pm);
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

	//setup f add gridview
	private void setAppAddGridview(){
		final ArrayList<AppInfo> applist = getApplistFromSystem(mContext.getPackageManager(),-1);
		gr_add_app.setAdapter(new AppAddItemAdapter(mContext, applist));

		//add view to extra menu
		gr_add_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i("LongDT", "onitem add app click");
				boolean isOnextramenu = false;
				for(int i = 0; i < mPacs.size(); i++){
					if(applist.get(position).equal(mPacs.get(i))) {
						isOnextramenu = true;
						break;
					}
				}
				if(!isOnextramenu){
					Log.i("LongDt", "add app success");
					exmiAdapter.add(0, applist.get(position).clone());
					mPacs.add(0, applist.get(position).clone());
				}
			}
		});
	}

	//edit extramenu, delete icon app, sort icon app
	private void editExtraMenu(){
		gr_content.startEditMode();
		btn_etra_edit.setText(BTN_EDIT_XONG);
		is_autoclose = false;

		//show the delete icon
		GridView gr_applist = gr_content;
		for (int i = 0; i < gr_applist.getChildCount(); i++) {
			View v = gr_applist.getChildAt(i);
			ImageView img_delete = (ImageView) v.findViewById(R.id.img_extramenu_app_delete);
			img_delete.setImageResource(R.drawable.icon_exit_mini);
			img_delete.setEnabled(true);
		}
	}

	//get applist from system, if numofapp = -1, get all ap
	private ArrayList<AppInfo> getApplistFromSystem(PackageManager pm, int numofapp){
		ArrayList<AppInfo> applist = new ArrayList<AppInfo>();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> pacsList = pm.queryIntentActivities(mainIntent, 0);

		int num=0;
		if(numofapp == -1)
			num = pacsList.size();
		else
			num = (numofapp < pacsList.size()) ? numofapp:pacsList.size();
		for(int i=0; i< num; i++){
			AppInfo p = new AppInfo();
			p.setInfo(pacsList.get(i), pm);
			applist.add(p);
		}
		return applist;
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
	GridView gr_add_app;
	//LinearLayout ll_content;
	LinearLayout llExtraMenu;
	LinearLayout llAppAdd;
	Button btn_etra_edit;
	RelativeLayout rl_background;

	
	ArrayList<AppInfo> mPacs;
	final ExtraMenuItemAdapter exmiAdapter;
	boolean is_autoclose;
}

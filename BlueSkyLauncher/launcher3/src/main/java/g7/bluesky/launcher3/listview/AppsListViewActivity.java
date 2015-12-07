package g7.bluesky.launcher3.listview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.Launcher;
import g7.bluesky.launcher3.R;
import g7.bluesky.launcher3.compat.LauncherActivityInfoCompat;
import g7.bluesky.launcher3.compat.LauncherAppsCompat;
import g7.bluesky.launcher3.compat.UserHandleCompat;
import g7.bluesky.launcher3.compat.UserManagerCompat;
import g7.bluesky.launcher3.setting.SettingConstants;
import g7.bluesky.launcher3.util.LauncherUtil;

/**
 * Created by tuanpt on 10/13/2015.
 */
public class AppsListViewActivity extends AppCompatActivity {

    List<AppInfo> listApps;
    private LauncherAppsCompat mLauncherApps;
    private UserManagerCompat mUserManager;
    private AppsListAdapter appListAdapter;
    private SharedPreferences defaultSharedPref;
    private int textColor;

    public AppsListViewActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list_view);
        final ListView listView = (ListView) findViewById(R.id.list_view);
        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);

        textColor = getResources().getColor(R.color.quantum_panel_text_color);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.sky);
        int drawableOpacity = 255;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textColor = extras.getInt("TEXT_COLOR", getResources().getColor(R.color.quantum_panel_text_color));
            drawable = ContextCompat.getDrawable(this, extras.getInt("BG_DRAWABLE_ID", R.drawable.sky));
            drawableOpacity = extras.getInt("BG_OPACITY", 255);
        }

        drawable.setAlpha(drawableOpacity);
        listView.getRootView().setBackground(drawable);

        inputSearch.setTextColor(textColor);

        listApps = new ArrayList<>();

        // Get preference
        defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Adding items to listview
        mUserManager = UserManagerCompat.getInstance(this);
        final List<UserHandleCompat> profiles = mUserManager.getUserProfiles();

        mLauncherApps = LauncherAppsCompat.getInstance(this);
        for (UserHandleCompat user : profiles) {
            List<LauncherActivityInfoCompat> apps = mLauncherApps.getActivityList(null, user);

            for (int i = 0; i < apps.size(); i++) {
                LauncherActivityInfoCompat app = apps.get(i);
                AppInfo appInfo = new AppInfo();
                appInfo.setComponentName(app.getComponentName());
                appInfo.setTitle(app.getLabel());
                appInfo.setIconDrawable(app.getApplicationInfo().loadIcon(getPackageManager()));
                listApps.add(appInfo);
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Launch app
                AppInfo selectedApp = (AppInfo) parent.getItemAtPosition(position);
                //Intent intent = selectedApp.getIntent();
                Intent intent = getPackageManager().getLaunchIntentForPackage(selectedApp.getComponentName().getPackageName());
                if (intent != null) {
                    LauncherUtil.increaseAppLaunchTimes(defaultSharedPref, selectedApp.getComponentName().getClassName());
                    startActivity(intent);
                }
            }
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Show app details
                AppInfo selectedApp = (AppInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", selectedApp.getComponentName().getPackageName(), null));
                startActivity(intent);
                return true;
            }
        });

        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                AppsListViewActivity.this.appListAdapter.getFilter().filter(arg0.toString());
            }
        });

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Clear spinner text
                ((TextView) view).setText(null);

                SharedPreferences.Editor editor = defaultSharedPref.edit();
                editor.putInt(SettingConstants.SORT_PREF_KEY, position);
                editor.apply();

                // Value from preference
                int prefVal = defaultSharedPref.getInt(SettingConstants.SORT_PREF_KEY, SettingConstants.SORT_A_Z);

                LauncherUtil.sortListApps(defaultSharedPref, listApps, prefVal);
                appListAdapter = new AppsListAdapter(getBaseContext(), listApps);
                listView.setAdapter(appListAdapter);
                appListAdapter.setTextColor(textColor);
                appListAdapter.notifyDataSetChanged();
                listView.invalidateViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Creating adapter for spinner
        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this, R.array.sort_options, android.R.layout.simple_spinner_item);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // Set default value
        final int prefSortOptionVal = defaultSharedPref.getInt(SettingConstants.SORT_PREF_KEY, SettingConstants.SORT_A_Z);
        spinner.setSelection(prefSortOptionVal);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_apps_list_view, menu);
        return true;
    }

    @Override
    public void onBackPressed(){
        startActivity( new Intent(this, Launcher.class) );
        //finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package g7.bluesky.launcher3.listview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.Launcher;
import g7.bluesky.launcher3.LauncherAppState;
import g7.bluesky.launcher3.LauncherModel;
import g7.bluesky.launcher3.R;
import g7.bluesky.launcher3.compat.LauncherActivityInfoCompat;
import g7.bluesky.launcher3.compat.LauncherAppsCompat;
import g7.bluesky.launcher3.compat.UserHandleCompat;
import g7.bluesky.launcher3.compat.UserManagerCompat;

/**
 * Created by tuanpt on 10/13/2015.
 */
public class AppsListViewActivity extends AppCompatActivity {

    List<AppInfo> listApps;
    private LauncherAppsCompat mLauncherApps;
    private UserManagerCompat mUserManager;
    private AppsListAdapter appListAdapter;

    public AppsListViewActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list_view);
        ListView listView = (ListView) findViewById(R.id.list_view);
        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);

        int textColor = getResources().getColor(R.color.quantum_panel_text_color);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            textColor = extras.getInt("TEXT_COLOR", getResources().getColor(R.color.quantum_panel_text_color));
        }

        inputSearch.setTextColor(textColor);

        listApps = new ArrayList<>();

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

        // Sort by name A-Z
        Collections.sort(listApps, new AppComparator());
        // Sort by name Z-A
        //Collections.reverse(listApps);

        appListAdapter = new AppsListAdapter(getBaseContext(), listApps);
        listView.setAdapter(appListAdapter);
        appListAdapter.setTextColor(textColor);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Launch app
                AppInfo selectedApp = (AppInfo) parent.getItemAtPosition(position);
                //Intent intent = selectedApp.getIntent();
                Intent intent = getPackageManager().getLaunchIntentForPackage(selectedApp.getComponentName().getPackageName());
                if (intent != null) {
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
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                AppsListViewActivity.this.appListAdapter.getFilter().filter(arg0.toString());
            }
        });
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

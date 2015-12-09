package g7.bluesky.launcher3.listview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
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
import g7.bluesky.launcher3.Utilities;
import g7.bluesky.launcher3.compat.LauncherActivityInfoCompat;
import g7.bluesky.launcher3.compat.LauncherAppsCompat;
import g7.bluesky.launcher3.compat.UserHandleCompat;
import g7.bluesky.launcher3.compat.UserManagerCompat;
import g7.bluesky.launcher3.setting.SettingConstants;
import g7.bluesky.launcher3.setting.ThemeTools;
import g7.bluesky.launcher3.setting.Tools;
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

        loadIconPack();

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
        startActivity(new Intent(this, Launcher.class));
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

    public void loadIconPack() {
        // theming vars-----------------------------------------------
        PackageManager pm = getPackageManager();
        final int ICONSIZE = Tools.numtodp(65, AppsListViewActivity.this);
        Resources themeRes = null;
        String resPacName = defaultSharedPref.getString(SettingConstants.ICON_THEME_PREF_KEY, "");
        String iconResource = null;
        int intres = 0;
        int intresiconback = 0;
        int intresiconfront = 0;
        int intresiconmask = 0;
        float scaleFactor = 1.0f;

        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        p.setAntiAlias(true);

        Paint origP = new Paint(Paint.FILTER_BITMAP_FLAG);
        origP.setAntiAlias(true);

        Paint maskp = new Paint(Paint.FILTER_BITMAP_FLAG);
        maskp.setAntiAlias(true);
        maskp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        if (resPacName.compareTo("") != 0) {
            try {
                themeRes = pm.getResourcesForApplication(resPacName);
            } catch (Exception e) {
            }
            ;
            if (themeRes != null) {
                String[] backAndMaskAndFront = ThemeTools.getIconBackAndMaskResourceName(themeRes, resPacName);
                if (backAndMaskAndFront[0] != null)
                    intresiconback = themeRes.getIdentifier(backAndMaskAndFront[0], "drawable", resPacName);
                if (backAndMaskAndFront[1] != null)
                    intresiconmask = themeRes.getIdentifier(backAndMaskAndFront[1], "drawable", resPacName);
                if (backAndMaskAndFront[2] != null)
                    intresiconfront = themeRes.getIdentifier(backAndMaskAndFront[2], "drawable", resPacName);
            }
        }

        BitmapFactory.Options uniformOptions = new BitmapFactory.Options();
        uniformOptions.inScaled = false;
        uniformOptions.inDither = false;
        uniformOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Canvas origCanv;
        Canvas canvas;
        scaleFactor = ThemeTools.getScaleFactor(themeRes, resPacName);
        Bitmap back = null;
        Bitmap mask = null;
        Bitmap front = null;
        Bitmap scaledBitmap = null;
        Bitmap scaledOrig = null;
        Bitmap orig = null;

        if (resPacName.compareTo("") != 0 && themeRes != null) {
            try {
                if (intresiconback != 0)
                    back = BitmapFactory.decodeResource(themeRes, intresiconback, uniformOptions);
            } catch (Exception e) {
            }
            try {
                if (intresiconmask != 0)
                    mask = BitmapFactory.decodeResource(themeRes, intresiconmask, uniformOptions);
            } catch (Exception e) {
            }
            try {
                if (intresiconfront != 0)
                    front = BitmapFactory.decodeResource(themeRes, intresiconfront, uniformOptions);
            } catch (Exception e) {
            }
        }
        // theming vars-----------------------------------------------
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        for (int I = 0; I < listApps.size(); I++) {
            if (themeRes != null) {
                iconResource = null;
                intres = 0;
                iconResource = ThemeTools.getResourceName(themeRes, resPacName, listApps.get(I).getComponentName().toString());
                if (iconResource != null) {
                    intres = themeRes.getIdentifier(iconResource, "drawable", resPacName);
                }

                if (intres != 0) {//has single drawable for app
                    listApps.get(I).setIconDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(themeRes, intres, uniformOptions)));
                } else {
                    Drawable drawable = listApps.get(I).getIconDrawable();
                    if (drawable == null) {
                        drawable = Utilities.createIconDrawable(listApps.get(I).getIconBitmap());
                    }
                    orig = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    drawable.draw(new Canvas(orig));

                    scaledOrig = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888);
                    scaledBitmap = Bitmap.createBitmap(ICONSIZE, ICONSIZE, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(scaledBitmap);
                    if (back != null) {
                        canvas.drawBitmap(back, Tools.getResizedMatrix(back, ICONSIZE, ICONSIZE), p);
                    }

                    origCanv = new Canvas(scaledOrig);
                    orig = Tools.getResizedBitmap(orig, ((int) (ICONSIZE * scaleFactor)), ((int) (ICONSIZE * scaleFactor)));
                    origCanv.drawBitmap(orig, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, scaledOrig.getWidth() - (orig.getWidth() / 2) - scaledOrig.getWidth() / 2, origP);

                    if (mask != null) {
                        origCanv.drawBitmap(mask, Tools.getResizedMatrix(mask, ICONSIZE, ICONSIZE), maskp);
                    }

                    if (back != null) {
                        canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0, 0, p);
                    } else
                        canvas.drawBitmap(Tools.getResizedBitmap(scaledOrig, ICONSIZE, ICONSIZE), 0, 0, p);

                    if (front != null)
                        canvas.drawBitmap(front, Tools.getResizedMatrix(front, ICONSIZE, ICONSIZE), p);

                    listApps.get(I).setIconDrawable(new BitmapDrawable(getResources(), scaledBitmap));
                }
            }
        }
    }
}

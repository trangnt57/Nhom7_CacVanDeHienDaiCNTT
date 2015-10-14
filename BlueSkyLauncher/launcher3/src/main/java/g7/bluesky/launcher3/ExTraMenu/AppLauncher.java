package g7.bluesky.launcher3.ExTraMenu;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import g7.bluesky.launcher3.R;

/**
 * Created by longdt57 on 10/11/15.
 */
public class AppLauncher extends LinearLayout
{
    private AppInfo mApp;
    private Context mContext;


    public AppLauncher(Context context, AppInfo app, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mApp = app;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.extramenu_item, this, true);

        ImageView imgIcon = (ImageView) this.findViewById(R.id.app_icon);
        TextView tvName = (TextView) this.findViewById(R.id.app_name);

        imgIcon.setImageDrawable(mApp.getIconDrawable());
        tvName.setText(mApp.getTitle());

    }
}
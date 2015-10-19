package g7.bluesky.launcher3.ExTraMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import g7.bluesky.launcher3.R;

/**
 * Created by longdt57 on 10/15/15.
 */
public class AppAddItemAdapter extends BaseAdapter{

    Context mContext;
    ArrayList<AppInfo> mApplist;

    public AppAddItemAdapter(Context context, ArrayList<AppInfo> applist){
        mContext = context;
        mApplist = applist;
    }

    @Override
    public int getCount() {
        return mApplist.size();
    }

    @Override
    public Object getItem(int position) {
        return mApplist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView app_icon;
        TextView app_name;
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = li.inflate(R.layout.extra_app_add_item, null);


        }
        app_icon = (ImageView) convertView.findViewById(R.id.im_add_app_icon);
        app_name = (TextView) convertView.findViewById(R.id.tv_add_app_name);

        AppInfo app = (AppInfo)getItem(position);
        app_icon.setImageDrawable(app.getIconDrawable());
        app_name.setText(app.getTitle());

        return convertView;
    }
}

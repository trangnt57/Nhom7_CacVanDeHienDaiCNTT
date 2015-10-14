package g7.bluesky.launcher3.ExTraMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import g7.bluesky.launcher3.ExTraMenu.dynamicgrid.BaseDynamicGridAdapter;
import g7.bluesky.launcher3.R;

public class ExtraMenuItemAdapter extends BaseDynamicGridAdapter{
	Context mContext;
	//ArrayList<AppInfo> mPacs;

	public ExtraMenuItemAdapter (Context context, ArrayList<AppInfo> pacs, int columnCount){
		super(context, pacs, columnCount);
		mContext = context;
		//mPacs = pacs;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ImageView app_icon;
		TextView app_name;
		ImageView img_app_delete;
		LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null){
			convertView = li.inflate(R.layout.extramenu_item, null);
			
			
		}
		app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
		app_name = (TextView) convertView.findViewById(R.id.app_name);
		img_app_delete = (ImageView) convertView.findViewById(R.id.img_extramenu_app_delete);

		AppInfo app = (AppInfo)getItem(position);
		app_icon.setImageDrawable(app.getIconDrawable());
		app_name.setText(app.getTitle());
		img_app_delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getItems().remove(position);
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}

}

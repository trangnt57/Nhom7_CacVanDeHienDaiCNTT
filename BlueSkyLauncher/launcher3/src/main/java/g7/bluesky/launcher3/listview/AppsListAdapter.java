package g7.bluesky.launcher3.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.BubbleTextView;
import g7.bluesky.launcher3.R;
import g7.bluesky.launcher3.util.StringUtil;

/**
 * Created by tuanpt on 10/13/2015.
 */
public class AppsListAdapter extends ArrayAdapter<AppInfo> implements Filterable {

    private Context context;
    private Filter appListFilter;
    private List<AppInfo> appList;
    private List<AppInfo> originalAppList;
    private int textColor;

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public AppsListAdapter(Context context, List<AppInfo> appList) {
        super(context, R.layout.app_list_item, appList);
        this.appList = appList;
        this.originalAppList = appList;
        this.context = context;
        textColor = getContext().getResources().getColor(R.color.quantum_panel_text_color);
    }

    @Override
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return appList == null ? 0 : appList.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return appList == null ? null : appList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AppInfo app = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.app_list_item, parent, false);
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.item_app_icon);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.item_app_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.iconView.setImageDrawable(app.getIconDrawable());
        viewHolder.appName.setText(app.getTitle());
        viewHolder.appName.setTextColor(textColor);
        // Return the completed view to render on screen
        return convertView;
    }

    public void resetData() {
        this.appList = this.originalAppList;
    }

    @Override
    public Filter getFilter() {
        return appListFilter == null ? new AppListFilter() : appListFilter;
    }

    // View lookup cache
    private static class ViewHolder {
        ImageView iconView;
        TextView appName;
    }

    private class AppListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                filterResults.values = originalAppList;
                filterResults.count = originalAppList.size();
            } else {
                List<AppInfo> searchList = new ArrayList<>();

                for (AppInfo app : originalAppList) {
                    String appTitle = StringUtil.convertVNString(app.getTitle().toString().toLowerCase().trim());
                    constraint = StringUtil.convertVNString(constraint.toString().toLowerCase().trim());
                    if (appTitle.contains(constraint)) {
                        searchList.add(app);
                    }
                }

                filterResults.values = searchList;
                filterResults.count = searchList.size();
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0) {
                notifyDataSetInvalidated();
            } else {
                appList = (List<AppInfo>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}

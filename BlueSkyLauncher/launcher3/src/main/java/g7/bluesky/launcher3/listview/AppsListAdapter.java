package g7.bluesky.launcher3.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import g7.bluesky.launcher3.AppInfo;
import g7.bluesky.launcher3.R;
import g7.bluesky.launcher3.util.StringUtil;

/**
 * Created by tuanpt on 10/13/2015.
 */
public class AppsListAdapter extends ArrayAdapter<AppInfo> implements Filterable, SectionIndexer {

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

    Map<String, Integer> mapIndex = new LinkedHashMap<>();
    String[] sections;

    public AppsListAdapter(Context context, List<AppInfo> appList) {
        super(context, R.layout.app_list_item, appList);
        this.appList = appList;
        this.originalAppList = appList;
        this.context = context;
        textColor = getContext().getResources().getColor(R.color.quantum_panel_text_color);
        calculateSections();
    }

    void calculateSections() {
        mapIndex = new LinkedHashMap<>();

        for (int x = 0; x < appList.size(); x++) {
            AppInfo fruit = appList.get(x);
            String ch = StringUtil.convertVNString(fruit.getTitle().toString()).substring(0, 1);
            ch = ch.toUpperCase(Locale.US);
            if (mapIndex.get(ch) == null) {
                mapIndex.put(ch, x);
            }
        }

        Set<String> sectionLetters = mapIndex.keySet();
        List<String> sectionList = new ArrayList<>(sectionLetters);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);
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
            if (results != null) {
                appList = (List<AppInfo>) results.values;
                calculateSections();
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Returns an array of objects representing sections of the list. The
     * returned array and its contents should be non-null.
     * <p/>
     * The list view will call toString() on the objects to get the preview text
     * to display while scrolling. For example, an adapter may return an array
     * of Strings representing letters of the alphabet. Or, it may return an
     * array of objects whose toString() methods return their section titles.
     *
     * @return the array of section objects
     */
    @Override
    public Object[] getSections() {
        return sections;
    }

    /**
     * Given the index of a section within the array of section objects, returns
     * the starting position of that section within the adapter.
     * <p/>
     * If the section's starting position is outside of the adapter bounds, the
     * position must be clipped to fall within the size of the adapter.
     *
     * @param sectionIndex the index of the section within the array of section
     *                     objects
     * @return the starting position of that section within the adapter,
     * constrained to fall within the adapter bounds
     */
    @Override
    public int getPositionForSection(int sectionIndex) {
        return mapIndex.get(sections[sectionIndex]);
    }

    /**
     * Given a position within the adapter, returns the index of the
     * corresponding section within the array of section objects.
     * <p/>
     * If the section index is outside of the section array bounds, the index
     * must be clipped to fall within the size of the section array.
     * <p/>
     * For example, consider an indexer where the section at array index 0
     * starts at adapter position 100. Calling this method with position 10,
     * which is before the first section, must return index 0.
     *
     * @param position the position within the adapter for which to return the
     *                 corresponding section index
     * @return the index of the corresponding section within the array of
     * section objects, constrained to fall within the array bounds
     */
    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}

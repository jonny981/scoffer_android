package com.scoff.scoffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OfferFeedAdapter extends BaseAdapter implements Filterable {

    private ArrayList<OfferItem> originalData = null;
    private ArrayList<OfferItem> filteredData = null;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private ItemFilter feedFilter = new ItemFilter();
    private ArrayList<Integer> favourites;
    private SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    int favouriteColour, standardColour;


    public OfferFeedAdapter(Context context, ArrayList<OfferItem> originalData) {
        this.originalData = originalData;
        this.filteredData = originalData;
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();
        favouriteColour = mContext.getResources().getColor(R.color.favourite_selected);
        standardColour = mContext.getResources().getColor(R.color.background_fill);
        favourites = new ArrayList<>();
        getFavourites();
        sortLists();
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    public Filter getFilter() {
        return feedFilter;
    }

    public void showAll() {
        filteredData = originalData;
        sortLists();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //allows re-use of views depending on whether Android determines they are visible or not
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        //convertView is the list item
        //parent is the listView
        //if no view is available for re-use then pass covertView a new view
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_layout, null);
            holder = new ViewHolder();

            //link the data object to the XML
            holder.offerTitle = (TextView) convertView.findViewById(R.id.title);
            holder.companyName = (TextView) convertView.findViewById(R.id.companyName);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            holder.offerImage = (ImageView) convertView.findViewById(R.id.thumbImage);
            holder.star = (CheckBox) convertView.findViewById(R.id.btn_star);
            //assign a reference for this view to it's holder so it can be re-used
            convertView.setTag(holder);
        } else {
            //if a view has already been created then re-use it
            holder = (ViewHolder) convertView.getTag();
        }
        final View colourView = convertView;

        //set the text in the widgets
        final OfferItem offer = filteredData.get(position);
        holder.offerTitle.setText(offer.getTitle());
        holder.companyName.setText(offer.getCompanyName());
        String distance = String.valueOf(offer.getDistance() + " miles");
        holder.distance.setText(distance);

        getFavourites();
        holder.star.setOnCheckedChangeListener(null);
        if (favourites.contains(offer.getCompanyID())) {
            holder.star.setChecked(true);
            offer.setFavourite(true);
            filteredData.get(position).setFavourite(true);
            convertView.setBackgroundColor(favouriteColour);
        } else {
            holder.star.setChecked(false);
            offer.setFavourite(false);
            filteredData.get(position).setFavourite(false);
            convertView.setBackgroundColor(standardColour);
        }

        holder.star.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setItemAsFavourite(position, colourView, offer, true);
                } else if (favourites.contains(offer.getCompanyID())) {
                    setItemAsFavourite(position, colourView, offer, false);
                }
                String faveToast = isChecked ? " saved in favourites" : " removed from favourites";
                Toast toast = Toast.makeText(mContext, offer.getCompanyName() + faveToast,
                        Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setBackgroundResource(R.color.scoffer_red);
                toast.show();
                storeFavourites(favourites);
            }
        });

        AQuery aq = new AQuery(convertView);
        aq.id(holder.offerImage).image(offer.getImageURL(), true, true, 0, R.drawable.no_image, null, AQuery.FADE_IN_NETWORK, 1.0f);

        //get the offer images asynchronously
//        if (holder.offerImage != null) {
//            holder.offerImage.setImageDrawable(holder.offerImage.getContext().getResources().getDrawable(R.drawable.list_placeholder));
//            new GetImagesAsync(holder.offerImage).execute(offer.getImageURL());
//        }
        return convertView;
    }

    public void setItemAsFavourite(int position, View colourView, OfferItem offer, boolean setting) {
        boolean showFavesAtTop = sharedPref.getBoolean("favourites_top", true);
        int colour = setting ? favouriteColour : standardColour;

        if (setting) {
            favourites.add(offer.getCompanyID());
        } else {
            int index = favourites.indexOf(offer.getCompanyID());
            favourites.remove(index);
        }

        filteredData.get(position).setFavourite(setting);
        offer.setFavourite(setting);
        colourView.setBackgroundColor(colour);
        if (showFavesAtTop) {
            sortLists();
        }
    }

    //
    static class ViewHolder {
        TextView offerTitle;
        TextView companyName;
        TextView distance;
        ImageView offerImage;
        CheckBox star;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new Filter.FilterResults();

            final ArrayList<OfferItem> originalList = originalData;

            int count = originalList.size();
            final ArrayList<OfferItem> filteredList = new ArrayList<OfferItem>(count);

            OfferItem filterItem;

            for (int i = 0; i < count; i++) {
                filterItem = originalList.get(i);

                if (filterString.equalsIgnoreCase("cafe") ||
                        filterString.equalsIgnoreCase("restaurant") ||
                        filterString.equalsIgnoreCase("bar") ||
                        filterString.equalsIgnoreCase("")) {
                    if (filterItem.getType().toLowerCase().contains(filterString)) {
                        filteredList.add(filterItem);
                    }
                } else {
                    if (filterItem.getCompanyName().toLowerCase().contains(filterString) || filterItem.getTitle().toLowerCase().contains(filterString)) {
                        filteredList.add(filterItem);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<OfferItem>) results.values;
            sortLists();
        }
    }

    public void sortLists() {
        boolean showFavesAtTop = sharedPref.getBoolean("favourites_top", true);

        for(int i = 0;i < originalData.size(); i++){
            Log.e("offer" + i, originalData.get(i).toString());
        }

        if (showFavesAtTop) {
            Collections.sort(originalData, new OfferComparatorFavouritesTop());
            Collections.sort(filteredData, new OfferComparatorFavouritesTop());
        } else {
            Collections.sort(filteredData, new OfferComparator());
            Collections.sort(originalData, new OfferComparator());
        }
        notifyDataSetChanged();
    }

    public void storeFavourites(ArrayList<Integer> savedFaves) {
        List<String> savedFavesToStrings = new ArrayList<>(savedFaves.size());
        for (Integer myInt : savedFaves) {
            savedFavesToStrings.add(String.valueOf(myInt));
        }
        Set<String> set = new HashSet<>();
        set.addAll(savedFavesToStrings);
        editor.putStringSet("key", set);
        editor.commit();
    }

    public void getFavourites() {
        favourites = new ArrayList<>();
        Set<String> set = sharedPref.getStringSet("key", null);
        ArrayList<Integer> favesFromPrefs = new ArrayList<>();

        if ((set!=null) && (set.size() > 0)) {
            for (String retreived : set) {
                favesFromPrefs.add(Integer.parseInt(retreived));
            }
            favourites = favesFromPrefs;
            Collections.sort(favourites);
        }
    }
}


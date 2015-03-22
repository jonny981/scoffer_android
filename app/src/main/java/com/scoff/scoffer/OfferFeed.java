package com.scoff.scoffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.util.AQUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class OfferFeed extends ListActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private static final String SERVER_URL = "http://scoffer.ddns.net/scripts/get_offers.php";
//    private static final String SERVER_URL = "http://192.168.1.15/scripts/get_offers.php";
    private static final String[] TRANSPORT_MODES = {"Walking", "Cycling", "Driving"};
    private static ArrayList<String> COMPANY_TYPES = new ArrayList<>(Arrays.asList("All", "Cafe", "Restaurant", "Bar/Club"));
    final Context CONTEXT = this;
    private static final String TAG = "SCOFFER DEBUG: OfferFeed";

    private ArrayList<OfferItem> feedList;
    private ArrayAdapter<String> transportAdapter, typeAdapter;
    private ProgressBar progressbar;
    private ListView feedListView;
    private TextView errorText, errorTitle;
    private ImageView whoopsIcon;
    private RelativeLayout errorLayout;
    private Button errorAction;
    private double latitude, longitude;
    private int currentRadius;
    private Typeface blenda;
    private String errorState = null, filterString;
    private PrefsHelper prefsHelper;
    protected GoogleApiClient mGoogleApiClient;
    protected Location lastLocation;
    private PreferenceChangeListener mPreferenceListener = null;
    MenuItem searchMenuItem;
    SearchView searchView;
    SharedPreferences prefs;

    Spinner transportMode, companyType;
    OfferFeedAdapter feedAdapter;

    //-------------------------------------------------- LIFECYLE EVENTS --------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "OfferFeed activity created");
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_item);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        setContentView(R.layout.activity_offer_feed);
        blenda = Typeface.createFromAsset(getAssets(), "fonts/BlendaScript.otf");
        progressbar = (ProgressBar) findViewById(R.id.progressBar);
        feedListView = this.getListView();
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);

        feedListView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        ArrayList<OfferItem> savedFeed = null;

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefsHelper = new PrefsHelper(CONTEXT);
        currentRadius = prefsHelper.getTransportRadiusSetting();
        Log.e("Radius", String.valueOf(currentRadius));

        try {
            errorState = savedInstanceState.getString("errorState");
            savedFeed = (ArrayList<OfferItem>) savedInstanceState.getSerializable("feedList");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (errorState == null) {
            //to prevent database call on when changing between activities, rotating screen or screen lock
            if (savedFeed != null) {
                Log.e(TAG, "Saved feed retrieved from bundle");
                feedList = savedFeed;
                buildFeedList();
            }
        } else {
            setErrorView(errorState);
        }


        transportAdapter = new ArrayAdapter<>(
                OfferFeed.this, android.R.layout.simple_dropdown_item_1line,
                TRANSPORT_MODES);

        typeAdapter = new ArrayAdapter<>(
                OfferFeed.this, android.R.layout.simple_dropdown_item_1line,
                COMPANY_TYPES);

        transportMode = (Spinner) findViewById(R.id.transportSpinner);
        companyType = (Spinner) findViewById(R.id.typeSpinner);

        transportMode.setDropDownWidth(400);
        transportMode.setDropDownVerticalOffset(1);

        companyType.setDropDownWidth(400);
        companyType.setDropDownVerticalOffset(1);

        transportMode.setAdapter(transportAdapter);
        companyType.setAdapter(typeAdapter);

        companyType.setSelection(prefsHelper.getFilterSpinnerSetting(), false);
        transportMode.setSelection(prefsHelper.getTransportSpinnerSetting(), false);

        transportMode.setOnItemSelectedListener(this);
        companyType.setOnItemSelectedListener(this);
        transportMode.setVisibility(View.INVISIBLE);
        companyType.setVisibility(View.INVISIBLE);

        filterString = prefsHelper.getFilterType();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        prefs.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        buildGoogleApiClient();
        getOverflowMenu();
    }

    private class PreferenceChangeListener implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            String keyChanged = key.toLowerCase();
            if (key.equalsIgnoreCase("favourites_top")) {
                feedAdapter.sortLists();
            }

            if ((key.equalsIgnoreCase("walking_value")) || (key.equalsIgnoreCase("cycling_value")) || (key.equalsIgnoreCase("driving_value"))) {
                String currentSelection = transportMode.getSelectedItem().toString().toLowerCase();
                if (keyChanged.contains(currentSelection)) {
                    refreshList();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        feedList = (ArrayList<OfferItem>) savedInstanceState.getSerializable("feedList");
        errorState = savedInstanceState.getString("errorState");
    }

    //------------------------------------------------- MENU AND CUSTOM SPINNERS --------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem share = menu.findItem(R.id.action_share);
        share.setVisible(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (feedList != null) {
                    hideKeyboard();
                    searchMenuItem.collapseActionView();
                    searchView.setIconified(true);
                    searchView.clearFocus();
                    filterList(query);
                    if (query != null) {
                        if (!COMPANY_TYPES.get(0).equalsIgnoreCase("custom")) {
                            COMPANY_TYPES.add(0, "Custom");
                        }
                        typeAdapter.notifyDataSetChanged();
                        companyType.setSelection(0, false);
                    }
                    return true;
                } else {
                    Toast toast = Toast.makeText(CONTEXT, "Sorry! Your feed is currently not populated. " +
                            "You may need to increase your travel distance.", Toast.LENGTH_LONG);
                    View view = toast.getView();
                    view.setBackgroundResource(R.color.scoffer_red);
                    toast.show();
                    return false;
                }
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return super.onCreateOptionsMenu(menu);
    }

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_refresh:
                refreshList();
                return true;
            case R.id.action_about:
                startActivity(new Intent(OfferFeed.this, About.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(OfferFeed.this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        try {
            switch (parent.getItemAtPosition(pos).toString()) {
                case "Walking":
                    refreshList();
                    break;
                case "Cycling":
                    refreshList();
                    break;
                case "Driving":
                    refreshList();
                    break;
                case "All":
                    filterList("all");
                    removeCustomSearch(pos);
                    break;
                case "Restaurant":
                    filterList("Restaurant");
                    removeCustomSearch(pos);
                    break;
                case "Cafe":
                    filterList("Cafe");
                    removeCustomSearch(pos);
                    break;
                case "Bar/Club":
                    filterList("Bar");
                    removeCustomSearch(pos);
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //------------------------------------------------ GOOGLE PLAY SERVICES ------------------------------------------------------------------
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (feedList == null) {
            refreshList();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        progressbar.setVisibility(View.GONE);
        Log.i("Google play connection problem: ", "ConnectionResult.getErrorCode() = " + result.getErrorCode());
        setErrorView("connectionerror");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //re-establish the connection if it was lost
        Log.i("Connection problem: ", "Connection suspended");
        mGoogleApiClient.connect();
    }

    //------------------------------------------------ DATA HANDLERS -------------------------------------------------------------------------
    private class getJSONData extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG, "onPostExecute() called");
            if (feedList != null) {
                buildFeedList();
            } else {
                feedList = new ArrayList<>();
                feedAdapter = new OfferFeedAdapter(CONTEXT, feedList);
                feedListView.setAdapter(feedAdapter);
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.e(TAG, "doInBackGround() called");
            String url = params[0];
            JSONObject jsonObject = getOffersFromDatabase(url);
            if (jsonObject != null) {
                parseJSONToFeedItem(jsonObject);
            }
            return null;
        }
    }

    public JSONObject getOffersFromDatabase(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        JSONObject object = null;

        Log.e(TAG, "current selection:" + transportMode.getSelectedItem().toString());

        switch (transportMode.getSelectedItem().toString().toLowerCase()) {
            case "walking":
                currentRadius = prefsHelper.getWalkingDistance();
                break;
            case "cycling":
                currentRadius = prefsHelper.getCyclingDistance();
                break;
            case "driving":
                currentRadius = prefsHelper.getDrivingDistance();
                break;
        }

        Log.e(TAG, "getOffersFromDatabase() called with radius: " + currentRadius);

        String geoQuery = "{\"radius\":\"" + currentRadius + "\",\"latitude\":\"" + latitude + "\",\"longitude\":\"" + longitude + "\"}";

        try {
            StringEntity params = new StringEntity(geoQuery);
//        httppost.addHeader("content-type", "application/x-www-form-urlencoded");
            httppost.setEntity(params);
            HttpResponse response = httpclient.execute(httppost);
            String jsonResult = inputStreamToString(response.getEntity().getContent()).toString();
            Log.e("JSON response:", jsonResult);
            object = new JSONObject(jsonResult);

        } catch (JSONException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setErrorView("connectionerror");
                }
            });
            e.printStackTrace();
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (ClientProtocolException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setErrorView("connectionerror");
                }
            });
            e.printStackTrace();
        } catch (IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setErrorView("connectionerror");
                }
            });
            e.printStackTrace();
        }
        return object;
    }

    private StringBuilder inputStreamToString(InputStream is) {
        String rLine;
        StringBuilder buildJsonString = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            while ((rLine = rd.readLine()) != null) {
                buildJsonString.append(rLine);
            }
        } catch (IOException e) {
            setErrorView("technical");
            e.printStackTrace();
        }
        return buildJsonString;
    }

    public void parseJSONToFeedItem(JSONObject json) {
        Log.e(TAG, "parseJSONToFeedItem() called");
        feedList = new ArrayList<>();
        try {
            if (Integer.parseInt(json.getString("totalOffers")) > 0) {
                JSONArray offers = json.getJSONArray("offers");
//                feedList = new ArrayList<>();
                for (int i = 0; i < offers.length(); i++) {
                    OfferItem offerItem = new OfferItem();
                    offerItem.buildFromJSON(offers.getJSONObject(i), i);
                    try {
                        offerItem.setFavourite(prefsHelper.checkIfFavourite(offerItem.getCompanyID()));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
//                        offerItem.setFavourite(false);
                    }
                    feedList.add(offerItem);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setErrorView("noresults");
                    }
                });
            }
        } catch (JSONException e) {
            setErrorView("technical");
            e.printStackTrace();
        }
    }

    public void buildFeedList() {
        Log.e(TAG, "buildFeedList() called");
        try {
            feedAdapter = new OfferFeedAdapter(this, feedList);
            feedListView.setAdapter(feedAdapter);
//            showFeedLayout();
            feedListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Object o = feedListView.getItemAtPosition(position);
                    OfferItem newsData = (OfferItem) o;

                    Intent intent = new Intent(OfferFeed.this, OfferItemDetail.class);
                    intent.putExtra("feed", newsData);
                    startActivity(intent);
                }
            });

            filterList(filterString);
            errorState = null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void refreshList() {
        Log.e(TAG, "refreshList() called");
        showProgressLayout();
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
            prefsHelper.saveLatLong(latitude, longitude);
            new getJSONData().execute(SERVER_URL);
        } else if (prefsHelper.useSavedLocation() && (prefsHelper.getSavedLatitude() + prefsHelper.getSavedLongitude() != 0)) {
            latitude = prefsHelper.getSavedLatitude();
            longitude = prefsHelper.getSavedLongitude();

            if (feedList == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT)
                        .setTitle("Using Saved Location")
                        .setMessage("Scoffer is using your last known location to gather data. To use your current location switch on GPS and tap refresh.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new getJSONData().execute(SERVER_URL);
                            }
                        })
                        .setInverseBackgroundForced(true);
                builder.show();
            } else {
                new getJSONData().execute(SERVER_URL);
            }
        } else {
            setErrorView("gps");
        }

        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
    }

    public void filterList(String filter) {
        Log.e(TAG, "filterList() called");
        filter = filter.trim();
        if (filter.equalsIgnoreCase("all")) {
            feedAdapter.showAll();
            if (feedList.size() > 0) {
                Log.e(TAG, "filterList() showing feed layout");
                showFeedLayout();
            }
        } else {
            feedAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                public void onFilterComplete(int count) {
                    if (count == 0) {
                        setErrorView("noresults");
                    } else {
                        showFeedLayout();
                    }
                }
            });
        }
        filterString = filter;
    }

    //----------------------------------------------- UI HELPERS -----------------------------------------------------------------------------
    public void showErrorLayout() {
        Log.e(TAG, "showErrorLayout() called");
        transportMode.setVisibility(View.VISIBLE);
        companyType.setVisibility(View.VISIBLE);
        feedListView.setVisibility(View.GONE);
        progressbar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void showFeedLayout() {
        Log.e(TAG, "showFeedLayout() called");
        feedListView.setVisibility(View.VISIBLE);
        transportMode.setVisibility(View.VISIBLE);
        companyType.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
    }

    public void showProgressLayout() {
        Log.e(TAG, "showProgressLayout() called");
        transportMode.setVisibility(View.GONE);
        companyType.setVisibility(View.GONE);
        feedListView.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
    }

    public void setErrorView(String error) {

        showErrorLayout();

        errorState = error;
//        feedList = null;
        whoopsIcon = (ImageView) findViewById(R.id.whoops_icon);
        errorText = (TextView) findViewById(R.id.error_message);
        errorTitle = (TextView) findViewById(R.id.something_wrong);
        errorAction = (Button) findViewById(R.id.error_action);
        errorTitle.setTypeface(blenda);
        transportMode.setVisibility(View.INVISIBLE);
        companyType.setVisibility(View.INVISIBLE);

        switch (error) {
            case "gps":
                errorText.setText("Location Services are disabled, Scoffer needs to know your location to find offers nearby. " +
                        "Enable Location Services and then refresh.");
                errorAction.setVisibility(View.VISIBLE);
                errorAction.setText("Enable Location Services");
                errorAction.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT)
                                .setTitle("Enable Location Services")
                                .setMessage("Selecting continue will navigate away from Scoffer. Please enable location services and then tap the back button.")
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent gpsOptionsIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(gpsOptionsIntent);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        builder.show();
                    }
                });
                break;
            case "noresults":
                transportMode.setVisibility(View.VISIBLE);
                companyType.setVisibility(View.VISIBLE);
                try {
                    errorAction.setVisibility(View.GONE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                errorText.setText("There are no offers that match your current search criteria, try changing the filters and try again.");
                break;
            case "connectionerror":
                try {
                    errorAction.setVisibility(View.GONE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                errorText.setText("There seems to be a problem with your connection, check that you have either mobile data or wifi switched on.");
                break;
            case "technical":
                try {
                    errorAction.setVisibility(View.GONE);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                errorText.setText("It seems we are currently experiencing technical difficulties. Please try again later");
        }
    }

    public void removeCustomSearch(int index) {
        if (COMPANY_TYPES.get(0).equalsIgnoreCase("Custom")) {
            COMPANY_TYPES.remove(0);
            typeAdapter.notifyDataSetChanged();
            companyType.setSelection(index - 1, false);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //------------------------------------------------ LIFECYCLE EVENTS ---------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        Log.e("DEBUG", "back pressed");
        searchMenuItem.collapseActionView();
        searchView.setIconified(true);
        searchView.clearFocus();
        moveTaskToBack(true);
    }

    @Override
    public void onPause() {
        super.onPause();
//        getIntent().putExtras();
//        getIntent().putParcelableArrayListExtra();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("feedList", feedList);
        savedInstanceState.putString("errorState", errorState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        //clean the file cache when root activity exit
        //the resulting total cache size will be less than 3M
        if (isTaskRoot()) {
            AQUtility.cleanCacheAsync(this);
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return feedList;
    }
}

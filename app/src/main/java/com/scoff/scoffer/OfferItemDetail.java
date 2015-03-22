package com.scoff.scoffer;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class OfferItemDetail extends FragmentActivity {

    private OfferItem offerDetail;
    // Might be null if Google Play services APK is not available.
    private GoogleMap mMap;
    private Button email, phone, weblink;
    private double latitude, longitude;
    private Typeface blenda;
    private TextView offerTitle, companyName, furtherInfoHeader, furtherInfo, companyInfoHeader, companyInfo, endDate, type, opening;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_details);
        blenda = Typeface.createFromAsset(getAssets(), "fonts/BlendaScript.otf");

        offerDetail = (OfferItem) this.getIntent().getSerializableExtra("feed");
        latitude = offerDetail.getLatitude();
        longitude = offerDetail.getLongitude();

        phone = (Button) findViewById(R.id.intent_phone);
        email = (Button) findViewById(R.id.intent_email);
        weblink = (Button) findViewById(R.id.intent_web);

        ImageView thumb = (ImageView) findViewById(R.id.detailImage);
//        new GetImagesAsync(thumb).execute(offerDetail.getImageURL());

        AQuery aq = new AQuery(this);
        thumb.setImageBitmap(aq.getCachedImage(offerDetail.getImageURL()));

        offerTitle = (TextView) findViewById(R.id.detailTitle);
        companyName = (TextView) findViewById(R.id.detailCompanyName);
        furtherInfoHeader = (TextView) findViewById(R.id.furtherInfoHeader);
        furtherInfo = (TextView) findViewById(R.id.furtherInfo);
        companyInfoHeader = (TextView) findViewById(R.id.companyInfoHeader);
        companyInfo = (TextView) findViewById(R.id.companyInfo);
        endDate = (TextView) findViewById(R.id.detailEndDate);
        type = (TextView) findViewById(R.id.detailCompanyType);
        opening = (TextView) findViewById(R.id.detailOpening);

        furtherInfoHeader.setTypeface(blenda);
        companyInfoHeader.setTypeface(blenda);

        offerTitle.setText(offerDetail.getTitle());
        companyName.setText(offerDetail.getCompanyName());
        furtherInfo.setText(offerDetail.getFurtherInfo());
        companyInfo.setText(offerDetail.getDescription());
        endDate.setText("Offer Ends: " + offerDetail.getOfferExpiryDate());
        type.setText(offerDetail.getType());

        String openingTimes = "Open " + offerDetail.getOpeningTime() + " - " + offerDetail.getClosingTime();
        opening.setText(openingTimes);
//			htmlTextView.setText(Html.fromHtml(feed.getContent(), null, null));
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "+44" + offerDetail.getPhoneNum().substring(1);
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        final String emailSubject = "Scoffer listing - \"" + offerDetail.getTitle() + "\"";

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", offerDetail.getEmailAddress(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n\nSent via Scoffer for Android");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        weblink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OfferItemDetail.this, CompanyWebsite.class);
                intent.putExtra("url", offerDetail.getUrl());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        //user shouldn't be able to refresh or search the feed list if they aren't on the feed page
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        refresh.setVisible(false);
        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(false);

        setUpMapIfNeeded();
        return super.onCreateOptionsMenu(menu);
    }

    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title(offerDetail.getCompanyName())
                .alpha(1.0f)
                .visible(true)
                .snippet(String.valueOf(offerDetail.getDistance()) + " miles"));

        marker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_about:
                startActivity(new Intent(OfferItemDetail.this, About.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(OfferItemDetail.this, SettingsActivity.class));
                return true;
            case R.id.action_share:
                shareOffer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareOffer() {
        Intent shareOfferDetails = new Intent();
        shareOfferDetails.setAction(Intent.ACTION_SEND);
        shareOfferDetails.putExtra(Intent.EXTRA_TEXT, "Check out this great offer I found on Scoffer!\n\n"
                + offerDetail.getTitle() + ", at " + offerDetail.getCompanyName() + ". \n\nFound using Scoffer for Android");
        shareOfferDetails.setType("text/plain");
        startActivity(Intent.createChooser(shareOfferDetails, "Share using"));
    }
}


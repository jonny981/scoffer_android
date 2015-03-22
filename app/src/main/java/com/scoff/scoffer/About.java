package com.scoff.scoffer;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class About extends Activity {

    private RelativeLayout[] accordions = new RelativeLayout[4];
    private RelativeLayout[] accordionsExpanded = new RelativeLayout[4];
    private ImageView[] chevrons = new ImageView[4];
    private TextView aboutContent, howtoContent, developerInfoContent, businessContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initResources();
        assignClickListeners();
    }

    public void initResources() {
        accordions[0] = (RelativeLayout) findViewById(R.id.developer_accordion);
        accordions[1] = (RelativeLayout) findViewById(R.id.business_accordion);
        accordions[2] = (RelativeLayout) findViewById(R.id.about_accordion);
        accordions[3] = (RelativeLayout) findViewById(R.id.howto_accordion);

        accordionsExpanded[0] = (RelativeLayout) findViewById(R.id.developer_accordion_expanded);
        accordionsExpanded[1] = (RelativeLayout) findViewById(R.id.business_accordion_expanded);
        accordionsExpanded[2] = (RelativeLayout) findViewById(R.id.about_accordion_expanded);
        accordionsExpanded[3] = (RelativeLayout) findViewById(R.id.howto_accordion_expanded);

        chevrons[0] = (ImageView) findViewById(R.id.dev_chevron);
        chevrons[1] = (ImageView) findViewById(R.id.bus_chevron);
        chevrons[2] = (ImageView) findViewById(R.id.about_chevron);
        chevrons[3] = (ImageView) findViewById(R.id.howto_chevron);

        aboutContent = (TextView) findViewById(R.id.about_accordion_content);
        aboutContent.setText(Html.fromHtml(getResources().getString(R.string.about_text)));
        howtoContent = (TextView) findViewById(R.id.howto_accordion_content);
        howtoContent.setText(Html.fromHtml(getResources().getString(R.string.howto_text)));
        developerInfoContent = (TextView) findViewById(R.id.developer_accordion_content);
        developerInfoContent.setText(Html.fromHtml(getResources().getString(R.string.developer_info_text)));
        developerInfoContent.setMovementMethod(LinkMovementMethod.getInstance());
        businessContent = (TextView) findViewById(R.id.business_accordion_content);
        businessContent.setText(Html.fromHtml(getResources().getString(R.string.business_use_text)));
        businessContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void assignClickListeners() {
        for (int i = 0; i < accordions.length; i++) {
            final int index = i;
            accordions[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeSelectedState(index);
                }
            });
        }
    }

    public void changeSelectedState(int index) {
        Drawable exp = getResources().getDrawable(R.drawable.ic_action_expand);
        Drawable col = getResources().getDrawable(R.drawable.ic_action_collapse);

        accordionsExpanded[index].setVisibility(accordionsExpanded[index].getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        chevrons[index].setImageDrawable(accordionsExpanded[index].getVisibility() == View.VISIBLE ? col : exp);

        for (int i = 0; i < accordionsExpanded.length; i++) {
            if ((i != index) && (accordionsExpanded[index].getVisibility() == View.VISIBLE)) {
                accordionsExpanded[i].setVisibility(View.GONE);
                chevrons[i].setImageDrawable(exp);
            }
        }
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
        MenuItem share = menu.findItem(R.id.action_share);
        share.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

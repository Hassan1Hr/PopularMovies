package com.example.hassan.popularmovies;

import android.os.Bundle;



import com.example.hassan.popularmovies.Base.BaseActivity;

public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DetailsActivityFragment detailsActivityFragment =  DetailsActivityFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.movie_detail_container, detailsActivityFragment).commit();
    }

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }
}

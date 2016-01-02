package com.example.hassan.popularmovies.Activities;

import android.os.Bundle;



import com.example.hassan.popularmovies.Activities.BaseActivity;
import com.example.hassan.popularmovies.Fragments.DetailsActivityFragment;
import com.example.hassan.popularmovies.R;

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

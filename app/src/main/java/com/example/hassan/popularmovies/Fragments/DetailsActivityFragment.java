package com.example.hassan.popularmovies.Fragments;


import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;


import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.hassan.popularmovies.Activities.BaseActivity;
import com.example.hassan.popularmovies.Activities.MyApplication;
import com.example.hassan.popularmovies.BuildConfig;
import com.example.hassan.popularmovies.Activities.DetailsActivity;
import com.example.hassan.popularmovies.Activities.MainActivity;
import com.example.hassan.popularmovies.R;
import com.example.hassan.popularmovies.adapter.TrailerAdapter;
import com.example.hassan.popularmovies.data.MovieContract;
import com.example.hassan.popularmovies.model.Movie;
import com.example.hassan.popularmovies.model.Review;
import com.example.hassan.popularmovies.model.Trailer;
import com.linearlistview.LinearListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class DetailsActivityFragment extends Fragment  {



    public static final String TAG = DetailsActivityFragment.class.getSimpleName();
    public static final String DETAIL_MOVIE = "DETAIL_MOVIE";
    private Movie mMovie;
    private ImageView posterImage;
    private ImageView backdropImage;
    private TextView mOverviewView;
    private TextView mDateView;
    private TextView mVoteAverageView;
    FloatingActionButton myFab;
    private LinearListView mTrailersView;
    private CardView mTrailersCardview;
    private TrailerAdapter mTrailerAdapter;
    private CoordinatorLayout mDetailLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toast mToast;
    TextView reviewContent;
    TextView Review;
    private ShareActionProvider mShareActionProvider;
    ArrayList<Review> reviews;
    private Trailer mTrailer;

    public DetailsActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

            inflater.inflate(R.menu.menu_fragment_detail, menu);
        MenuItem action_share = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);

        if (mTrailer != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details_activity, container, false);


        if(getActivity() instanceof MainActivity) {
            mMovie = new Movie(getArguments().getBundle(DetailsActivityFragment.DETAIL_MOVIE));


        }else if(getActivity() instanceof DetailsActivity){
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Movie.EXTRA_MOVIE)) {
                mMovie = new Movie(intent.getBundleExtra(Movie.EXTRA_MOVIE));
                if (!((BaseActivity) getActivity()).providesActivityToolbar()) {
                    // No Toolbar present. Set include_toolbar:
                    ((BaseActivity) getActivity()).setToolbar((Toolbar) rootView.findViewById(R.id.toolbar));
                }
            }
        }


            reviewContent = (TextView)rootView.findViewById(R.id.review_content);
             Review= (TextView)rootView.findViewById(R.id.movie_reviews_header);
            mDetailLayout = (CoordinatorLayout) rootView.findViewById(R.id.main_content);
            mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);


            backdropImage = (ImageView) rootView.findViewById(R.id.backdrop_image);
            posterImage = (ImageView) rootView.findViewById(R.id.poster_image);
            mOverviewView = (TextView) rootView.findViewById(R.id.detail_overview);
            mDateView = (TextView) rootView.findViewById(R.id.movie_date);
            mVoteAverageView = (TextView) rootView.findViewById(R.id.rating);
            myFab = (FloatingActionButton)  rootView.findViewById(R.id.FABFavorite);
            new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return isFavorited(getActivity(), mMovie.getId());
            }

            @Override
            protected void onPostExecute(Integer isFavorited) {
                myFab.setBackground(isFavorited == 1 ?
                        getDrawable(getActivity(), R.drawable.ic_favorite_full) :
                        getDrawable(getActivity(), R.drawable.ic_favorite_border));
            }
           }.execute();
             myFab.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     addFavorite();
                 }
             });



            mTrailersView = (LinearListView) rootView.findViewById(R.id.detail_trailers);



            mTrailersCardview = (CardView) rootView.findViewById(R.id.detail_trailers_cardview);

            mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
            mTrailersView.setAdapter(mTrailerAdapter);

            mTrailersView.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView linearListView, View view,
                                        int position, long id) {
                    Trailer trailer = mTrailerAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                    startActivity(intent);
                }
            });


            if (mMovie != null) {

                Uri posterUri = mMovie.buildBackdropUri("w780");
               // Picasso.with(getActivity()).load(posterUri).into(backdropImage);
                Glide.with(MyApplication.getAppContext()).load(posterUri)
                        .placeholder(R.color.colorPrimary)
                        .centerCrop()
                        .crossFade()
                        .into(backdropImage);
                Uri posterUri2 = mMovie.buildPosterUri(getActivity().getString(R.string.api_poster_default_size));
              //  Picasso.with(getActivity()).load(posterUri2).into(posterImage);
                Glide.with(MyApplication.getAppContext()).load(posterUri2)
                        .centerCrop()
                        .crossFade()
                        .into(posterImage);
                mCollapsingToolbarLayout.setTitle(mMovie.getTitle());
                mOverviewView.setText(mMovie.getOverview());

                String movie_date = mMovie.getRelease_date();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    String date = DateUtils.formatDateTime(getActivity(),
                            formatter.parse(movie_date).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                    mDateView.setText(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mVoteAverageView.setText(mMovie.getRating());
            }

            return rootView;
        }

    private void addFavorite() {
        if (mMovie != null) {
            new AsyncTask<Void, Void, Integer>() {

                @Override
                protected Integer doInBackground(Void... params) {
                    return isFavorited(getActivity(), mMovie.getId());
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    if (isFavorited == 1) {
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... params) {
                                return getActivity().getContentResolver().delete(
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                        new String[]{Long.toString(mMovie.getId())}
                                );
                            }

                            @Override
                            protected void onPostExecute(Integer rowsDeleted) {
                                myFab.setBackground(getDrawable(getActivity(), R.drawable.ic_favorite_border));
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(getActivity(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }.execute();
                    }
                    else {
                        new AsyncTask<Void, Void, Uri>() {
                            @Override
                            protected Uri doInBackground(Void... params) {
                                ContentValues values = new ContentValues();

                                values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
                                values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
                                values.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, (mMovie.getPoster_path()));
                                values.put(MovieContract.MovieEntry.COLUMN_BACKDROP_IMAGE, mMovie.getBackdrop_path());
                                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,mMovie.getOverview());
                                values.put(MovieContract.MovieEntry.COLUMN_RATING, mMovie.getRating());
                                values.put(MovieContract.MovieEntry.COLUMN_DATE, mMovie.getRelease_date());

                                return getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                        values);
                            }

                            @Override
                            protected void onPostExecute(Uri returnUri) {
                                myFab.setBackground(getDrawable(getActivity(), R.drawable.ic_favorite_full));
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }.execute();
                    }
                }
            }.execute();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mMovie != null) {
            new FetchTrailersTask().execute(Long.toString(mMovie.getId()));
            new FetchReviewsTask().execute(Long.toString(mMovie.getId()));
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie.getTitle() + " " +
                "http://www.youtube.com/watch?v=" + mTrailer.getKey());
        return shareIntent;
    }



    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for(int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Trailer trailerModel = new Trailer(trailer);
                    results.add(trailerModel);
                }
            }

            return results;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIES_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    mTrailersCardview.setVisibility(View.VISIBLE);
                    if (mTrailerAdapter != null) {
                        mTrailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            mTrailerAdapter.add(trailer);
                        }
                    }

                    mTrailer = trailers.get(0);
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieIntent());
                    }
                }
            }
        }
    }
    public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private ArrayList<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            ArrayList<Review> results = new ArrayList<>();

            for(int i = 0; i < reviewArray.length(); i++) {
                JSONObject trailer = reviewArray.getJSONObject(i);
                Review review1 = new Review(
                        trailer.getString("author"),
                        trailer.getString("content")
                );

                 results.add(review1);
            }

            return results;
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIES_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> review) {

            if (review != null) {
                reviews = review;
                if (reviews.size() > 0) {
                    Review review1;
                    Review.setVisibility(View.VISIBLE);
                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x < reviews.size(); x++) {
                        review1 = reviews.get(x);
                        sb.append(review1.getAuthor().toUpperCase()+ "\n"+ "\n"+ "\n");
                        sb.append(review1.getContent()+ "\n");
                        sb.append("-----------------------"+ "\n"+ "\n");
                    }
                    reviewContent.setText(sb.toString());
                    if (getActivity() instanceof MainActivity) {
                        TextView selectMovie = (TextView) getActivity().findViewById(R.id.selectMovie);
                        selectMovie.setVisibility(View.GONE);
                    }
                }
            }
            }
        }

    public static int isFavorited(Context context, long id) {
        Cursor cursor = MyApplication.getAppContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,   // projection
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", // selection
                new String[] { Long.toString(id) },   // selectionArgs
                null    // sort order
        );
        if(cursor != null) {
            int numRows = cursor.getCount();
            cursor.close();
            return numRows;
        }else
            return 0;

    }
    public static DetailsActivityFragment newInstance(Movie movie) {
        DetailsActivityFragment fragment = new DetailsActivityFragment();
        Bundle args = new Bundle();
        args.putBundle(DetailsActivityFragment.DETAIL_MOVIE,movie.toBundle());
        fragment.setArguments(args);
        return fragment;
    }
    public static DetailsActivityFragment newInstance() {
        DetailsActivityFragment fragment = new DetailsActivityFragment();

        return fragment;
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getDrawable(Context context, int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return MyApplication.getAppContext().getResources().getDrawable(resource, null);
        }

        return MyApplication.getAppContext().getResources().getDrawable(resource);
    }



}



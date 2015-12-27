package com.example.hassan.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hassan.popularmovies.adapter.ImageAdapter;
import com.example.hassan.popularmovies.data.MovieContract;
import com.example.hassan.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class MainActivityFragment extends Fragment {



    private ArrayList<Movie> mMovies = null;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_IMAGE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DATE
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_IMAGE = 3;
    public static final int COL_IMAGE2 = 4;
    public static final int COL_OVERVIEW = 5;
    public static final int COL_RATING = 6;
    public static final int COL_DATE = 7;

    private boolean twoPaneMode;

    private static String mSortBy ;
    public static final int PAGEMAX = 100;
    private boolean isLoading = false;
    private int mPagesLoaded = 0;
    private ProgressBar loading;
    private ImageAdapter mImageAdapter;
    public MainActivityFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mSortBy =getArguments().getString(MainActivity.FRAGMENT_ID);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        if (isTwoPaneLayoutUsed()) {
            twoPaneMode = true;
            Log.d("TEST", "TWO POANE TASDFES");

        }
        mImageAdapter = new ImageAdapter(getActivity(),twoPaneMode,getActivity().getFragmentManager());
        loading = (ProgressBar) view.findViewById(R.id.loading);

        initGrid(view);

        startLoading();

        return view;
    }

    public class FetchAsyncTask extends AsyncTask<Integer, Void, Collection<Movie>> {

        public  final String LOG_TAG = FetchAsyncTask.class.getSimpleName();

        @Override
        protected Collection<Movie> doInBackground(Integer... params) {
            if (params.length == 0) {
                return null;
            }

            int page = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String responseJsonStr = null;

            try {
                final String API_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String API_PARAM_PAGE = "page";
                final String SORT_BY_PARAM = "sort_by";
                final String API_PARAM_KEY = "api_key";


                Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()

                        .appendQueryParameter(SORT_BY_PARAM, mSortBy)
                        .appendQueryParameter(API_PARAM_PAGE, String.valueOf(page))
                        .appendQueryParameter(API_PARAM_KEY, BuildConfig.MOVIES_DB_API_KEY)
                        .build();


                Log.d(LOG_TAG, "QUERY URI: " + builtUri.toString());
                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb api, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                responseJsonStr = buffer.toString();

            } catch (Exception ex) {
                Log.e(LOG_TAG, "Error", ex);
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
                return fetchMoviesFromJson(responseJsonStr);
            } catch (JSONException ex) {
                Log.d(LOG_TAG, "Can't parse JSON: " + responseJsonStr, ex);
                return null;
            }
        }

        private Collection<Movie> fetchMoviesFromJson(String jsonStr) throws JSONException {
            final String KEY_MOVIES = "results";

            JSONObject json  = new JSONObject(jsonStr);
            JSONArray movies = json.getJSONArray(KEY_MOVIES);
            ArrayList<Movie> result = new ArrayList<Movie>();

            for (int i = 0; i < movies.length(); i++) {
                result.add(Movie.fromJson(movies.getJSONObject(i)));
            }

            return result;
        }

        @Override
        protected void onPostExecute(Collection<Movie> xs) {
            if (xs == null) {
                TextView noInternet = (TextView)getActivity().findViewById(R.id.noInternet);
                noInternet.setVisibility(View.VISIBLE);

                stopLoading();
                return;
            }

            mPagesLoaded++;

            stopLoading();

            mImageAdapter.addAll(xs);
        }

    }

    private void startLoading() {
        if (isLoading) {
            return;
        }

        if (mPagesLoaded >= PAGEMAX) {
            return;
        }

        isLoading = true;

        if (loading != null) {
            loading.setVisibility(View.VISIBLE);
        }
        if(mSortBy.equalsIgnoreCase("favorite"))
            new FetchFavoriteMoviesTask(getActivity()).execute();
        else
           new FetchAsyncTask().execute(mPagesLoaded + 1);
    }

    private void stopLoading() {
        if (!isLoading) {
            return;
        }

        isLoading = false;

        if (loading != null) {
            loading.setVisibility(View.GONE);
        }
    }



    private void initGrid(View view) {
        GridView gridview = (GridView) view.findViewById(R.id.grid_view);

        if (gridview == null) {
            return;
        }

        gridview.setAdapter(mImageAdapter);




        gridview.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int lastInScreen = firstVisibleItem + visibleItemCount;
                        if (lastInScreen == totalItemCount) {
                            startLoading();
                        }
                    }
                }

        );
    }
    public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        private Context mContext;

        public FetchFavoriteMoviesTask(Context context) {
            mContext = context;

        }

        private List<Movie> getFavoriteMoviesDataFromCursor(Cursor cursor) {
            List<Movie> results = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie(cursor);
                    results.add(movie);
                } while (cursor.moveToNext());
                cursor.close();
            }
            return results;
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {
            Cursor cursor = mContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
            return getFavoriteMoviesDataFromCursor(cursor);
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            loading.setVisibility(View.GONE);
            if (movies != null) {
                if (mImageAdapter != null) {
                    mImageAdapter.addAll(movies);
                }else {
                    mMovies = new ArrayList<>();
                    mMovies.addAll(movies);
                }
                if (movies.size()==0) {
                    TextView noFavorite = (TextView) getActivity().findViewById(R.id.noFavorite);
                    noFavorite.setVisibility(View.VISIBLE);
                }

            }

        }
    }
    private boolean isTwoPaneLayoutUsed() {
        return getActivity().findViewById(R.id.movies_details_container) != null;
    }
}


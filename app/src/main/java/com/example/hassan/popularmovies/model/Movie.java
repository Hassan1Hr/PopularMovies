package com.example.hassan.popularmovies.model;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.hassan.popularmovies.MainActivityFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie  {
    public static final String EXTRA_MOVIE = "com.example.hassan.popularmovies.EXTRA_MOVIE";
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_POSTER_PATH = "poster_path";
    public static final String KEY_BACKDROP_PATH = "backdrop_path";
    public static final String KEY_VOTE_AVERAGE = "vote_average";
    public static final String KEY_VOTE_COUNT = "vote_count";
    public static final String KEY_RELEASE_DATE = "release_date";

    public final long id;
    public final String title;
    public final String overview;
    public final String poster_path;
    public final String backdrop_path;
    public final double vote_average;
    public final long vote_count;
    public final String release_date;

    public Movie(long id,
                 String title, String overview, String poster_path,String backdrop_path,
                 double vote_average, long vote_count, String release_date) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.poster_path = poster_path;
        this.backdrop_path = backdrop_path;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
        this.release_date = release_date;
    }

    public Movie(Bundle bundle) {
        this(
                bundle.getLong(KEY_ID),
                bundle.getString(KEY_TITLE),
                bundle.getString(KEY_OVERVIEW),
                bundle.getString(KEY_POSTER_PATH),
                bundle.getString(KEY_BACKDROP_PATH),
                bundle.getDouble(KEY_VOTE_AVERAGE),
                bundle.getLong(KEY_VOTE_COUNT),
                bundle.getString(KEY_RELEASE_DATE)
        );
    }

    public String getRating() {
        return "" + vote_average + " / 10";
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putLong(KEY_ID, id);
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_OVERVIEW, overview);
        bundle.putString(KEY_POSTER_PATH, poster_path);
        bundle.putString(KEY_BACKDROP_PATH,backdrop_path);
        bundle.putDouble(KEY_VOTE_AVERAGE, vote_average);
        bundle.putLong(KEY_VOTE_COUNT, vote_count);
        bundle.putString(KEY_RELEASE_DATE, release_date);


        return bundle;
    }


    public static Movie fromJson(JSONObject jsonObject) throws JSONException {
        return new Movie(
                jsonObject.getLong(KEY_ID),
                jsonObject.getString(KEY_TITLE),
                jsonObject.getString(KEY_OVERVIEW),
                jsonObject.getString(KEY_POSTER_PATH),
                jsonObject.getString(KEY_BACKDROP_PATH),
                jsonObject.getDouble(KEY_VOTE_AVERAGE),
                jsonObject.getLong(KEY_VOTE_COUNT),
                jsonObject.getString(KEY_RELEASE_DATE)
        );
    }
    public Movie(Cursor cursor) {

        this.id = cursor.getInt(MainActivityFragment.COL_MOVIE_ID);
        this.title = cursor.getString(MainActivityFragment.COL_TITLE);
        this.poster_path = cursor.getString(MainActivityFragment.COL_IMAGE);
        this.backdrop_path = cursor.getString(MainActivityFragment.COL_IMAGE2);
        this.overview = cursor.getString(MainActivityFragment.COL_OVERVIEW);
        this.vote_count = cursor.getInt(MainActivityFragment.COL_RATING);
        this.vote_average = cursor.getInt(MainActivityFragment.COL_RATING);
        this.release_date = cursor.getString(MainActivityFragment.COL_DATE);
    }
    public Uri buildPosterUri(String size) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(poster_path)
                .build();

        return builtUri;
    }
    public Uri buildBackdropUri(String size) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(backdrop_path)
                .build();

        return builtUri;
    }
    public String getRelease_date() {
        return release_date;
    }

    public long getVote_count() {
        return vote_count;
    }

    public double getVote_average() {
        return vote_average;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }


}
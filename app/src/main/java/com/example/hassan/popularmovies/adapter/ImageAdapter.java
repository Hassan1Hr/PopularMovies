package com.example.hassan.popularmovies.adapter;


import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hassan.popularmovies.Base.MyApplication;
import com.example.hassan.popularmovies.DetailsActivity;
import com.example.hassan.popularmovies.DetailsActivityFragment;
import com.example.hassan.popularmovies.MainActivity;
import com.example.hassan.popularmovies.R;
import com.example.hassan.popularmovies.data.MovieContract;
import com.example.hassan.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;



public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    Toast mToast;
    private final ArrayList<Movie> mMovies;
    private final LayoutInflater mInflater;
    private boolean twoPaneMode;
    Callback call;
    FragmentManager mFragment;



    public ImageAdapter(Context c,boolean paneMode,FragmentManager fragment) {
        mContext = c;
        mFragment = fragment;
       call = new Callback() {
           @Override
           public void onItemSelected(Movie movie) {

           }
       };
        mMovies = new ArrayList<>();
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        twoPaneMode = paneMode;
    }
    public interface Callback {
        void onItemSelected(Movie movie);


    }
    public void addAll(Collection<Movie> xs) {
        mMovies.addAll(xs);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public Movie getItem(int position) {
        if (position < 0 || position >= mMovies.size()) {
            return null;
        }
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        Movie movie = getItem(position);
        if (movie == null) {
            return -1L;
        }

        return movie.id;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Movie movie = getItem(position);
        View view = convertView;
        final ViewHolder viewHolder;

        if (view == null) {
            view = mInflater.inflate(R.layout.item_movie, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder) view.getTag();
        viewHolder.position = position;
        Uri posterUri = movie.buildPosterUri(mContext.getString(R.string.api_poster_default_size));
        Glide.with(MyApplication.getAppContext()).load(posterUri).into(viewHolder.imageView);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return isFavorited(mContext, movie.getId());
            }

            @Override
            protected void onPostExecute(Integer isFavorited) {
                viewHolder.favoriteImage.setBackground(isFavorited == 1 ?
                        getDrawable(mContext, R.drawable.ic_favorite_full) :
                        getDrawable(mContext, R.drawable.ic_favorite_border));
            }
        }.execute();
        viewHolder.favoriteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFavorite(movie, viewHolder);
            }
        });



        Picasso.with(mContext).load(posterUri).into(
         new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                Palette.from(bitmap).generate( new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {

                        int bgColor = palette.getVibrantColor(mContext.getResources().getColor(android.R.color.black));
                        viewHolder.gridlayout.setBackgroundColor(bgColor);
                    }
                });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }


        });



        viewHolder.titleView.setText(movie.getTitle());
        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getDrawable(Context context, int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(resource, null);
        }

        return context.getResources().getDrawable(resource);
    }
    public class ViewHolder implements View.OnClickListener {

        public final ImageView imageView;
        public final TextView titleView;
        public final LinearLayout gridlayout;
        public final com.pkmmte.view.CircularImageView favoriteImage;
        public int position;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.gridImage);
            titleView = (TextView) view.findViewById(R.id.gridTitle);
            gridlayout = (LinearLayout) view.findViewById(R.id.movie_item_footer);
            favoriteImage = (com.pkmmte.view.CircularImageView) view.findViewById(R.id.movie_item_btn_favorite);

            imageView.setOnClickListener(this);
            titleView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
               if(twoPaneMode) {


                   DetailsActivityFragment detailsActivityFragment =  DetailsActivityFragment.newInstance((mMovies.get(position)));
                   mFragment.beginTransaction().replace(R.id.movies_details_container, detailsActivityFragment).commit();

               } else {
                   Intent intent = new Intent(mContext, DetailsActivity.class);
                   intent.putExtra(Movie.EXTRA_MOVIE, mMovies.get(position).toBundle());
                   mContext.startActivity(intent);
               }

        }

    }

    public static int isFavorited(Context context, long id) {
        Cursor cursor = context.getContentResolver().query(
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
    private void addFavorite(final Movie mMovie,final ViewHolder viewHolder) {
        if (mMovie != null) {
            // check if movie is in favorites or not
            new AsyncTask<Void, Void, Integer>() {

                @Override
                protected Integer doInBackground(Void... params) {
                    return isFavorited(mContext, mMovie.getId());
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    // if it is in favorites
                    if (isFavorited == 1) {
                        // delete from favorites
                        new AsyncTask<Void, Void, Integer>() {
                            @Override
                            protected Integer doInBackground(Void... params) {
                                return mContext.getContentResolver().delete(
                                        MovieContract.MovieEntry.CONTENT_URI,
                                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                        new String[]{Long.toString(mMovie.getId())}
                                );
                            }

                            @Override
                            protected void onPostExecute(Integer rowsDeleted) {
                                viewHolder.favoriteImage.setBackground(getDrawable(mContext,R.drawable.ic_favorite_border));
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(mContext, mContext.getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }.execute();
                    }
                    // if it is not in favorites
                    else {
                        // add to favorites
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

                                return mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                        values);
                            }

                            @Override
                            protected void onPostExecute(Uri returnUri) {
                                viewHolder.favoriteImage.setBackground(getDrawable(mContext, R.drawable.ic_favorite_full));
                                if (mToast != null) {
                                    mToast.cancel();
                                }
                                mToast = Toast.makeText(mContext, mContext.getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }.execute();
                    }
                }
            }.execute();
        }
    }

}

package com.thomaskioko.moviemaniac.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.thomaskioko.moviemaniac.MovieManiacApplication;
import com.thomaskioko.moviemaniac.R;
import com.thomaskioko.moviemaniac.model.Result;
import com.thomaskioko.moviemaniac.ui.MovieDetailActivity;
import com.thomaskioko.moviemaniac.ui.MovieListActivity;
import com.thomaskioko.moviemaniac.util.ApplicationConstants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    @Bind(R.id.layout_movie_title)
    RelativeLayout mRelativeLayout;
    @Bind(R.id.movie_detail_year)
    TextView mMovieYear;
    @Bind(R.id.movie_detail_pg)
    TextView mMoviePg;
    @Bind(R.id.movie_detail_plot)
    TextView mMoviePlot;
    @Bind(R.id.movie_detail_thumbnail)
    ImageView mThumbnail;
    private Result mMovieResult;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovieResult = MovieManiacApplication.getResult();

        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            if (mMovieResult != null) {
                appBarLayout.setTitle(mMovieResult.getTitle());
                ImageView imageView = (ImageView) activity.findViewById(R.id.ivBigImage);

                String imagePath = ApplicationConstants.TMDB_IMAGE_URL
                        + ApplicationConstants.IMAGE_SIZE_780
                        + mMovieResult.getBackdropPath();


                Glide.with(imageView.getContext())
                        .load(imagePath)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(imageView) {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                                super.onResourceReady(bitmap, anim);
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        if (palette.getDarkVibrantSwatch() != null) {
                                            mRelativeLayout.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());

                                        } else if (palette.getMutedSwatch() != null) {
                                            mRelativeLayout.setBackgroundColor(palette.getMutedSwatch().getRgb());
                                        }
                                    }
                                });
                            }
                        });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        String imagePath = ApplicationConstants.TMDB_IMAGE_URL
                + ApplicationConstants.IMAGE_SIZE_185
                + mMovieResult.getPosterPath();

        Glide.with(getActivity())
                .load(imagePath)
                .asBitmap()
                .centerCrop()
                .into(mThumbnail);

        mMoviePlot.setText(mMovieResult.getOverview());
        mMovieYear.setText(mMovieResult.getReleaseDate());

        return rootView;
    }
}

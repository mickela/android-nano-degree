package com.thomaskioko.moviemaniac.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.thomaskioko.moviemaniac.MovieManiacApplication;
import com.thomaskioko.moviemaniac.R;
import com.thomaskioko.moviemaniac.api.TmdbApiClient;
import com.thomaskioko.moviemaniac.interfaces.MovieCallback;
import com.thomaskioko.moviemaniac.data.tasks.DatabaseAsyncTask;
import com.thomaskioko.moviemaniac.model.Result;
import com.thomaskioko.moviemaniac.model.ReviewResults;
import com.thomaskioko.moviemaniac.model.Reviews;
import com.thomaskioko.moviemaniac.model.VideoResults;
import com.thomaskioko.moviemaniac.model.Videos;
import com.thomaskioko.moviemaniac.ui.MovieDetailActivity;
import com.thomaskioko.moviemaniac.ui.MovieListActivity;
import com.thomaskioko.moviemaniac.ui.adapters.ReviewsRecyclerViewAdapter;
import com.thomaskioko.moviemaniac.ui.adapters.VideosRecyclerViewAdapter;
import com.thomaskioko.moviemaniac.util.ApplicationConstants;
import com.thomaskioko.moviemaniac.util.SharedPreferenceManager;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements MovieCallback {

    @Bind(R.id.layout_movie_title)
    RelativeLayout mRelativeLayout;
    @Bind(R.id.movie_detail_year)
    TextView mMovieYear;
    @Bind(R.id.movie_detail_plot)
    TextView mMoviePlot;
    @Bind(R.id.movie_detail_thumbnail)
    ImageView mThumbnail;
    @Bind(R.id.movie_detail_rating)
    TextView mMovieRating;
    @Bind(R.id.movie_detail_popularity)
    TextView mMoviePopularity;
    @Bind(R.id.movie_detail_votes)
    TextView mMovieVote;
    @Bind(R.id.circularProgressBar)
    CircularProgressBar mCircularProgressBar;
    @Bind(R.id.recyclerview_trailers)
    RecyclerView mRecyclerViewTrailer;
    @Bind(R.id.recyclerview_reviews)
    RecyclerView mRecyclerViewReviews;
    @Bind(R.id.card_view_reviews)
    CardView mReviewsCardView;
    @Bind(R.id.coordinated_layout)
    CoordinatorLayout mCoordinatedLayout;
    @Bind(R.id.toolbar_layout)
    CollapsingToolbarLayout appBarLayout;
    @Bind(R.id.backdrop)
    ImageView imageView;
    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private MovieCallback movieCallback = this;
    private MovieCallback mCallback;
    private Result mMovieResult;
    private TmdbApiClient mTmdbApiClient;
    private SharedPreferenceManager sharedPreferenceManager;
    private ArrayList<VideoResults> mVideoResults = new ArrayList<>();
    private ArrayList<ReviewResults> mReviewsResults = new ArrayList<>();
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        sharedPreferenceManager = new SharedPreferenceManager(getActivity());
        mTmdbApiClient = MovieManiacApplication.getTmdbApiClient();

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle mBundle = getArguments();
            if (mBundle != null) mMovieResult = mBundle.getParcelable("data");
            else mMovieResult = getActivity().getIntent().getParcelableExtra("data");

            loadMovieData();
        } else {
            mMovieResult = savedInstanceState.getParcelable(ApplicationConstants.KEY_MOVIE_OBJECT);
        }

        new DatabaseAsyncTask(this, getActivity(), null).execute(ApplicationConstants.TASK_IS_FAVORITE, String.valueOf(mMovieResult.getId()));

        initViews();

        return rootView;
    }

    /**
     * Helper method to initialise views.
     */
    private void initViews() {
        //Image path
        String imagePath = ApplicationConstants.TMDB_IMAGE_URL
                + ApplicationConstants.IMAGE_SIZE_185
                + mMovieResult.getPosterPath();

        Glide.with(
                getActivity())
                .load(imagePath)
                .asBitmap()
                .centerCrop()
                .into(mThumbnail
                );

        float rating = mMovieResult.getVoteAverage().floatValue() * 10;
        float popularity = mMovieResult.getPopularity().intValue();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
                .withLocale(Locale.getDefault());

        //Get the year from the release date.
        LocalDate date = formatter.parseLocalDate(mMovieResult.getReleaseDate());

        mMoviePlot.setText(mMovieResult.getOverview());
        mMovieYear.setText(String.valueOf(date.getYear()));
        mMovieRating.setText(String.valueOf(mMovieResult.getVoteAverage()));
        mMoviePopularity.setText(String.valueOf(popularity));
        mMovieVote.setText(String.valueOf(mMovieResult.getVoteCount()));
        mCircularProgressBar.setProgressWithAnimation(rating);
        mReviewsCardView.setVisibility(View.GONE);

        appBarLayout.setTitle(mMovieResult.getTitle());

        //Backdrop Image URL
        String imagePathBackDrop = ApplicationConstants.TMDB_IMAGE_URL
                + ApplicationConstants.IMAGE_SIZE_780
                + mMovieResult.getBackdropPath();


        Glide.with(imageView.getContext())
                .load(imagePathBackDrop)
                .asBitmap()
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, final GlideAnimation glideAnimation) {
                        super.onResourceReady(bitmap, glideAnimation);
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {

                                if (palette.getDarkVibrantSwatch() != null) {
                                    mRelativeLayout.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
                                    mCircularProgressBar.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());

                                } else if (palette.getMutedSwatch() != null) {
                                    mRelativeLayout.setBackgroundColor(palette.getMutedSwatch().getRgb());
                                    mCircularProgressBar.setBackgroundColor(palette.getMutedSwatch().getRgb());
                                }
                                if (palette.getLightVibrantSwatch() != null) {
                                    mCircularProgressBar.setColor(palette.getLightVibrantSwatch().getRgb());
                                } else if (palette.getLightMutedSwatch() != null) {
                                    mCircularProgressBar.setColor(palette.getLightMutedSwatch().getRgb());
                                }
                            }
                        });
                    }
                });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        assert mRecyclerViewTrailer != null;
        mRecyclerViewTrailer.setLayoutManager(linearLayoutManager);
        LinearLayoutManager _linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        assert mRecyclerViewReviews != null;
        mRecyclerViewReviews.setLayoutManager(_linearLayoutManager);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MovieListActivity.isTwoPane) mCallback = (MovieCallback) getActivity();
    }

    /**
     * Method that calls {@link #getMovieVideos()} and {@link #getMovieReviews()} to load videos and
     * and reviews
     */
    private void loadMovieData() {
        getMovieReviews();
        getMovieVideos();
    }

    /**
     * Method to get movie videos
     */
    private void getMovieVideos() {

        Call<Videos> topRatedList = mTmdbApiClient.movieInterface().getMovieVideos(mMovieResult.getId());
        topRatedList.enqueue(new Callback<Videos>() {
            @Override
            public void onResponse(Call<Videos> call, Response<Videos> response) {

                if (response.body().getVideoResults().size() < 0) {
                    mReviewsCardView.setVisibility(View.GONE);
                } else {
                    mReviewsCardView.setVisibility(View.VISIBLE);
                    for (VideoResults videoResults : response.body().getVideoResults()) {
                        mVideoResults.add(videoResults);
                        mRecyclerViewTrailer.setAdapter(
                                new VideosRecyclerViewAdapter(getActivity(), mVideoResults)
                        );
                    }
                }
            }

            @Override
            public void onFailure(Call<Videos> call, Throwable t) {
                Log.e(LOG_TAG, "@getMovieVideos Error Message:: " + t.getLocalizedMessage());
            }
        });
    }

    /**
     * Method to fetch movie reviews.
     */
    private void getMovieReviews() {

        Call<Reviews> topRatedList = mTmdbApiClient.movieInterface().getMovieReviews(mMovieResult.getId());
        topRatedList.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(Call<Reviews> call, Response<Reviews> response) {

                for (ReviewResults reviewResults : response.body().getReviewResultsList()) {
                    mReviewsResults.add(reviewResults);
                    mRecyclerViewReviews.setAdapter(
                            new ReviewsRecyclerViewAdapter(getActivity(), mReviewsResults)
                    );
                }
            }

            @Override
            public void onFailure(Call<Reviews> call, Throwable t) {
                Log.e(LOG_TAG, "@getMovieReviews Error Message:: " + t.getLocalizedMessage());
            }
        });
    }

    @OnClick({R.id.fab})
    void onClickViews(View view) {
        switch (view.getId()) {
            case R.id.fab:
                new DatabaseAsyncTask(movieCallback, getActivity(), mMovieResult).execute(ApplicationConstants.TASK_ADD_FAVORITE, null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ApplicationConstants.KEY_MOVIE_OBJECT, mMovieResult);
    }

    @Override
    public void CallbackRequest(String requestType, String bundleData) {
        switch (requestType) {
            case ApplicationConstants.CALLBACK_ADD_FAVORITE:
                if (bundleData.equals("true")) {
                    if (sharedPreferenceManager.getMovieType().
                            equals(ApplicationConstants.PREF_MOVIE_LIST_FAVORITES) && MovieDetailActivity.twoPane) {
                        mCallback.CallbackRequest(ApplicationConstants.CALLBACK_REFRESH_FAVORITES, "");
                    }
                    Snackbar snackbar = Snackbar
                            .make(mCoordinatedLayout, mMovieResult.getTitle() + " has been added to Favorites", Snackbar.LENGTH_LONG);

                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    snackbar.show();
                } else if (bundleData.equals("false")) {
                    Snackbar snackbar = Snackbar
                            .make(mCoordinatedLayout, mMovieResult.getTitle()
                                    + " could not be added to Favorites", Snackbar.LENGTH_LONG);

                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                    snackbar.show();
                }
                break;
        }
    }

    @Override
    public void CallbackRequest(String requestType, ArrayList<Result> resultArrayList) {

    }

}

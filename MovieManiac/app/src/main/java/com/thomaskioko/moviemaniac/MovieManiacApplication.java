package com.thomaskioko.moviemaniac;

import android.app.Application;

import com.thomaskioko.moviemaniac.api.TmdbApiClient;
import com.thomaskioko.moviemaniac.model.Result;

/**
 * Application class.
 *
 * @author Thomas Kioko
 */
public class MovieManiacApplication extends Application {

    private static TmdbApiClient tmdbApiClient = new TmdbApiClient();
    public static Result result;

    @Override
    public void onCreate() {
        super.onCreate();
        tmdbApiClient.setIsDebug(true);
    }

    /**
     * @return {@link TmdbApiClient} instance
     */
    public static TmdbApiClient getTmdbApiClient() {
        return tmdbApiClient;
    }

    /**
     * @return
     */
    public static Result getResult() {
        return result;
    }
}
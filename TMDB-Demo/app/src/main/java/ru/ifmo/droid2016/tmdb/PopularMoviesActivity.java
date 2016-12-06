package ru.ifmo.droid2016.tmdb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Экран, отображающий список популярных фильмов из The Movie DB.
 */
public class PopularMoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
    }

}

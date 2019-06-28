package com.example.flixster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.flixster.models.Config;
import com.example.flixster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MovieListActivity extends AppCompatActivity {

    // constants
    // base URL for the API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // parameter name of the API key
    public final static String API_KEY_PARAM = "api_key";
    // tag for all logging form this activity
    public static final String TAG = "MovieListActivity";


    // instance fields
    AsyncHttpClient client;
    // list of now playing movies
    ArrayList<Movie>  movies;
    // the recycler view
    RecyclerView rvMovies;
    //adapter wired to recycler view
    MovieAdapter adapter;
    // image config
    Config config;



    private void getNowPlaying() {
        // create url
        String url = API_BASE_URL + "/movie/now_playing";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //API KEY is always required
        // execute GET request , expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    // iterate through result set and create movie list
                    for ( int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify adapter that row was added
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    // notify that movies list is loaded in console
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("failed to parse now playing movies",e, true );
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError( "failed to get data from now playing endpoint", throwable, true);
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize the client
        client = new AsyncHttpClient();
        // initialize list of movies
        movies = new ArrayList<>();
        //initialize  the adapter - movies array cannot be reinitialized at this point
        adapter = new MovieAdapter(movies);

        // resolve the recycler view and connect a layout manager and the adapter
        rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        // get config on app creation
        getConfiguration();
    }

    //get configuration from API
    private void getConfiguration() {
        //create url
        String url = API_BASE_URL + "/configuration";
        //set request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //API KEY is always required
        // execute GET request , expecting JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG, String.format("Loaded Configuration wih imageBaseUrl %s and posterSize %s",
                            config.getImageBaseUrl(),
                            config.getPosterSize()));
                    // pass config object to adapter
                    adapter.setConfig(config);
                    // get now playing list
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuration", throwable, true );
            }
        });
    }

    // handles silent errors that don't show up , logs and alerts user
    private void logError(String message , Throwable error, boolean alertUser) {
        // always logs error
        Log.e( TAG , message, error);
        //alert user
        if (alertUser) {
            // show a long toast with error msg
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

    }
}

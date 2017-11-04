package com.pratiksymz.android.newsguardian;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class NewsMainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<NewsClass>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = NewsMainActivity.class.getName();

    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    /**
     * Loader for background thread
     */
    private static LoaderManager loaderManager;

    /**
     * Initial Query which will be combined with the user's input
     */
    private static final String GUARDIAN_API_QUERY = "https://content.guardianapis.com/search?";

    /**
     * Adapter for the List
     */
    private static NewsAdapter newsAdapter;

    /**
     * TextView displaying messages to the user
     */
    private TextView messageTextView;

    /**
     * Spinner displaying progress to user
     */
    private ProgressBar progressBar;

    /**
     * Refresh Layout
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Network info to check for internet connection
     */
    private NetworkInfo networkInfo;

    /**
     * Link to the SharedPreferences class
     */
    private SharedPreferences sharedPreferences;

    private Toolbar searchToolbar;
    private MaterialSearchView searchView;

    // Get the user search query
    String searchQuery;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);

        // Lookup the recyclerView in activity layout
        RecyclerView newsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Create a new adapter that takes an empty list of news articles as input
        newsAdapter = new NewsAdapter(this, new ArrayList<NewsClass>());
        // Set the adapter on the {@link RecyclerView}
        // so the list can be populated in the user interface
        newsRecyclerView.setAdapter(newsAdapter);
        // Set layout manager to position the items
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        searchToolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                // Do nothing
                return;
            }

            @Override
            public void onSearchViewClosed() {
                if (searchQuery.isEmpty()) {

                }
            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    searchQuery = query;
                } else {
                    searchQuery = "";
                }
                // Clear the ListView as a new query will be kicked off
                newsAdapter.clearAll();

                // Hide the empty state text view as the loading indicator will be displayed
                messageTextView.setVisibility(View.GONE);

                // Show the loading indicator while new data is being fetched
                progressBar.setVisibility(View.VISIBLE);

                // Restart the loader to query the Guardian again as the query settings have been updated
                getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsMainActivity.this);

                searchView.setSubmitOnClick(true);
                // Initialize Loader and News Adapter
                initializeLoaderAndAdapter();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Obtain a reference to the SharedPreferences file for this app
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // And register to be notified of preference changes
        // So we know when the user has adjusted the query settings
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // TextView to show the user information about that data retrieval status
        messageTextView = (TextView) findViewById(R.id.message_textView);
        // ProgressBar to indicate the user about the loading of information
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Initialize SwipeRefreshLayout and assign Listener
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecyclerView();
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Show the message for Fetching Data
            messageTextView.setText(getString(R.string.message_fetching));

            // Initialize Loader and News Adapter
            initializeLoaderAndAdapter();

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            progressBar.setVisibility(View.GONE);

            // Update empty state with no connection error message
            messageTextView.setText(getString(R.string.message_no_internet));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_search_query_key)) ||
                key.equals(getString(R.string.settings_order_by_list_key))) {
            // Clear the ListView as a new query will be kicked off
            newsAdapter.clearAll();

            // Hide the empty state text view as the loading indicator will be displayed
            messageTextView.setVisibility(View.GONE);

            // Show the loading indicator while new data is being fetched
            progressBar.setVisibility(View.VISIBLE);

            // Restart the loader to query the Guardian again as the query settings have been updated
            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<NewsClass>> onCreateLoader(int i, Bundle bundle) {
        // Get order by preference
        String orderBy = sharedPreferences.getString(getString(R.string
                .settings_order_by_list_key), getString(R.string.settings_order_by_list_default));

        // Build the Uri based on the preferences
        Uri baseIri = Uri.parse(GUARDIAN_API_QUERY);
        Uri.Builder uriBuilder = baseIri.buildUpon();

        uriBuilder.appendQueryParameter("q", searchQuery);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", "test");
        Log.v("NewsMainActivity", "Uri: " + uriBuilder);

        // Create a new loader with the supplied Url
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsClass>> loader, List<NewsClass> newsArticles) {
        // If there is a valid list of {@link News}s, then add them to the adapter's
        // data set. This will trigger the RecyclerView to update.
        if (newsArticles != null && !newsArticles.isEmpty()) {
            newsAdapter.addAll(newsArticles);
            // Hide loading indicator because the data has been loaded
            progressBar.setVisibility(View.GONE);
            // Hide message text
            messageTextView.setText("");
        } else {
            // Hide loading indicator because the data has been loaded
            progressBar.setVisibility(View.GONE);
            // Set message text to display "No articles found!"
            messageTextView.setText(getString(R.string.message_no_articles));
            // Clear the adapter of previous news data
            newsAdapter.clearAll();
        }
        Log.v("NewsMainActivity", "Loader completed operation!");
    }

    @Override
    public void onLoaderReset(Loader<List<NewsClass>> loader) {
        // Loader reset, so we can clear out our existing data.
        newsAdapter.clearAll();
    }

    public void initializeLoaderAndAdapter() {
        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);

        // Lookup the recyclerView in activity layout
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Create adapter passing the data
        newsAdapter = new NewsAdapter(this, new ArrayList<NewsClass>());
        // Attach the adapter to the recyclerView to populate items
        recyclerView.setAdapter(newsAdapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void refreshRecyclerView() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();
        Log.v("NewsMainActivity", "networkInfo: " + networkInfo);

        if (networkInfo != null && networkInfo.isConnected()) {
            // Show message text
            messageTextView.setText(getString(R.string.message_refreshing));
            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);

            // Check if newsAdapter is not null (which will happen if on launch there was no
            // connection)
            if (newsAdapter != null) {
                // Clear the adapter
                newsAdapter.clearAll();
            }

            // Restarting the Loader
            if (loaderManager != null) {
                // Restart Loader
                loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
                // Inform SwipeRefreshLayout that loading is complete so it can hide its progress bar
                swipeRefreshLayout.setRefreshing(false);
            } else {
                initializeLoaderAndAdapter();
                // Inform SwipeRefreshLayout that loading is complete so it can hide its progress bar
                swipeRefreshLayout.setRefreshing(false);
            }

        } else {
            // Hide progressBar
            progressBar.setVisibility(View.GONE);

            // Check if newsAdapter is not null (which will happen if on launch there was no
            // connection)
            if (newsAdapter != null) {
                // Clear the adapter
                newsAdapter.clearAll();
            }

            // Display error
            messageTextView.setText(getString(R.string.message_no_internet));
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        searchView.setMenuItem(search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
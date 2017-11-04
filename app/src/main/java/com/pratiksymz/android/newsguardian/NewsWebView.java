package com.pratiksymz.android.newsguardian;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.pratiksymz.android.newsguardian.databinding.NewsWebViewBinding;

/**
 * This class covers content related to WebView.
 * When you click on a news list item, you are connected to the web view by the intent.
 */
public class NewsWebView extends AppCompatActivity {
    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();
    private NewsWebViewBinding binding;
    private ConnectivityManager connectivityManager;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.news_web_view);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            String url = getIntent().getStringExtra("url");
            String title = getIntent().getStringExtra("title");

            // Set the WebView with the appropriate values
            binding.webViewStory.getSettings().setLoadWithOverviewMode(true);
            binding.webViewStory.setWebViewClient(new MyWebViewClient());
            binding.webViewStory.loadUrl(url);

            binding.toolbarStory.toolbarTitle.setText(title);
            binding.toolbarStory.toolbarUp.setOnClickListener(new UpClickListener());
        } else {
            messageTextView = (TextView) findViewById(R.id.web_message_textView);
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            binding.storyLoadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            messageTextView.setText(getString(R.string.message_no_internet));
            binding.toolbarStory.toolbarUp.setOnClickListener(new UpClickListener());
        }

    }

    /**
     * This listener handles what happens when the back button is pressed.
     */
    private class UpClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (binding.webViewStory.canGoBack()) {
                binding.webViewStory.goBack();
            } else {
                NewsWebView.super.onBackPressed();
            }
        }
    }

    /**
     * This class has been overridden for the following reasons:
     * 1. Change the state of the progressbar and the webview depending on the loading state
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            binding.storyLoadingIndicator.setVisibility(View.GONE);
            binding.webViewStory.setVisibility(View.VISIBLE);
        }
    }
}

package org.supla.android;

/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class CreateAccountActivity extends NavigationActivity {

    private WebView mWebView;
    private ProgressBar progress;

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {

            view.loadUrl("javascript:(function() {$(document.body).addClass('in-app-register');})()");

            progress.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);
        mWebView = (WebView) findViewById(R.id.webBrowser);
        mWebView.getSettings().setJavaScriptEnabled(true);

        progress = (ProgressBar)findViewById(R.id.caProgressBar);
        progress.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));

        showMenuBar();
    }

    @Override
    public void onBackPressed() {
        showCfg(this);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progress.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(webViewClient);
        mWebView.setVisibility(View.GONE);
        mWebView.loadUrl("https://cloud.supla.org/auth/create");
    }
}

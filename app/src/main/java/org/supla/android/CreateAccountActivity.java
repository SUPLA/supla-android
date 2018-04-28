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
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CreateAccountActivity extends NavigationActivity {

    private WebView mWebView;

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {

            view.loadUrl("javascript:(function() { " +
                    "document.getElementsByClassName('login-footer')[0].style.display='none'; })()");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);
        mWebView = (WebView) findViewById(R.id.webBrowser);
        mWebView.getSettings().setJavaScriptEnabled(true);
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
        mWebView.setWebViewClient(webViewClient);
        mWebView.loadUrl("https://cloud.supla.org/auth/create");
    }
}

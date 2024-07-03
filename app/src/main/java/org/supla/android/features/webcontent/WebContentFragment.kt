package org.supla.android.features.webcontent
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import androidx.core.content.res.ResourcesCompat
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.ViewEvent
import org.supla.android.databinding.FragmentWebContentBinding

abstract class WebContentFragment<S : WebContentViewState, E : ViewEvent> : BaseFragment<S, E>(R.layout.fragment_web_content) {

  protected abstract val url: String

  abstract override val viewModel: WebContentViewModel<S, E>
  protected val binding by viewBinding(FragmentWebContentBinding::bind)

  private val client = object : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
      viewModel.urlLoaded(url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
      return viewModel.allowRequest(request).not()
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
      if (request != null && errorResponse != null) {
        viewModel.handleError(request.url.toString(), errorResponse.statusCode)
      }
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
      if (failingUrl != null) {
        viewModel.handleError(failingUrl, errorCode)
      }
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      webBrowser.settings.javaScriptEnabled = true
      webBrowser.settings.domStorageEnabled = true
      webBrowser.clearCache(true)

      caProgressBar.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.progressbar, null)

      binding.webBrowser.webViewClient = client
      binding.webBrowser.loadUrl(url)
    }
  }

  @CallSuper
  override fun handleViewState(state: S) {
    if (state.loading) {
      binding.caProgressBar.visibility = View.VISIBLE
      binding.webBrowser.visibility = View.GONE
    } else {
      binding.caProgressBar.visibility = View.GONE
      binding.webBrowser.visibility = View.VISIBLE
    }
  }
}

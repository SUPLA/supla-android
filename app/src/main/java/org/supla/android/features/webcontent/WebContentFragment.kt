package org.supla.android.features.webcontent

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import androidx.core.content.res.ResourcesCompat
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.databinding.FragmentWebContentBinding

abstract class WebContentFragment<S : ViewState, E : ViewEvent> : BaseFragment<S, E>(R.layout.fragment_web_content) {

  protected abstract val url: String

  protected val binding by viewBinding(FragmentWebContentBinding::bind)

  private val client = object : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
      (getViewModel() as WebContentViewModel).urlLoaded(url)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      webBrowser.settings.javaScriptEnabled = true
      webBrowser.settings.domStorageEnabled = true

      caProgressBar.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.progressbar, null)
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

  override fun onStart() {
    super.onStart()

    binding.webBrowser.webViewClient = client
    binding.webBrowser.loadUrl(url)
  }
}

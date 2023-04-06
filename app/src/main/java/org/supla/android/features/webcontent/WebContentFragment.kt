package org.supla.android.features.webcontent

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentWebContentBinding

private const val ARG_URL = "arg_url"

@AndroidEntryPoint
class WebContentFragment : BaseFragment<WebContentViewState, WebContentViewEvent>(R.layout.fragment_web_content) {

  private val viewModel: WebContentViewModel by viewModels()
  override fun getViewModel(): BaseViewModel<WebContentViewState, WebContentViewEvent> = viewModel

  private val binding by viewBinding(FragmentWebContentBinding::bind)

  private val url: String by lazy { arguments!!.getString(ARG_URL)!! }

  private val client = object: WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
      viewModel.urlLoaded(url)
      binding.caProgressBar.visibility = View.GONE
      binding.webBrowser.visibility = View.VISIBLE
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

  override fun handleEvents(event: WebContentViewEvent) = when(event) {
    WebContentViewEvent.LoadRegistrationScript -> {
      binding.webBrowser.loadUrl("javascript:(function() { document.body.classList.add('in-app-register'); })()")
    }
  }

  override fun handleViewState(state: WebContentViewState) {
  }

  override fun onStart() {
    super.onStart()
    binding.caProgressBar.visibility = View.VISIBLE
    binding.webBrowser.visibility = View.GONE

    binding.webBrowser.webViewClient = client
    binding.webBrowser.loadUrl(url)
  }

  companion object {
    fun bundle(url: String) = bundleOf(ARG_URL to url)
  }
}
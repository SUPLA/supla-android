package org.supla.android.features.createaccountweb

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.features.webcontent.WebContentFragment

@AndroidEntryPoint
class CreateAccountWebFragment : WebContentFragment<CreateAccountWebViewState, CreateAccountWebViewEvent>() {

  override val url: String by lazy { getString(R.string.create_url) }
  override val viewModel: CreateAccountWebViewModel by viewModels()

  override fun handleEvents(event: CreateAccountWebViewEvent) = when (event) {
    CreateAccountWebViewEvent.LoadRegistrationScript -> {
      binding.webBrowser.loadUrl("javascript:(function() { document.body.classList.add('in-app-register'); })()")
    }
  }
}

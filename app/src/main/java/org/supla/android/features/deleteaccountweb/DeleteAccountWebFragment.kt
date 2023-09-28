package org.supla.android.features.deleteaccountweb

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BackHandler
import org.supla.android.features.webcontent.WebContentFragment
import org.supla.android.navigator.CfgActivityNavigator
import java.io.Serializable
import javax.inject.Inject

private const val ARG_DESTINATION = "ARG_DESTINATION"
private const val ARG_SERVER_ADDRESS = "ARG_SERVER_ADDRESS"

@AndroidEntryPoint
class DeleteAccountWebFragment : WebContentFragment<DeleteAccountWebViewState, DeleteAccountWebViewEvent>(), BackHandler {

  override val url: String by lazy { viewModel.getUrl(getString(R.string.delete_url), serverAddress) }
  override val viewModel: DeleteAccountWebViewModel by viewModels()

  private val serverAddress: String? by lazy { arguments?.getString(ARG_SERVER_ADDRESS) }

  @Suppress("DEPRECATION") // Not deprecated method can't be accessed from API 21
  private val destination: EndDestination? by lazy { arguments?.getSerializable(ARG_DESTINATION) as? EndDestination }

  @Inject lateinit var navigator: CfgActivityNavigator

  override fun handleEvents(event: DeleteAccountWebViewEvent) {
    when (event) {
      DeleteAccountWebViewEvent.CloseClicked -> {
        when (destination) {
          EndDestination.RESTART -> navigator.restartAppStack()
          EndDestination.RECONNECT -> navigator.navigateToStatus()
          else -> navigator.back()
        }
      }
    }
  }

  companion object {
    fun bundle(serverAddress: String?, destination: EndDestination): Bundle = bundleOf(
      ARG_SERVER_ADDRESS to serverAddress,
      ARG_DESTINATION to destination
    )
  }

  enum class EndDestination : Serializable {
    RESTART, RECONNECT, CLOSE
  }

  override fun onBackPressed() = when (destination) {
    EndDestination.RESTART -> {
      navigator.restartAppStack()
      true
    }
    EndDestination.RECONNECT -> {
      navigator.navigateToStatus()
      true
    }
    EndDestination.CLOSE -> false
    null -> false
  }
}

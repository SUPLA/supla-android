package org.supla.android.features.deleteaccountweb
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

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.features.webcontent.WebContentFragment
import org.supla.android.navigator.CfgActivityNavigator
import java.io.Serializable
import javax.inject.Inject

private const val ARG_DESTINATION = "ARG_DESTINATION"
private const val ARG_SERVER_ADDRESS = "ARG_SERVER_ADDRESS"

@AndroidEntryPoint
class DeleteAccountWebFragment : WebContentFragment<DeleteAccountWebViewState, DeleteAccountWebViewEvent>() {

  override val url: String by lazy { viewModel.getUrl(getString(R.string.delete_url), serverAddress) }
  override val viewModel: DeleteAccountWebViewModel by viewModels()

  private val serverAddress: String? by lazy { arguments?.getString(ARG_SERVER_ADDRESS) }

  @Suppress("DEPRECATION") // Not deprecated method can't be accessed from API 21
  private val destination: EndDestination? by lazy { arguments?.getSerializable(ARG_DESTINATION) as? EndDestination }

  @Inject
  lateinit var navigator: CfgActivityNavigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val onBackPressedCallback = object : OnBackPressedCallback(destination == EndDestination.RESTART) {
      override fun handleOnBackPressed() {
        navigator.restartAppStack()
      }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  override fun handleEvents(event: DeleteAccountWebViewEvent) {
    when (event) {
      DeleteAccountWebViewEvent.CloseClicked -> {
        when (destination) {
          EndDestination.RESTART -> navigator.restartAppStack()
          else -> navigator.navigateToMain()
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
    RESTART, CLOSE
  }
}

package org.supla.android.main
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

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.fragment.app.FragmentActivity
import androidx.navigation3.runtime.rememberNavBackStack
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.supla.android.core.networking.suplaclient.SuplaClientState
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.features.nfc.NfcHost
import org.supla.android.main.view.MainComposeNavHost
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity(), NfcHost {
  // FragmentActivity used because of hosting legacy fragment
  // When last fragment replaced with compose can be changed to ComponentActivity

  @Inject
  lateinit var navigator: MainComposeNavigator

  @Inject
  lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @Inject
  lateinit var suplaSchedulers: SuplaSchedulers

  @Inject
  lateinit var applicationPreferences: ApplicationPreferences

  private val viewModel: MainViewModel by viewModels()

  private val disposables = CompositeDisposable()
  private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    navigator.setActivity(this@MainActivity)

    enableEdgeToEdge()

    setContent {
      MainContent()
    }
  }

  override fun onStart() {
    super.onStart()
    handleState()
  }

  override fun onStop() {
    disposables.clear()
    super.onStop()
  }

  override fun enableNfcReader(intentHandler: (Tag) -> Unit) {
    Timber.d("Enable NFC dispatch")

    val adapter = nfcAdapter ?: return
    val flags = NfcAdapter.FLAG_READER_NFC_A or
      NfcAdapter.FLAG_READER_NFC_B or
      NfcAdapter.FLAG_READER_NFC_F or
      NfcAdapter.FLAG_READER_NFC_V

    adapter.enableReaderMode(this, { intentHandler(it) }, flags, null)
  }

  override fun disableNfcReader() {
    Timber.d("Disable NFC dispatch")
    nfcAdapter?.disableReaderMode(this)
  }

  @Composable
  private fun MainContent() {
    val backStack = rememberNavBackStack(MainRoute.Status)

    DisposableEffect(backStack) {
      navigator.bind(backStack)
      onDispose { navigator.unbind(backStack) }
    }

    CompositionLocalProvider(
      LocalApplicationPreferences provides applicationPreferences,
      LocalNavigator provides navigator
    ) {
      SuplaTheme {
        MainComposeNavHost(
          backStack = backStack,
          navigator = navigator,
          notificationState = viewModel.eventNotificationState,
          onEventRemoved = viewModel::hideNotification
        )
      }
    }
  }

  private fun handleState() {
    disposables.add(
      suplaClientStateHolder.state()
        .observeOn(suplaSchedulers.ui)
        .subscribeBy(
          onNext = {
            Timber.d("Got state $it")
            when (it) {
              SuplaClientState.FirstProfileCreation -> navigator.navigateToNewProfile()
              is SuplaClientState.Connecting,
              SuplaClientState.Initialization,
              is SuplaClientState.Disconnecting,
              SuplaClientState.Locking,
              is SuplaClientState.Finished -> navigator.navigateToStatus()
              SuplaClientState.Locked -> navigator.navigateTo(MainRoute.UnlockApp(UnlockAction.AuthorizeApplication))
              else -> {}
            }
          }
        )
    )
  }
}

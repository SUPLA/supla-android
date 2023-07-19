package org.supla.android.features.thermostatdetail.thermostatgeneral

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentThermostatGeneralBinding
import org.supla.android.features.thermostatdetail.thermostatgeneral.ui.ThermostatDetail
import org.supla.android.lib.SuplaClientMsg

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class ThermostatGeneralFragment : BaseFragment<ThermostatGeneralViewState, ThermostatGeneralViewEvent>(
  R.layout.fragment_thermostat_general
) {

  override val viewModel: ThermostatGeneralViewModel by viewModels()
  private val binding by viewBinding(FragmentThermostatGeneralBinding::bind)

  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      SuplaTheme {
        ThermostatDetail(viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()

    viewModel.loadChannel(remoteId = remoteId)
  }

  override fun handleEvents(event: ThermostatGeneralViewEvent) {
  }

  override fun handleViewState(state: ThermostatGeneralViewState) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == remoteId) {
          viewModel.loadChannel(remoteId)
        } else {
          viewModel.loadTemperature(message.channelId)
        }
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(
      ARG_REMOTE_ID to remoteId
    )
  }
}

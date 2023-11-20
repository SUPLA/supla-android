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
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.standarddetail.ItemBundle
import org.supla.android.features.thermostatdetail.thermostatgeneral.ui.ThermostatDetail
import org.supla.android.lib.SuplaClientMsg

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class ThermostatGeneralFragment : BaseFragment<ThermostatGeneralViewState, ThermostatGeneralViewEvent>(
  R.layout.fragment_compose
) {

  override val viewModel: ThermostatGeneralViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.observeData(item.remoteId, item.deviceId)

    binding.composeContent.setContent {
      SuplaTheme {
        ThermostatDetail(viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(item.remoteId, item.deviceId)
  }

  override fun handleEvents(event: ThermostatGeneralViewEvent) {
  }

  override fun handleViewState(state: ThermostatGeneralViewState) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (message.channelId == item.remoteId) {
          viewModel.triggerDataLoad(item.remoteId)
        } else {
          viewModel.loadTemperature(message.channelId)
        }
      }
    }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}

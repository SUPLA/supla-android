package org.supla.android.features.legacydetail

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.*
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentLegacyDetailBinding
import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.listview.DetailLayout
import org.supla.android.ui.animations.DEFAULT_ANIMATION_DURATION
import org.supla.android.usecases.details.DetailType
import java.io.Serializable

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_DETAIL_TYPE = "ARG_DETAIL_TYPE"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class LegacyDetailFragment : BaseFragment<LegacyDetailViewState, LegacyDetailViewEvent>(R.layout.fragment_legacy_detail) {

  private val viewModel: LegacyDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentLegacyDetailBinding::bind)

  private val detailType: DetailType by lazy { arguments!!.getSerializable(ARG_DETAIL_TYPE) as DetailType }
  private val itemType: ItemType by lazy { arguments!!.getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }

  override fun getViewModel(): BaseViewModel<LegacyDetailViewState, LegacyDetailViewEvent> = viewModel

  private lateinit var detailView: DetailLayout

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.loadData(remoteId, itemType)
  }

  override fun onDestroyView() {
    if (this::detailView.isInitialized) {
      detailView.onDetailHide()
    }

    super.onDestroyView()
  }

  override fun handleEvents(event: LegacyDetailViewEvent) {
    when (event) {
      is LegacyDetailViewEvent.LoadDetailView -> setupDetailView(event.channelBase)
    }
  }

  override fun handleViewState(state: LegacyDetailViewState) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (this::detailView.isInitialized.not()) {
          return // view will not handle updates because it's not initialized
        }
        if (itemType == ItemType.CHANNEL && message.channelId != remoteId) {
          return // message for another channel
        }
        if (itemType == ItemType.GROUP && message.channelGroupId != remoteId) {
          return // message for another group
        }
        detailView.OnChannelDataChanged()
      }
    }
  }

  private fun setupDetailView(channelBase: ChannelBase) {
    setToolbarTitle(channelBase.getNotEmptyCaption(context))

    detailView = getDetailView().apply { setData(channelBase) }
    binding.legacyDetailContent.addView(
      detailView,
      ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    )
    detailView.visibility = View.VISIBLE
    Handler(Looper.getMainLooper()).postDelayed({ detailView.onDetailShow() }, 2*DEFAULT_ANIMATION_DURATION)
  }

  private fun getDetailView(): DetailLayout = when (detailType) {
    DetailType.RGBW -> ChannelDetailRGBW(context)
    DetailType.RS -> ChannelDetailRS(context)
    DetailType.IC -> ChannelDetailIC(context)
    DetailType.EM -> ChannelDetailEM(context)
    DetailType.TEMPERATURE -> ChannelDetailTemperature(context)
    DetailType.TEMPERATURE_HUMIDITY -> ChannelDetailTempHumidity(context)
    DetailType.THERMOSTAT_HP -> ChannelDetailThermostatHP(context)
    DetailType.DIGIGLASS -> ChannelDetailDigiglass(context)
  }

  companion object {
    fun bundle(remoteId: Int, detailType: DetailType, itemType: ItemType) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_DETAIL_TYPE to detailType,
      ARG_ITEM_TYPE to itemType
    )
  }

  enum class ItemType : Serializable {
    CHANNEL, GROUP
  }
}

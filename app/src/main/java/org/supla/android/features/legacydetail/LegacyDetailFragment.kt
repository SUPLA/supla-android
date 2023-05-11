package org.supla.android.features.legacydetail

import android.os.Bundle
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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.databinding.FragmentLegacyDetailBinding
import org.supla.android.db.ChannelBase
import org.supla.android.listview.ChannelListView
import org.supla.android.listview.DetailLayout
import org.supla.android.usecases.details.DetailType
import java.io.Serializable
import javax.inject.Inject

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

  @Inject
  lateinit var channelRepository: ChannelRepository

  private lateinit var detailView: DetailLayout

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val channelBase: ChannelBase = when (itemType) {
      ItemType.CHANNEL -> channelRepository.getChannel(remoteId)
      ItemType.GROUP -> channelRepository.getChannelGroup(remoteId)
    }
    detailView = getDetailView().apply { setData(channelBase) }
    binding.legacyDetailContent.addView(
      detailView,
      ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    )
    detailView.visibility = View.VISIBLE

    setToolbarTitle(channelBase.getNotEmptyCaption(context))
  }

  override fun onStart() {
    super.onStart()
    detailView.onDetailShow()
  }

  override fun onStop() {
    detailView.onDetailHide()
    super.onStop()
  }

  override fun handleEvents(event: LegacyDetailViewEvent) {
  }

  override fun handleViewState(state: LegacyDetailViewState) {
  }

  private fun getDetailView(channelListView: ChannelListView? = null): DetailLayout = when (detailType) {
    DetailType.RGBW -> ChannelDetailRGBW(context, channelListView)
    DetailType.RS -> ChannelDetailRS(context, channelListView)
    DetailType.IC -> ChannelDetailIC(context, channelListView)
    DetailType.EM -> ChannelDetailEM(context, channelListView)
    DetailType.TEMPERATURE -> ChannelDetailTemperature(context, channelListView)
    DetailType.TEMPERATURE_HUMIDITY -> ChannelDetailTempHumidity(context, channelListView)
    DetailType.THERMOSTAT_HP -> ChannelDetailThermostatHP(context, channelListView)
    DetailType.DIGIGLASS -> ChannelDetailDigiglass(context, channelListView)
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

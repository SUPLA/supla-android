package org.supla.android.usecases.group

import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.db.ChannelGroup
import org.supla.android.usecases.group.activepercentage.DimmerAndRgbGroupActivePercentage
import org.supla.android.usecases.group.activepercentage.DimmerGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.FacadeBlindGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.OpenedClosedGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.ProjectorScreenGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.RgbGroupActivePercentageProvider
import org.supla.android.usecases.group.activepercentage.ShadingSystemGroupActivePercentageProvider
import org.supla.android.usecases.group.totalvalue.GroupTotalValue.Companion.parse
import org.supla.android.usecases.group.totalvalue.GroupValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetGroupActivePercentageUseCase @Inject constructor() {

  private val providers: List<GroupActivePercentageProvider> = listOf(
    OpenedClosedGroupActivePercentageProvider,
    ShadingSystemGroupActivePercentageProvider,
    FacadeBlindGroupActivePercentageProvider,
    ProjectorScreenGroupActivePercentageProvider,
    DimmerGroupActivePercentageProvider,
    RgbGroupActivePercentageProvider,
    DimmerAndRgbGroupActivePercentage
  )

  operator fun invoke(channelGroupEntity: ChannelGroupEntity, valueIndex: Int = 0): Int =
    getActivePercentage(channelGroupEntity.function, channelGroupEntity.totalValue, valueIndex)

  operator fun invoke(channelGroup: ChannelGroup, valueIndex: Int = 0) =
    getActivePercentage(channelGroup.func, channelGroup.totalValue, valueIndex)

  private fun getActivePercentage(function: Int, totalValue: String?, valueIndex: Int): Int {
    val values = parse(function, totalValue)
    if (values.isEmpty()) {
      return 0
    }

    providers.forEach {
      if (it.handleFunction(function)) {
        return it.getActivePercentage(valueIndex, values)
      }
    }

    return 0
  }
}

interface GroupActivePercentageProvider {
  fun handleFunction(function: Int): Boolean

  fun getActivePercentage(valueIndex: Int, values: List<GroupValue>): Int
}

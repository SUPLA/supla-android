package org.supla.android.features.details.electricitymeterdetail.history

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.stringProvider
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.features.details.detailbase.history.ui.DataSetContainer
import org.supla.android.images.ImageId
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.spinner.TextSpinner
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString

enum class IntroductionPage {
  FIRST_FOR_SINGLE_PHASE, FIRST_FOR_MULTI_PHASE, SECOND
}

@Composable
fun ElectricityMeterHistoryIntroduction(
  pages: List<IntroductionPage>,
  onClose: () -> Unit
) {
  val pagerState = rememberPagerState { pages.count() }
  val coroutineScope = rememberCoroutineScope()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(colorResource(R.color.info_scrim))
      .clickable(enabled = false) { }
  ) {
    HorizontalPager(state = pagerState) { page ->
      Box(modifier = Modifier.fillMaxSize()) {
        when (pages[page]) {
          IntroductionPage.FIRST_FOR_SINGLE_PHASE -> FirstPageSinglePhase { coroutineScope.launch { pagerState.scrollToPage(1) } }
          IntroductionPage.FIRST_FOR_MULTI_PHASE -> FirstPageMultiplePhase { coroutineScope.launch { pagerState.scrollToPage(1) } }
          IntroductionPage.SECOND -> SecondPage(onClick = onClose)
        }
      }
    }

    Row(
      Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .padding(bottom = Distance.default),
      horizontalArrangement = Arrangement.Center
    ) {
      repeat(pagerState.pageCount) { iteration ->
        val color = if (pagerState.currentPage == iteration) Color.LightGray else Color.DarkGray
        Box(
          modifier = Modifier
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
            .size(16.dp)
        )
      }
    }
  }
}

context(BoxScope)
@Composable
private fun FirstPageMultiplePhase(onClick: () -> Unit) {
  IconButton(
    icon = R.drawable.ic_arrow_right,
    onClick = onClick,
    tint = MaterialTheme.colorScheme.onPrimary,
    modifier = Modifier.align(Alignment.TopEnd)
  )
  DataSetContainer(
    data = mockDataSetContainerDataMultiplePhases(),
    showHistory = true,
    modifier = Modifier
      .padding(top = Distance.tiny, start = Distance.tiny)
      .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(size = dimensionResource(R.dimen.radius_default))
      )
      .padding(vertical = Distance.tiny)
  )
  InfoText(
    textId = R.string.details_em_info_data_set_multiple_phase,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 100.dp, start = Distance.default, end = Distance.default)
  )
}

context(BoxScope)
@Composable
private fun FirstPageSinglePhase(onClick: () -> Unit) {
  IconButton(
    icon = R.drawable.ic_arrow_right,
    onClick = onClick,
    tint = MaterialTheme.colorScheme.onPrimary,
    modifier = Modifier.align(Alignment.TopEnd)
  )
  DataSetContainer(
    data = mockDataSetContainerDataSinglePhases(),
    showHistory = true,
    modifier = Modifier
      .padding(top = Distance.tiny, start = Distance.tiny)
      .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(size = dimensionResource(R.dimen.radius_default))
      )
      .padding(vertical = Distance.tiny)
  )
  InfoText(
    textId = R.string.details_em_info_data_set_single_phase,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 100.dp, start = Distance.default, end = Distance.default)
  )
}

context(BoxScope)
@Composable
private fun SecondPage(onClick: () -> Unit) {
  IconButton(
    icon = R.drawable.ic_close,
    onClick = onClick,
    tint = MaterialTheme.colorScheme.onPrimary,
    modifier = Modifier.align(Alignment.TopEnd)
  )

  Column(modifier = Modifier.padding(start = Distance.tiny, top = 88.dp)) {
    TextSpinner(
      options = SingleSelectionList(ChartRange.LAST_WEEK, listOf(ChartRange.LAST_WEEK), R.string.history_range_label),
      onOptionSelected = { },
      active = false,
      modifier = Modifier
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(size = dimensionResource(R.dimen.radius_default))
        )
        .padding(all = Distance.tiny)
    )
    InfoText(
      textId = R.string.details_em_info_range,
      modifier = Modifier
        .width(150.dp)
        .padding(vertical = Distance.small)
    )
  }

  Column(
    modifier = Modifier
      .align(Alignment.TopEnd)
      .padding(end = Distance.tiny, top = 88.dp),
    horizontalAlignment = Alignment.End
  ) {
    TextSpinner(
      options = SingleSelectionList(ChartDataAggregation.MINUTES, listOf(ChartDataAggregation.MINUTES), R.string.history_data_type),
      onOptionSelected = { },
      active = false,
      modifier = Modifier
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(size = dimensionResource(R.dimen.radius_default))
        )
        .padding(all = Distance.tiny)
    )
    InfoText(
      textId = R.string.details_em_info_data_type,
      modifier = Modifier
        .width(150.dp)
        .padding(vertical = Distance.small)
    )
  }
}

@Composable
private fun InfoText(@StringRes textId: Int, modifier: Modifier) =
  Text(
    stringResource(textId),
    color = MaterialTheme.colorScheme.onPrimary,
    style = MaterialTheme.typography.bodyMedium,
    modifier = modifier,
    textAlign = TextAlign.Center
  )

private fun mockDataSetContainerDataMultiplePhases(): ChannelChartSets =
  ChannelChartSets(
    remoteId = 0,
    function = SuplaFunction.NONE,
    name = stringProvider { it.getString(R.string.channel_caption_electricitymeter) },
    aggregation = ChartDataAggregation.DAYS,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.ELECTRICITY,
        label = HistoryDataSet.Label.Multiple(
          listOf(
            HistoryDataSet.LabelData(ImageId(R.drawable.fnc_electricity_meter), "78,08", R.color.phase1),
            HistoryDataSet.LabelData(null, "73,45", R.color.phase2),
            HistoryDataSet.LabelData(null, "28,66", R.color.phase3)
          )
        ),
        valueFormatter = ListElectricityMeterValueFormatter()
      )
    ),
    typeName = LocalizedString.WithResource(R.string.channel_caption_electricitymeter)
  )

private fun mockDataSetContainerDataSinglePhases(): ChannelChartSets =
  ChannelChartSets(
    remoteId = 0,
    function = SuplaFunction.NONE,
    name = stringProvider { it.getString(R.string.channel_caption_electricitymeter) },
    aggregation = ChartDataAggregation.DAYS,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.ELECTRICITY,
        label = HistoryDataSet.Label.Multiple(
          listOf(
            HistoryDataSet.LabelData(ImageId(R.drawable.fnc_electricity_meter), "78,08", R.color.phase1)
          )
        ),
        valueFormatter = ListElectricityMeterValueFormatter()
      )
    ),
    typeName = LocalizedString.WithResource(R.string.channel_caption_electricitymeter)
  )

@Composable
@Preview
private fun PreviewFirstMulti() {
  SuplaTheme {
    ElectricityMeterHistoryIntroduction(listOf(IntroductionPage.FIRST_FOR_MULTI_PHASE)) {}
  }
}

@Composable
@Preview
private fun PreviewFirstSingle() {
  SuplaTheme {
    ElectricityMeterHistoryIntroduction(listOf(IntroductionPage.FIRST_FOR_SINGLE_PHASE)) {}
  }
}

@Composable
@Preview(name = "Second")
private fun PreviewSecond() {
  SuplaTheme {
    ElectricityMeterHistoryIntroduction(listOf(IntroductionPage.SECOND)) {}
  }
}

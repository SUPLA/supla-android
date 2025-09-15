package org.supla.android.data.model.chart.datatype

import org.assertj.core.api.Assertions
import org.junit.Test
import org.supla.android.core.ui.stringProvider
import org.supla.android.data.model.chart.AggregatedEntity
import org.supla.android.data.model.chart.AggregatedValue
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.formatters.HumidityValueFormatter
import java.util.Date

class LineChartDataTest {

  @Test
  fun `should calculate max value when min and max is above zero`() {
    // given
    val data = createData(10f, 30f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(30f.times(1.2f))
  }

  @Test
  fun `should calculate max value when min and max is below zero`() {
    // given
    val data = createData(-10f, -30f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(0f)
  }

  @Test
  fun `should calculate max value when min and max is below zero but should calculate place for marker`() {
    // given
    val data = createData(-2f, -30f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(-2f + 30f.times(0.2f))
  }

  @Test
  fun `should calculate max value when min below zero and max above zero`() {
    // given
    val data = createData(-10f, 30f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(30f + 40f.times(.2f))
  }

  @Test
  fun `should calculate max value when min and max below zero and same`() {
    // given
    val data = createData(-10f, -10f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(0f)
  }

  @Test
  fun `should calculate max value when min and max above zero and same`() {
    // given
    val data = createData(10f, 10f)

    // when
    val maxValue = data.getAxisMaxValue { it.leftAxis() }

    // then
    Assertions.assertThat(maxValue).isEqualTo(12f)
  }

  private fun createData(min: Float, max: Float): LineChartData {
    return LineChartData(
      dateRange = DateRange(Date(), Date()),
      chartRange = ChartRange.DAY,
      aggregation = ChartDataAggregation.MINUTES,
      sets = listOf(
        ChannelChartSets(
          remoteId = 1,
          function = SuplaFunction.HUMIDITY,
          name = stringProvider { "" },
          aggregation = ChartDataAggregation.MINUTES,
          dataSets = listOf(
            HistoryDataSet(
              type = ChartEntryType.HUMIDITY_ONLY,
              label = HistoryDataSet.Label.Single(HistoryDataSet.LabelData(123)),
              valueFormatter = HumidityValueFormatter(),
              active = true,
              entities = listOf(
                listOf(
                  AggregatedEntity(0, AggregatedValue.Single(min)),
                  AggregatedEntity(1, AggregatedValue.Single(max))
                )
              )
            )
          )
        )
      )
    )
  }
}

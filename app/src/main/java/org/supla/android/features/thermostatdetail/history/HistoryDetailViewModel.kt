package org.supla.android.features.thermostatdetail.history
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

import android.content.res.Resources
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.Channel
import org.supla.android.extensions.toPx
import org.supla.android.features.thermostatdetail.history.ui.HistoryDetailProxy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailProxy {

  override fun onViewCreated() {
    val mainEntries = mutableListOf<Entry>()
    val mainMaxEntries = mutableListOf<Entry>()
    val mainMinEntries = mutableListOf<Entry>()
    val auxEntries = mutableListOf<Entry>()
    val humidityEntries = mutableListOf<BarEntry>()
    for (i in 0..10) {
      mainEntries.add(Entry(i.toFloat(), i.toFloat()))
      mainMaxEntries.add(Entry(i.toFloat(), (i + 0.25).toFloat()))
      mainMinEntries.add(Entry(i.toFloat(), (i - 0.25).toFloat()))
      auxEntries.add(Entry(i.toFloat(), (i + 1).toFloat()))
      humidityEntries.add(BarEntry(i.toFloat(), 5f + (i % 2)))
    }
    updateState {
      it.copy(
        mainTemperaturesSet = mainEntries,
        mainTemperaturesMaxSet = mainMaxEntries,
        mainTemperaturesMinSet = mainMinEntries,
        auxTemperaturesSet = auxEntries,
        humiditySet = humidityEntries
      )
    }
  }

  fun loadData(remoteId: Int) {
    readChannelWithChildrenUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it) }
      )
      .disposeBySelf()
    updateState { it.copy(remoteId = remoteId) }
  }

  override fun startDownload() {
    currentState().let { state ->
      state.auxThermometer?.let {
        downloadChannelMeasurementsUseCase.invoke(it.remoteId, state.profileId, it.function)
      }
    }
  }

  private fun handleData(channel: ChannelWithChildren) {
    updateState { state ->
      state.copy(
        remoteId = channel.channel.remoteId,
        profileId = channel.channel.profileId,
        mainThermometer = ThermometerData.from(channel.children.firstOrNull { it.relationType.isMainThermometer() }?.channel),
        auxThermometer = ThermometerData.from(channel.children.firstOrNull { it.relationType.isAuxThermometer() }?.channel)
      )
    }
  }
}

sealed class HistoryDetailViewEvent : ViewEvent

data class HistoryDetailViewState(
  val remoteId: Int = 0,
  val profileId: Long = 0,
  val mainThermometer: ThermometerData? = null,
  val auxThermometer: ThermometerData? = null,
  val mainTemperaturesSet: List<Entry> = emptyList(),
  val mainTemperaturesMaxSet: List<Entry> = emptyList(),
  val mainTemperaturesMinSet: List<Entry> = emptyList(),
  val auxTemperaturesSet: List<Entry> = emptyList(),
  val humiditySet: List<BarEntry> = emptyList()
) : ViewState() {
  fun combinedData(resources: Resources): CombinedData {
    val mainLineDataSet = LineDataSet(mainTemperaturesSet, "Test").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.HORIZONTAL_BEZIER
      cubicIntensity = 0.05f
      color = ResourcesCompat.getColor(resources, R.color.primary, null)
      circleColors = listOf(ResourcesCompat.getColor(resources, R.color.gray_light, null))
      circleHoleColor = ResourcesCompat.getColor(resources, R.color.primary, null)
      circleRadius = 1.dp.toPx(resources)
    }

    val mainMaxLineDataSet = LineDataSet(mainTemperaturesMaxSet, "Test").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.HORIZONTAL_BEZIER
      cubicIntensity = 0.05f
      color = ResourcesCompat.getColor(resources, R.color.light_green, null)
      setDrawCircles(false)
    }

    val mainMinLineDataSet = LineDataSet(mainTemperaturesMinSet, "Test").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.HORIZONTAL_BEZIER
      cubicIntensity = 0.05f
      color = ResourcesCompat.getColor(resources, R.color.light_green, null)
      setDrawCircles(false)
    }

    val auxLineDataSet = LineDataSet(auxTemperaturesSet, "Test").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.HORIZONTAL_BEZIER
      cubicIntensity = 0.05f
      color = ResourcesCompat.getColor(resources, R.color.red, null)
      circleColors = listOf(ResourcesCompat.getColor(resources, R.color.gray_light, null))
      circleHoleColor = ResourcesCompat.getColor(resources, R.color.red, null)
      circleRadius = 1.dp.toPx(resources)
    }

//    val humidityBarDataSet = BarDataSet(humiditySet, null).apply {
//      setDrawValues(false)
//      color = ResourcesCompat.getColor(resources, R.color.blue, null)
//    }
    val humidityBarDataSet = LineDataSet(humiditySet, "Test").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.LINEAR
      cubicIntensity = 0.05f
      color = ResourcesCompat.getColor(resources, R.color.blue, null)
      circleColors = listOf(ResourcesCompat.getColor(resources, R.color.gray_light, null))
      circleHoleColor = ResourcesCompat.getColor(resources, R.color.blue, null)
      circleRadius = 1.dp.toPx(resources)
    }

    return CombinedData().apply {
      setData(LineData(listOf(mainLineDataSet, mainMaxLineDataSet, mainMinLineDataSet, auxLineDataSet, humidityBarDataSet)))
    }
  }
}

data class ThermometerData(
  val remoteId: Int,
  val function: Int
) {
  companion object {
    fun from(channel: Channel?) =
      if (channel == null) null
      else {
        ThermometerData(
          remoteId = channel.remoteId,
          function = channel.func
        )
      }
  }
}

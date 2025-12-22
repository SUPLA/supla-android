package org.supla.android.features.details.rgbanddimmer.common
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.networking.suplaclient.DelayedCommandSubject
import org.supla.android.extensions.HsvColor
import org.supla.android.features.details.rgbanddimmer.rgb.RgbDetailModelState
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.client.ExecuteRgbwActionUseCase
import javax.inject.Inject
import javax.inject.Singleton

private const val DELAY_MS = 250L

@Singleton
class DelayedRgbwwActionSubject @Inject constructor(
  private val executeRgbwActionUseCase: ExecuteRgbwActionUseCase,
  schedulers: SuplaSchedulers
) : DelayedCommandSubject<State>(schedulers, DELAY_MS, Mode.SAMPLE) {

  override fun execute(state: State): Completable =
    executeRgbwActionUseCase(
      type = state.type,
      remoteId = state.remoteId,
      color = state.color,
      brightness = state.brightness,
      onOff = false,
      vibrate = false
    )
}

data class State(
  val type: SubjectType,
  val remoteId: Int,
  val color: HsvColor?,
  val brightness: Int?,
  val cct: Int?,
  override val sent: Boolean = false
) : DelayableState {
  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}

val RgbDetailModelState.delayableState: State?
  get() {
    val type = type
    val remoteId = remoteId
    val color = viewState.value.hsv

    return if (type != null && remoteId != null && color != null) {
      State(
        type = type.subjectType,
        remoteId = remoteId,
        color = color,
        brightness = dimmerBrightness,
        cct = dimmerCct
      )
    } else {
      null
    }
  }

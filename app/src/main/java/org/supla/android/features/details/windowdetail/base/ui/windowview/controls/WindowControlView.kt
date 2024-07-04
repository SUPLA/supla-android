package org.supla.android.features.details.windowdetail.base.ui.windowview.controls
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.features.details.windowdetail.base.data.CurtainWindowState
import org.supla.android.features.details.windowdetail.base.data.GarageDoorState
import org.supla.android.features.details.windowdetail.base.data.ProjectorScreenState
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.RoofWindowState
import org.supla.android.features.details.windowdetail.base.data.TerraceAwningState
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.curtain.CurtainWindowView
import org.supla.android.features.details.windowdetail.base.ui.facadeblinds.FacadeBlindsWindowView
import org.supla.android.features.details.windowdetail.base.ui.garagedoor.GarageDoorScreenView
import org.supla.android.features.details.windowdetail.base.ui.projectorscreen.ProjectorScreenView
import org.supla.android.features.details.windowdetail.base.ui.rollershutter.RollerShutterWindowView
import org.supla.android.features.details.windowdetail.base.ui.roofwindow.RoofWindowView
import org.supla.android.features.details.windowdetail.base.ui.terraceawning.TerraceAwningView
import org.supla.android.features.details.windowdetail.base.ui.verticalblinds.VerticalBlindsWindowView

@Composable
fun WindowControlView(
  viewState: WindowViewState,
  windowState: WindowState,
  onAction: (ShadingSystemAction) -> Unit,
  modifier: Modifier = Modifier
) {
  when (windowState) {
    is RoofWindowState ->
      RoofWindowView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(it)) },
        onPositionChanged = { if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(it)) }
      )

    is RollerShutterWindowState ->
      RollerShutterWindowView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(it)) },
        onPositionChanged = { if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(it)) }
      )

    is FacadeBlindWindowState ->
      FacadeBlindsWindowView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltTo(position, tilt)) },
        onPositionChanged = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltSetTo(position, tilt)) }
      )

    is TerraceAwningState ->
      TerraceAwningView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(position)) },
        onPositionChanged = { position -> if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(position)) }
      )

    is ProjectorScreenState ->
      ProjectorScreenView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(position)) },
        onPositionChanged = { position -> if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(position)) }
      )

    is CurtainWindowState ->
      CurtainWindowView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(position)) },
        onPositionChanged = { position -> if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(position)) }
      )

    is VerticalBlindWindowState ->
      VerticalBlindsWindowView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltTo(position, tilt)) },
        onPositionChanged = { tilt, position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveAndTiltSetTo(position, tilt)) }
      )

    is GarageDoorState ->
      GarageDoorScreenView(
        windowState = windowState,
        enabled = viewState.enabled,
        modifier = modifier,
        onPositionChanging = { position -> if (viewState.enabled) onAction(ShadingSystemAction.MoveTo(position)) },
        onPositionChanged = { position -> if (viewState.enabled) onAction(ShadingSystemAction.OpenAt(position)) }
      )
  }
}

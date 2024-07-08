package org.supla.android.features.details.thermostatdetail.general.ui
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

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ConstraintSetScope
import org.supla.android.core.ui.theme.Distance
import org.supla.android.features.details.thermostatdetail.general.ThermostatGeneralViewState

object Constraints {
  const val CONTROL_WHEEL = "control_wheel"
  const val DECREASE_BUTTON = "decrease_button"
  const val INCREASE_BUTTON = "increase_button"
  const val HEATING_ICON = "heating_icon"
  const val HEATING_TEXT = "heating_text"
  const val COOLING_ICON = "cooling_icon"
  const val COOLING_TEXT = "cooling_text"
  const val PUMP_ICON = "pump_icon"
  const val SOURCE_ICON = "source_icon"

  fun build(viewState: ThermostatGeneralViewState, isSmallScreen: Boolean, distance: Distance.Static): ConstraintSet = ConstraintSet {
    val distanceFromCenter = if (viewState.currentPower == null) 94.dp else 129.dp
    val controlWheel = createRefFor(CONTROL_WHEEL)

    constrain(controlWheel) {
      top.linkTo(parent.top, if (isSmallScreen) distance.small else 0.dp)
      start.linkTo(parent.start)
      end.linkTo(parent.end)
    }

    constrainIncreaseDecreaseButtons(viewState, isSmallScreen, controlWheel, distance)
    constrainHeatingIndicator(viewState, distanceFromCenter, controlWheel, distance)
    constrainCoolingIndicator(viewState, distanceFromCenter, controlWheel, distance)
    constrainSwitchesIcons(viewState, isSmallScreen, distanceFromCenter, controlWheel)
  }

  context(ConstraintSetScope)
  private fun constrainIncreaseDecreaseButtons(
    viewState: ThermostatGeneralViewState,
    isSmallScreen: Boolean,
    controlWheel: ConstrainedLayoutReference,
    distance: Distance.Static
  ) {
    if ((!viewState.isOff || viewState.programmedModeActive) && !viewState.isOffline) {
      val decreaseButton = createRefFor(DECREASE_BUTTON)
      val increaseButton = createRefFor(INCREASE_BUTTON)

      if (isSmallScreen) {
        constrain(increaseButton) {
          end.linkTo(parent.end, distance.default)
          bottom.linkTo(decreaseButton.top, distance.default)
        }
        constrain(decreaseButton) {
          end.linkTo(parent.end, distance.default)
          bottom.linkTo(parent.bottom, distance.default)
        }
      } else {
        val horizontalCenterGuideline = createGuidelineFromStart(0.5f)

        constrain(increaseButton) {
          start.linkTo(horizontalCenterGuideline, 25.dp)
          bottom.linkTo(controlWheel.bottom, margin = 35.dp)
        }
        constrain(decreaseButton) {
          end.linkTo(horizontalCenterGuideline, 25.dp)
          bottom.linkTo(controlWheel.bottom, margin = 35.dp)
        }
      }
    }
  }

  context(ConstraintSetScope)
  private fun constrainHeatingIndicator(
    viewState: ThermostatGeneralViewState,
    distanceFromCenter: Dp,
    controlWheel: ConstrainedLayoutReference,
    distance: Distance.Static
  ) {
    if (viewState.showHeatingIndicator) {
      val heatingIcon = createRefFor(HEATING_ICON)
      val heatingText = createRefFor(HEATING_TEXT)

      constrain(heatingIcon) {
        top.linkTo(controlWheel.top)
        bottom.linkTo(controlWheel.bottom, margin = distanceFromCenter)
        start.linkTo(controlWheel.start)
        end.linkTo(controlWheel.end)
      }
      constrain(heatingText) {
        top.linkTo(heatingIcon.bottom, margin = distance.tiny)
        start.linkTo(heatingIcon.start)
        end.linkTo(heatingIcon.end)
      }
    }
  }

  context(ConstraintSetScope)
  private fun constrainCoolingIndicator(
    viewState: ThermostatGeneralViewState,
    distanceFromCenter: Dp,
    controlWheel: ConstrainedLayoutReference,
    distance: Distance.Static
  ) {
    if (viewState.showCoolingIndicator) {
      val coolingIcon = createRefFor(COOLING_ICON)
      val coolingText = createRefFor(COOLING_TEXT)

      constrain(coolingIcon) {
        top.linkTo(controlWheel.top, margin = distanceFromCenter)
        bottom.linkTo(controlWheel.bottom)
        start.linkTo(controlWheel.start)
        end.linkTo(controlWheel.end)
      }
      constrain(coolingText) {
        bottom.linkTo(coolingIcon.top, margin = distance.tiny)
        start.linkTo(coolingIcon.start)
        end.linkTo(coolingIcon.end)
      }
    }
  }

  context(ConstraintSetScope)
  private fun constrainSwitchesIcons(
    viewState: ThermostatGeneralViewState,
    isSmallScreen: Boolean,
    distanceFromCenter: Dp,
    controlWheel: ConstrainedLayoutReference
  ) {
    if (viewState.pumpSwitchIcon != null) {
      val pumpIcon = createRefFor(PUMP_ICON)

      if (isSmallScreen) {
        constrain(pumpIcon) {
          top.linkTo(controlWheel.top, margin = distanceFromCenter)
          bottom.linkTo(controlWheel.bottom)
          start.linkTo(controlWheel.start)
          end.linkTo(controlWheel.end, margin = 90.dp)
        }
      } else {
        val decreaseButton = createRefFor(DECREASE_BUTTON)
        constrain(pumpIcon) {
          top.linkTo(decreaseButton.top)
          bottom.linkTo(decreaseButton.bottom)
          end.linkTo(decreaseButton.start, margin = 24.dp)
        }
      }
    }

    if (viewState.heatOrColdSourceSwitchIcon != null) {
      val sourceIcon = createRefFor(SOURCE_ICON)
      if (isSmallScreen) {
        constrain(sourceIcon) {
          top.linkTo(controlWheel.top, margin = distanceFromCenter)
          bottom.linkTo(controlWheel.bottom)
          start.linkTo(controlWheel.start, margin = 90.dp)
          end.linkTo(controlWheel.end)
        }
      } else {
        val increaseButton = createRefFor(INCREASE_BUTTON)
        constrain(sourceIcon) {
          top.linkTo(increaseButton.top)
          bottom.linkTo(increaseButton.bottom)
          start.linkTo(increaseButton.end, margin = 24.dp)
        }
      }
    }
  }
}

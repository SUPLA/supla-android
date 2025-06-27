package org.supla.android.features.addwizard.configuration
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

import org.supla.android.features.addwizard.model.AddWizardFinalAction

sealed interface EspConfigurationState {
  fun handle(event: EspConfigurationEvent): EspConfigurationState
}

/**
 * Initial state
 */

class Idle(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Start -> {
        espConfigurationController.checkRegistration()
        CheckingRegistration(espConfigurationController)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

/**
 * Processing states
 */

class CheckingRegistration(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.RegistrationEnabled -> {
        espConfigurationController.findEspNetwork()
        NetworkSearch(espConfigurationController)
      }

      EspConfigurationEvent.RegistrationDisabled -> {
        espConfigurationController.authorize()
        Authorizing(espConfigurationController)
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

class Authorizing(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Authorized -> {
        espConfigurationController.activateRegistration()
        ActivatingRegistration(espConfigurationController)
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

class ActivatingRegistration(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.RegistrationActivated -> {
        espConfigurationController.findEspNetwork()
        NetworkSearch(espConfigurationController)
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationEnable)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

class NetworkSearch(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      is EspConfigurationEvent.NetworkFound -> {
        espConfigurationController.connectToNetwork(event.ssid)
        ChangingNetwork(espConfigurationController)
      }

      is EspConfigurationEvent.MultipleNetworksFound -> {
        espConfigurationController.showNetworkSelector(event.ssids, false)
        NetworkSearch(espConfigurationController)
      }

      is EspConfigurationEvent.NetworkScanDisabled -> {
        espConfigurationController.showNetworkSelector(event.cached, true)
        NetworkSearch(espConfigurationController)
      }

      is EspConfigurationEvent.NetworkNotFound -> {
        espConfigurationController.showError(EspConfigurationError.NotFound)
        ConfigurationFailure(espConfigurationController)
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.Scan)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

class ChangingNetwork(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.NetworkConnected -> {
        espConfigurationController.configureEsp()
        ConfiguringEsp(espConfigurationController)
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close, reconnect = true)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back, reconnect = true)
      }

      else -> {
        espConfigurationController.reconnect()
        Reconnecting(espConfigurationController, AddWizardFinalAction.Error(EspConfigurationError.Connect))
      }
    }
}

class ConfiguringEsp(private val espConfigurationController: EspConfigurationController) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.EspConfigured -> {
        espConfigurationController.reconnect()
        Reconnecting(espConfigurationController, AddWizardFinalAction.Success)
      }

      is EspConfigurationEvent.EspConfigurationFailure -> {
        espConfigurationController.reconnect()
        Reconnecting(espConfigurationController, AddWizardFinalAction.Error(event.error))
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Close, reconnect = true)
      }

      EspConfigurationEvent.Back -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController, AddWizardFinalAction.Back, reconnect = true)
      }

      else -> {
        espConfigurationController.reconnect()
        Reconnecting(espConfigurationController, AddWizardFinalAction.Error(EspConfigurationError.ConfigureTimeout))
      }
    }
}

class Reconnecting(
  private val espConfigurationController: EspConfigurationController,
  private val finalAction: AddWizardFinalAction
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Reconnected -> {
        handleFinalAction(finalAction, espConfigurationController)
      }

      EspConfigurationEvent.Cancel,
      EspConfigurationEvent.Back -> {
        // Do nothing, just wait for result
        Reconnecting(espConfigurationController, finalAction)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.Reconnect)
        ConfigurationFailure(espConfigurationController)
      }
    }
}

class Canceling(
  private val espConfigurationController: EspConfigurationController,
  private val finalAction: AddWizardFinalAction,
  private val reconnect: Boolean = false
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Canceled -> {
        if (reconnect) {
          espConfigurationController.reconnect()
          Reconnecting(espConfigurationController, finalAction)
        } else {
          handleFinalAction(finalAction, espConfigurationController)
        }
      }

      else -> Canceling(espConfigurationController, finalAction)
    }
}

/**
 * Final states
 */

data object Canceled : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState {
    throw IllegalStateException("Should not get any event!")
  }
}

class Finished(
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Start -> {
        espConfigurationController.checkRegistration()
        CheckingRegistration(espConfigurationController)
      }

      else -> Finished(espConfigurationController)
    }
}

class ConfigurationFailure(
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Start -> {
        espConfigurationController.checkRegistration()
        CheckingRegistration(espConfigurationController)
      }

      else -> ConfigurationFailure(espConfigurationController)
    }
}

/**
 * Helper function
 */

private fun handleFinalAction(
  finalAction: AddWizardFinalAction,
  espConfigurationController: EspConfigurationController
): EspConfigurationState =
  when (finalAction) {
    AddWizardFinalAction.Close -> {
      espConfigurationController.close()
      Canceled
    }

    is AddWizardFinalAction.Error -> {
      espConfigurationController.showError(finalAction.error)
      ConfigurationFailure(espConfigurationController)
    }

    AddWizardFinalAction.Success -> {
      espConfigurationController.showFinished()
      Finished(espConfigurationController)
    }

    AddWizardFinalAction.Back -> {
      espConfigurationController.back()
      Idle(espConfigurationController)
    }
  }

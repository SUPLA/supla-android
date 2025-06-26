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
        ConfigurationFailure
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
        Canceling(espConfigurationController)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
        ConfigurationFailure
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
        Canceling(espConfigurationController)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
        ConfigurationFailure
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
        Canceling(espConfigurationController)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.RegistrationEnable)
        ConfigurationFailure
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
        ConfigurationFailure
      }

      EspConfigurationEvent.Cancel -> {
        espConfigurationController.cancel()
        Canceling(espConfigurationController)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.Scan)
        ConfigurationFailure
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
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
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
        Canceling(espConfigurationController, AddWizardFinalAction.Close)
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
        when (finalAction) {
          AddWizardFinalAction.Close -> {
            espConfigurationController.close()
            Canceled
          }

          is AddWizardFinalAction.Error -> {
            espConfigurationController.showError(finalAction.error)
            ConfigurationFailure
          }

          AddWizardFinalAction.Success -> {
            espConfigurationController.showFinished()
            Finished
          }
        }
      }

      EspConfigurationEvent.Cancel -> {
        // Do nothing, just wait for result
        Reconnecting(espConfigurationController, finalAction)
      }

      else -> {
        espConfigurationController.showError(EspConfigurationError.Reconnect)
        ConfigurationFailure
      }
    }
}

class Canceling(
  private val espConfigurationController: EspConfigurationController,
  private val reconnectAction: AddWizardFinalAction? = null
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState =
    when (event) {
      EspConfigurationEvent.Canceled -> {
        if (reconnectAction != null) {
          espConfigurationController.reconnect()
          Reconnecting(espConfigurationController, reconnectAction)
        } else {
          espConfigurationController.close()
          Canceled
        }
      }

      else -> Canceling(espConfigurationController, reconnectAction)
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

data object Finished : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState = Finished
}

data object ConfigurationFailure : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent): EspConfigurationState = ConfigurationFailure
}

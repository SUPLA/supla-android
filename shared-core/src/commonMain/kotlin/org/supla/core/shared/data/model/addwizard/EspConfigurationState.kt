package org.supla.core.shared.data.model.addwizard
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

sealed interface EspConfigurationState {
  fun handle(event: EspConfigurationEvent)
}

interface EspConfigurationStateHolder {
  fun setState(state: EspConfigurationState)
}

/**
 * Initial state
 */

class Idle(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Start -> {
        stateHolder.setState(CheckingRegistration(stateHolder, espConfigurationController))
        espConfigurationController.checkRegistration()
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
      }
    }
  }
}

/**
 * Processing states
 */

class CheckingRegistration(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.RegistrationEnabled -> {
        stateHolder.setState(NetworkSearch(stateHolder, espConfigurationController))
        espConfigurationController.findEspNetwork()
      }

      EspConfigurationEvent.RegistrationDisabled -> {
        stateHolder.setState(Authorizing(stateHolder, espConfigurationController))
        espConfigurationController.authorize()
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back))
        espConfigurationController.cancel()
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
      }
    }
  }
}

class Authorizing(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Authorized -> {
        stateHolder.setState(ActivatingRegistration(stateHolder, espConfigurationController))
        espConfigurationController.activateRegistration()
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back))
        espConfigurationController.cancel()
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.RegistrationCheck)
      }
    }
  }
}

class ActivatingRegistration(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.RegistrationActivated -> {
        stateHolder.setState(NetworkSearch(stateHolder, espConfigurationController))
        espConfigurationController.findEspNetwork()
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back))
        espConfigurationController.cancel()
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.RegistrationEnable)
      }
    }
  }
}

class NetworkSearch(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      is EspConfigurationEvent.NetworkFound -> {
        stateHolder.setState(ChangingNetwork(stateHolder, espConfigurationController))
        espConfigurationController.connectToNetwork(event.ssid)
      }

      is EspConfigurationEvent.MultipleNetworksFound -> {
        stateHolder.setState(NetworkSearch(stateHolder, espConfigurationController))
        espConfigurationController.showNetworkSelector(event.ssids, false)
      }

      is EspConfigurationEvent.NetworkScanDisabled -> {
        stateHolder.setState(NetworkSearch(stateHolder, espConfigurationController))
        espConfigurationController.showNetworkSelector(event.cached, true)
      }

      is EspConfigurationEvent.NetworkNotFound -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.NotFound)
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back))
        espConfigurationController.cancel()
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.Scan)
      }
    }
  }
}

class ChangingNetwork(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.NetworkConnected -> {
        stateHolder.setState(ConfiguringEsp(stateHolder, espConfigurationController))
        espConfigurationController.configureEsp()
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close, reconnect = true))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back, reconnect = true))
        espConfigurationController.cancel()
      }

      else -> {
        val finalAction = AddWizardFinalAction.Error(EspConfigurationError.Connect)
        stateHolder.setState(Reconnecting(stateHolder, espConfigurationController, finalAction))
        espConfigurationController.reconnect()
      }
    }
  }
}

class ConfiguringEsp(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.EspConfigured -> {
        stateHolder.setState(Reconnecting(stateHolder, espConfigurationController, AddWizardFinalAction.Success))
        espConfigurationController.reconnect()
      }

      is EspConfigurationEvent.EspConfigurationFailure -> {
        val finalAction = AddWizardFinalAction.Error(event.error)
        stateHolder.setState(Reconnecting(stateHolder, espConfigurationController, finalAction))
        espConfigurationController.reconnect()
      }

      EspConfigurationEvent.Cancel -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Close, reconnect = true))
        espConfigurationController.cancel()
      }

      EspConfigurationEvent.Back -> {
        stateHolder.setState(Canceling(stateHolder, espConfigurationController, AddWizardFinalAction.Back, reconnect = true))
        espConfigurationController.cancel()
      }

      else -> {
        val finalAction = AddWizardFinalAction.Error(EspConfigurationError.ConfigureTimeout)
        stateHolder.setState(Reconnecting(stateHolder, espConfigurationController, finalAction))
        espConfigurationController.reconnect()
      }
    }
  }
}

class Reconnecting(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController,
  private val finalAction: AddWizardFinalAction
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Reconnected -> {
        handleFinalAction(stateHolder, finalAction, espConfigurationController)
      }

      EspConfigurationEvent.Cancel,
      EspConfigurationEvent.Back -> {
        // Do nothing, just wait for result
      }

      else -> {
        stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
        espConfigurationController.showError(EspConfigurationError.Reconnect)
      }
    }
  }
}

class Canceling(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController,
  private val finalAction: AddWizardFinalAction,
  private val reconnect: Boolean = false
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Canceled -> {
        if (reconnect) {
          stateHolder.setState(Reconnecting(stateHolder, espConfigurationController, finalAction))
          espConfigurationController.reconnect()
        } else {
          handleFinalAction(stateHolder, finalAction, espConfigurationController)
        }
      }

      else -> {
        // Do nothing
      }
    }
  }
}

/**
 * Final states
 */

data object Canceled : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    throw IllegalStateException("Should not get any event!")
  }
}

class Finished(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Start -> {
        stateHolder.setState(CheckingRegistration(stateHolder, espConfigurationController))
        espConfigurationController.checkRegistration()
      }

      else -> {
        // Do nothing
      }
    }
  }
}

class ConfigurationFailure(
  private val stateHolder: EspConfigurationStateHolder,
  private val espConfigurationController: EspConfigurationController
) : EspConfigurationState {
  override fun handle(event: EspConfigurationEvent) {
    when (event) {
      EspConfigurationEvent.Start -> {
        stateHolder.setState(CheckingRegistration(stateHolder, espConfigurationController))
        espConfigurationController.checkRegistration()
      }

      else -> {
        // Do nothing
      }
    }
  }
}

/**
 * Helper function
 */

private fun handleFinalAction(
  stateHolder: EspConfigurationStateHolder,
  finalAction: AddWizardFinalAction,
  espConfigurationController: EspConfigurationController
) {
  when (finalAction) {
    AddWizardFinalAction.Close -> {
      stateHolder.setState(Canceled)
      espConfigurationController.close()
    }

    is AddWizardFinalAction.Error -> {
      stateHolder.setState(ConfigurationFailure(stateHolder, espConfigurationController))
      espConfigurationController.showError(finalAction.error)
    }

    AddWizardFinalAction.Success -> {
      stateHolder.setState(Finished(stateHolder, espConfigurationController))
      espConfigurationController.showFinished()
    }

    AddWizardFinalAction.Back -> {
      stateHolder.setState(Idle(stateHolder, espConfigurationController))
      espConfigurationController.back()
    }
  }
}

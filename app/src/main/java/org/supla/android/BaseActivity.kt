package org.supla.android
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

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.supla.android.db.DbHelper
import org.supla.android.extensions.setStatusBarColor
import org.supla.android.lib.SuplaChannelBasicCfg
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMessageHandler.OnSuplaClientMessageListener
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.lib.SuplaConnError
import org.supla.android.lib.SuplaEvent
import org.supla.android.lib.SuplaOAuthToken
import org.supla.android.lib.SuplaRegisterError
import org.supla.android.lib.SuplaRegistrationEnabled
import org.supla.android.lib.SuplaVersionError
import org.supla.android.lib.ZWaveNode
import org.supla.android.lib.ZWaveWakeUpSettings

@SuppressLint("registered")
open class BaseActivity : AppCompatActivity(), OnSuplaClientMessageListener {
  private var dbHelper: DbHelper? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    SuplaApp.getApp().initTypefaceCollection(this)
    setStatusBarColor(R.color.primary_container, false)
  }

  override fun onResume() {
    super.onResume()
    invalidateDbHelper()
  }

  override fun onSuplaClientMessageReceived(msg: SuplaClientMsg) {
    when (msg.type) {
      SuplaClientMsg.onConnecting,
      SuplaClientMsg.onRegistering,
      SuplaClientMsg.onRegistered,
      SuplaClientMsg.onRegisterError,
      SuplaClientMsg.onDisconnected,
      SuplaClientMsg.onConnected,
      SuplaClientMsg.onVersionError -> beforeStatusMsg()
    }

    when (msg.type) {
      SuplaClientMsg.onDataChanged -> onDataChangedMsg(msg.channelId, msg.channelGroupId, msg.isExtendedValue)
      SuplaClientMsg.onConnecting -> onConnectingMsg()
      SuplaClientMsg.onRegistering -> onRegisteringMsg()
      SuplaClientMsg.onRegistered -> onRegisteredMsg()
      SuplaClientMsg.onRegisterError -> onRegisterErrorMsg(msg.registerError)
      SuplaClientMsg.onDisconnected -> onDisconnectedMsg()
      SuplaClientMsg.onConnected -> onConnectedMsg()
      SuplaClientMsg.onVersionError -> onVersionErrorMsg(msg.versionError)
      SuplaClientMsg.onEvent -> onEventMsg(msg.event)
      SuplaClientMsg.onConnError -> onConnErrorMsg(msg.connError)
      SuplaClientMsg.onRegistrationEnabled -> onRegistrationEnabled(msg.registrationEnabled)
      SuplaClientMsg.onOAuthTokenRequestResult -> onOAuthTokenRequestResult(msg.oAuthToken)
      SuplaClientMsg.onCalCfgResult -> onCalCfgResult(msg.channelId, msg.command, msg.result, msg.data)
      SuplaClientMsg.onSuperuserAuthorizationResult -> onSuperuserAuthorizationResult(msg.isSuccess, msg.result)
      SuplaClientMsg.onChannelState -> onChannelState(msg.channelState)
      SuplaClientMsg.onChannelBasicCfg -> onChannelBasicCfg(msg.channelBasicCfg)
      SuplaClientMsg.onChannelFunctionSetResult -> onChannelFunctionSetResult(msg.channelId, msg.func, msg.code)
      SuplaClientMsg.onChannelCaptionSetResult -> onChannelCaptionSetResult(msg.channelId, msg.text, msg.code)
      SuplaClientMsg.onClientsReconnectResult -> onClientsReconnectResult(msg.code)
      SuplaClientMsg.onSetRegistrationEnabledResult -> onSetRegistrationEnabledResult(msg.code)
      SuplaClientMsg.onZWaveResetAndClearResult -> onZWaveResetAndClearResult(msg.result)
      SuplaClientMsg.onZWaveAddNodeResult -> onZWaveAddNodeResult(msg.result, msg.node)
      SuplaClientMsg.onZWaveRemoveNodeResult -> onZWaveRemoveNodeResult(msg.result, msg.nodeId)
      SuplaClientMsg.onZWaveGetNodeListResult -> onZWaveGetNodeListResult(msg.result, msg.node)
      SuplaClientMsg.onZWaveGetAssignedNodeIdResult -> onZWaveGetAssignedNodeIdResult(msg.result, msg.nodeId)
      SuplaClientMsg.onZWaveAssignNodeIdResult -> onZWaveAssignNodeIdResult(msg.result, msg.nodeId)
      SuplaClientMsg.onZWaveWakeUpSettingsReport -> onZWaveWakeUpSettingsReport(msg.result, msg.wakeUpSettings)
      SuplaClientMsg.onZWaveSetWakeUpTimeResult -> onZwaveSetWakeUpTimeResult(msg.result)
      SuplaClientMsg.onCalCfgProgressReport -> onCalCfgProgressReport(msg.channelId, msg.command, msg.progress)
    }
  }

  protected fun registerMessageHandler() {
    SuplaClientMessageHandler.getGlobalInstance().registerMessageListener(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this)
  }

  protected fun beforeStatusMsg() {}

  protected fun onDataChangedMsg(ChannelId: Int, GroupId: Int, extendedValue: Boolean) {}

  protected fun onConnectingMsg() {}

  protected fun onRegisteringMsg() {}

  protected open fun onRegisteredMsg() {}

  protected fun onRegisterErrorMsg(error: SuplaRegisterError?) {}

  protected fun onDisconnectedMsg() {}

  protected fun onConnectedMsg() {}

  protected fun onVersionErrorMsg(error: SuplaVersionError?) {}

  protected open fun onEventMsg(event: SuplaEvent) {}

  protected fun onConnErrorMsg(error: SuplaConnError?) {}

  protected open fun onRegistrationEnabled(registrationEnabled: SuplaRegistrationEnabled?) {}

  protected fun onOAuthTokenRequestResult(token: SuplaOAuthToken?) {}

  protected open fun onCalCfgResult(channelId: Int, command: Int, result: Int, data: ByteArray?) {}

  protected open fun onCalCfgProgressReport(channelId: Int, command: Int, progress: Short) {}

  protected fun onSuperuserAuthorizationResult(success: Boolean, code: Int) {}

  protected fun onChannelState(state: SuplaChannelState?) {}

  protected open fun onChannelBasicCfg(basicCfg: SuplaChannelBasicCfg?) {}

  protected open fun onChannelFunctionSetResult(channelId: Int, func: Int, code: Int) {}

  protected open fun onChannelCaptionSetResult(channelId: Int, caption: String?, code: Int) {}

  protected fun onClientsReconnectResult(code: Int) {}

  protected open fun onSetRegistrationEnabledResult(code: Int) {}

  protected open fun onZWaveResetAndClearResult(result: Int) {}

  protected open fun onZWaveAddNodeResult(result: Int, node: ZWaveNode?) {}

  protected open fun onZWaveRemoveNodeResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveGetNodeListResult(result: Int, node: ZWaveNode?) {}

  protected open fun onZWaveGetAssignedNodeIdResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveAssignNodeIdResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveWakeUpSettingsReport(result: Int, settings: ZWaveWakeUpSettings?) {}

  protected open fun onZwaveSetWakeUpTimeResult(result: Int) {}

  protected fun getDbHelper(): DbHelper? {
    if (dbHelper == null) {
      dbHelper = DbHelper.getInstance(this)
    }

    return dbHelper
  }

  protected fun invalidateDbHelper() {
    dbHelper = null
  }
}

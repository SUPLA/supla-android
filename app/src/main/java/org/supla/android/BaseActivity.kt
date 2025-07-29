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
import org.supla.android.core.shared.event
import org.supla.android.db.DbHelper
import org.supla.android.extensions.setStatusBarColor
import org.supla.android.lib.AndroidSuplaClientMessageHandler
import org.supla.android.lib.SuplaChannelBasicCfg
import org.supla.android.lib.SuplaEvent
import org.supla.android.lib.ZWaveNode
import org.supla.android.lib.ZWaveWakeUpSettings
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessageHandler

@SuppressLint("registered")
open class BaseActivity : AppCompatActivity(), SuplaClientMessageHandler.Listener {
  private var dbHelper: DbHelper? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStatusBarColor(R.color.primary_container, R.color.surface, false)
  }

  override fun onResume() {
    super.onResume()
    invalidateDbHelper()
  }

  override fun onReceived(message: SuplaClientMessage) {
    when (message) {
      SuplaClientMessage.ClientRegistered -> onRegisteredMsg()
      is SuplaClientMessage.Event -> onEventMsg(message.event)
      is SuplaClientMessage.CallConfigResult -> onCalCfgResult(message.channelId, message.command, message.result, message.data)
      is SuplaClientMessage.ChannelBasicConfig -> onChannelBasicCfg(message.channelBasicConfig)
      is SuplaClientMessage.ChannelFunctionSetResult -> onChannelFunctionSetResult(message.channelId, message.function, message.resultCode)
      is SuplaClientMessage.ZWaveResetAndClearResult -> onZWaveResetAndClearResult(message.resultCode)
      is SuplaClientMessage.ZWaveAddNodeResult -> onZWaveAddNodeResult(message.resultCode, message.node)
      is SuplaClientMessage.ZWaveRemoveNodeResult -> onZWaveRemoveNodeResult(message.resultCode, message.nodeId)
      is SuplaClientMessage.ZWaveGetNodeListResult -> onZWaveGetNodeListResult(message.resultCode, message.node)
      is SuplaClientMessage.ZWaveGetAssignedNodeIdResult -> onZWaveGetAssignedNodeIdResult(message.resultCode, message.nodeId)
      is SuplaClientMessage.ZWaveAssignNodeIdResult -> onZWaveAssignNodeIdResult(message.resultCode, message.nodeId)
      is SuplaClientMessage.ZWaveWakeUpSettingsReport -> onZWaveWakeUpSettingsReport(message.resultCode, message.settings)
      is SuplaClientMessage.ZWaveSetWakeUpTimeResult -> onZwaveSetWakeUpTimeResult(message.result)
      is SuplaClientMessage.CallConfigProgressReport -> onCalCfgProgressReport(message.channelId, message.command, message.progress)
      is SuplaClientMessage.AuthorizationResult,
      is SuplaClientMessage.ChannelCaptionSetResult,
      is SuplaClientMessage.ChannelDataChanged,
      is SuplaClientMessage.ChannelState,
      is SuplaClientMessage.ClientRegistrationError,
      is SuplaClientMessage.GroupDataChanged,
      is SuplaClientMessage.OAuthToken,
      is SuplaClientMessage.RegistrationEnabled,
      is SuplaClientMessage.SetRegistrationEnabledResult -> {}
    }
  }

  protected fun registerMessageHandler() {
    AndroidSuplaClientMessageHandler.getGlobalInstance().register(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    AndroidSuplaClientMessageHandler.getGlobalInstance().unregister(this)
  }

  protected open fun onRegisteredMsg() {}

  protected open fun onEventMsg(event: SuplaEvent) {}

  protected open fun onCalCfgResult(channelId: Int, command: Int, result: Int, data: ByteArray?) {}

  protected open fun onCalCfgProgressReport(channelId: Int, command: Int, progress: Short) {}

  protected open fun onChannelBasicCfg(basicCfg: SuplaChannelBasicCfg?) {}

  protected open fun onChannelFunctionSetResult(channelId: Int, func: Int, code: Int) {}

  protected open fun onChannelCaptionSetResult(channelId: Int, caption: String?, code: Int) {}

  protected open fun onZWaveResetAndClearResult(result: Int) {}

  protected open fun onZWaveAddNodeResult(result: Int, node: ZWaveNode?) {}

  protected open fun onZWaveRemoveNodeResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveGetNodeListResult(result: Int, node: ZWaveNode?) {}

  protected open fun onZWaveGetAssignedNodeIdResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveAssignNodeIdResult(result: Int, nodeId: Short) {}

  protected open fun onZWaveWakeUpSettingsReport(result: Int, settings: ZWaveWakeUpSettings?) {}

  protected open fun onZwaveSetWakeUpTimeResult(result: Int) {}

  protected fun getDbHelper(): DbHelper {
    val helper = dbHelper
    if (helper != null) {
      return helper
    }

    val instance = DbHelper.getInstance(this)
    dbHelper = instance
    return instance
  }

  protected fun invalidateDbHelper() {
    dbHelper = null
  }
}

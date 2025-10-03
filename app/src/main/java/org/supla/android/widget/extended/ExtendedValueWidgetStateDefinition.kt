package org.supla.android.widget.extended
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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.supla.android.R
import org.supla.android.images.ImageId
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Date

object ExtendedValueWidgetStateDefinition : GlanceStateDefinition<ExtendedValueWidgetState> {

  private const val DATA_STORE_FILENAME_PREFIX = "extended_value_widget_state_"

  override suspend fun getDataStore(context: Context, fileKey: String): DataStore<ExtendedValueWidgetState> =
    DataStoreFactory.create(
      serializer = StateSerializer,
      produceFile = { getLocation(context, fileKey) }
    )

  override fun getLocation(context: Context, fileKey: String): File =
    context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey.lowercase())

  object StateSerializer : Serializer<ExtendedValueWidgetState> {
    override val defaultValue: ExtendedValueWidgetState
      get() = ExtendedValueWidgetState(
        icon = ImageId(R.drawable.logo),
        caption = "Supla",
        function = SuplaFunction.NONE,
        value = WidgetValue.Empty,
        updateTime = Date().time
      )

    override suspend fun readFrom(input: InputStream): ExtendedValueWidgetState =
      try {
        Json.decodeFromString(
          ExtendedValueWidgetState.serializer(),
          input.readBytes().decodeToString()
        )
      } catch (exception: SerializationException) {
        Timber.e(exception, "Could decode from string")
        defaultValue
      }

    override suspend fun writeTo(t: ExtendedValueWidgetState, output: OutputStream) =
      try {
        output.use {
          it.write(Json.encodeToString(ExtendedValueWidgetState.serializer(), t).encodeToByteArray())
        }
      } catch (exception: Exception) {
        Timber.e(exception, "Could encode to string")
        throw exception
      }
  }
}

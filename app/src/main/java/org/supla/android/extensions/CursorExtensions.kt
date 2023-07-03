package org.supla.android.extensions

import android.database.Cursor
import org.supla.android.db.Channel

fun Cursor.toListOfChannels(): List<Channel> = this.use {
  val channels = mutableListOf<Channel>()
  if (!it.moveToFirst()) {
    return channels
  }

  do {
    val channel = Channel()
    channel.AssignCursorData(it)
    channels.add(channel)
  } while (it.moveToNext())

  return channels
}
package org.supla.android.db
 
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

import android.content.ContentValues
import android.database.Cursor
import java.util.Date
import java.text.DateFormat

import org.supla.android.lib.SuplaScene
import org.supla.android.lib.SuplaSceneState

data class Scene(var profileId: Long = 0,
                 var sceneId: Int = 0,
                 var locationId: Int = 0,
                 var altIcon: Int = 0,
                 var userIcon: Int = 0,
                 var caption: String = "",
                 var sortOrder: Int = 0,
                 var startedAt: Date? = null,
                 var estimatedEndDate: Date? = null,
                 var initiatorId: Int? = null,
                 var initiatorName: String? = null): DbItem() {

    override fun AssignCursorData(cur: Cursor) {
        setId(cur.getLong(0))
        sceneId = cur.getInt(1)
        locationId = cur.getInt(2)
        altIcon = cur.getInt(3)
        userIcon = cur.getInt(4)
        caption = cur.getString(5)
        if(!cur.isNull(6)) {
            startedAt = dateFromString(cur.getString(6))
        }
        if(!cur.isNull(7)) {
            estimatedEndDate = dateFromString(cur.getString(7))
        }
        if(!cur.isNull(8)) {
            initiatorId = cur.getInt(8)
        }
        if(!cur.isNull(9)) {
            initiatorName = cur.getString(9)
        }
        sortOrder = cur.getInt(10)
        profileId = cur.getLong(11)
    }

    override fun getContentValues(): ContentValues {
        val vals = ContentValues()
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_SCENEID, sceneId)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID, locationId)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_ALTICON, altIcon)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_USERICON, userIcon)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_CAPTION, caption)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER, sortOrder)
        if(startedAt != null) {
            vals.put(SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT, dateToString(startedAt!!))
        } else {
            vals.putNull(SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT)
        }
        if(estimatedEndDate != null) {
            vals.put(SuplaContract.SceneEntry.COLUMN_NAME_EST_END_DATE, dateToString(estimatedEndDate!!))
        } else {
            vals.putNull(SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT)
        }
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_ID, initiatorId)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_NAME, initiatorName)
        vals.put(SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID, profileId)

        return vals
    }

    fun assign(scene: SuplaScene) {
        sceneId = scene.id
        locationId = scene.locationId
        altIcon = scene.altIcon
        userIcon = scene.userIcon
        caption = scene.caption
    }

    fun assign(state: SuplaSceneState) {
        assert(sceneId == state.sceneId)
        startedAt = state.startedAt
        estimatedEndDate = state.estimatedEndDate
        initiatorId = state.initiatorId
        initiatorName = state.initiatorName
    }

    fun clone(): Scene {
        val rv = copy()
        rv.setId(getId())
        return rv
    }

    fun isExecuting(): Boolean {
        val sst = startedAt
        val now = Date()
//        val timeSinceStart = computeTimeSinceStart(now)
        if(sst != null && sst < now) {
            val eet = estimatedEndDate
            if(eet == null || eet > now) {
                return true
            }
        }
        return false
    }

    private fun dateFromString(str: String): Date {
        val fmt = DateFormat.getDateTimeInstance()
        return fmt.parse(str)!!
    }

    private fun dateToString(date: Date): String {
        val fmt = DateFormat.getDateTimeInstance()
        return fmt.format(date)
    }

    private fun formatMillis(v: Long): String {
        var r = v
        var k: Long = 0
        var rv = ""
        k = r / 3600000
        rv += String.format("%02d:", k)
        r -= k * 3600000
        k = r / 60000
        rv += String.format("%02d:", k)
        r -= k * 60000
        rv += String.format("%02d", r / 1000)

        return rv
    }

    private fun computeTimeSinceStart(now: Date): String? {
        val sst = startedAt
        val eet = estimatedEndDate
        if(sst != null && sst < now && (eet == null || eet.time > now.time)) {
            val diff = now.time - sst.time
            val rv = formatMillis(diff)
            return rv
        } else {
            return null
        }
    }
}

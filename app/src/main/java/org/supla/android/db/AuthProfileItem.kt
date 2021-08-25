package org.supla.android.db

import org.supla.android.db.DbItem
import android.database.Cursor
import android.content.ContentValues

data class AuthProfileItem(val name: String,
                           val emailAddr: String? = null,
                           val accessID: Int? = null,
                           val accessIDpwd: String? = null) : DbItem() {

    override fun AssignCursorData(cur: Cursor) {

    }

    override fun getContentValues(): ContentValues {
        val vals = ContentValues()

        return vals
    }
}

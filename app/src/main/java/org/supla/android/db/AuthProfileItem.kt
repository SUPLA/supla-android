package org.supla.android.db

import org.supla.android.db.DbItem
import android.database.Cursor
import android.content.ContentValues

data class AuthProfileItem(var name: String,
                           var emailAddr: String? = null,
                           var serverAddr: String? = null,
                           var accessID: Int? = null,
                           var accessIDpwd: String? = null) : DbItem() {

    override fun AssignCursorData(cur: Cursor) {
        setId(cur.getLong(0))
        fun stringOrNull(cur: Cursor, idx: Int): String? {
            return if(cur.isNull(idx)) null else cur.getString(idx)
        }
                             
        name = cur.getString(1)
        emailAddr = stringOrNull(cur, 2)
        serverAddr = stringOrNull(cur, 3)
        accessID = if(cur.isNull(4)) null else cur.getInt(4)
        accessIDpwd = stringOrNull(cur, 5)
    }

    override fun getContentValues(): ContentValues {
        val vals = ContentValues()
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_PROFILE_NAME, name)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_EMAIL_ADDR, emailAddr)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_ADDR, serverAddr)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID,
                 accessID)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID_PWD,
                 accessIDpwd)
        return vals
    }
}

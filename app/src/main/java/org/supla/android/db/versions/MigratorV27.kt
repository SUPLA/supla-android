package org.supla.android.db.versions

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.supla.android.Preferences
import org.supla.android.data.source.local.BaseDao.DatabaseAccessProvider
import org.supla.android.data.source.local.SceneDao
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.SuplaContract
import org.supla.android.db.SuplaContract.AuthProfileEntry
import org.supla.android.profile.AuthInfo

class MigratorV27(private val db: SQLiteDatabase, private val context: Context) {

  private val sceneDao: SceneDao by lazy {
    SceneDao(object : DatabaseAccessProvider {
      override fun getReadableDatabase() = db

      override fun getWritableDatabase() = db

      override fun getCachedProfileId() = 0L
    })
  }

  fun migrateUserProfiles() {
    var validAccountAvailable = false

    getAllProfiles().use { cursor ->
      if (cursor.moveToFirst()) {
        val prefs = Preferences(context)
        do {
          val profile: AuthProfileItem = makeEmptyAuthItem()
          profile.AssignCursorData(cursor)
          if (profile.authInfo.isAuthDataComplete) {
            prefs.isAnyAccountRegistered = true
            validAccountAvailable = true
          }
        } while (cursor.moveToNext())
      }
    }

    if (!validAccountAvailable) {
      // There is only empty account in the database which is not needed anymore.
      db.delete(AuthProfileEntry.TABLE_NAME, null, null)
    }
  }

  fun migrateScenesDates(tableCreator: Runnable) {
    val allScenes = sceneDao.getAllScenes()

    db.execSQL("DROP TABLE ${SuplaContract.SceneEntry.TABLE_NAME}")
    db.execSQL("DROP VIEW ${SuplaContract.SceneViewEntry.VIEW_NAME}")
    tableCreator.run()

    for (legacyScene in allScenes) {
      sceneDao.insertScene(legacyScene.getScene())
    }
  }

  private fun getAllProfiles(): Cursor = db.query(
    AuthProfileEntry.TABLE_NAME,
    AuthProfileEntry.ALL_COLUMNS,
    null,
    null,
    null,
    null,
    null
  )

  private fun makeEmptyAuthItem(): AuthProfileItem {
    return AuthProfileItem(
      name = "",
      authInfo = AuthInfo(
        emailAuth = true,
        serverAutoDetect = true,
        serverForEmail = "",
        serverForAccessID = "",
        emailAddress = "",
        accessID = 0,
        accessIDpwd = "",
        preferredProtocolVersion = 0,
        guid = byteArrayOf(0),
        authKey = byteArrayOf(0)
      ),
      advancedAuthSetup = false,
      isActive = false
    )
  }
}

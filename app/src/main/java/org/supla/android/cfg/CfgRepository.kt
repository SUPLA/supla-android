package org.supla.android.cfg

import android.content.Context
import org.supla.android.Preferences
import org.supla.android.db.DbHelper

interface CfgRepository {
    fun getCfg(): CfgData
    fun storeCfg(cfg: CfgData)
}

class PrefsCfgRepositoryImpl(ctx: Context, private val helper: DbHelper): CfgRepository {
    private val prefs: Preferences

    init {
        prefs = Preferences(ctx)
    }

    override fun getCfg(): CfgData {
        return CfgData(prefs.serverAddress, prefs.accessID, prefs.accessIDpwd,
                       prefs.email)
    }


    override fun storeCfg(cfg: CfgData) {
        helper.deleteUserIcons() // TODO: I'm not sure if this is the right place for this yet.
        prefs.serverAddress = cfg.serverAddr
        prefs.accessID = cfg.accessID
        prefs.accessIDpwd = cfg.accessIDpwd
        prefs.email = cfg.email
        prefs.setPreferedProtocolVersion()
    }

}

package org.supla.android.cfg

import android.content.Context
import org.supla.android.Preferences

interface CfgRepository {
    fun getCfg(): CfgData
    fun storeCfg(cfg: CfgData)
}

class PrefsCfgRepositoryImpl(ctx: Context): CfgRepository {
    private val prefs: Preferences

    init {
        prefs = Preferences(ctx)
    }

    override fun getCfg(): CfgData {
        return CfgData(prefs.getServerAddress(),
                       prefs.getAccessID(),
                       prefs.getAccessIDpwd(),
                       prefs.getEmail())
    }


    override fun storeCfg(cfg: CfgData) {
        /* TODO: implement */
    }

}

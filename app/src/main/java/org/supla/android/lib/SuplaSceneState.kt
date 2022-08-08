package org.supla.android.lib

import java.util.*

class SuplaSceneState(
    val sceneId: Int, millisecondsFromStart: Long, millisecondsLeft: Long,
    initiatorId: Int, initiatorName: String, eol: Boolean
) {
    var startedAt: Date? = null
        private set
    var estimatedEndDate: Date? = null
        private set
    val initiatorId: Int
    val initiatorName: String
    val isEol: Boolean
    val isDuringExecution: Boolean
        get() {
            if (startedAt != null && estimatedEndDate != null) {
                val now = Date()
                return (startedAt!!.compareTo(now) <= 0
                        && estimatedEndDate!!.compareTo(now) > 0)
            }
            return false
        }
    val millisecondsLeft: Long
        get() {
            if (estimatedEndDate != null) {
                val now = Date()
                val diff = estimatedEndDate!!.getTime() - now.time
                if (diff > 0) {
                    return diff
                }
            }
            return 0
        }

    init {
        if (millisecondsFromStart > 0 || millisecondsLeft > 0) {
            startedAt = Date(System.currentTimeMillis() - millisecondsFromStart)
            estimatedEndDate = Date(System.currentTimeMillis() + millisecondsLeft)
        } else {
            startedAt = null
            estimatedEndDate = null
        }

        this.initiatorId = initiatorId
        this.initiatorName = initiatorName
        isEol = eol
    }
}

package org.supla.android.lib;

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

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("unused")
public class SuplaTimerState implements Serializable {

    private Date countdownEndsAt;
    private byte[] targetValue;
    private int senderId;
    private String senderName;

    public SuplaTimerState(long countdownEndsAt, byte[] targetValue, int senderId, String senderName) {

        if (countdownEndsAt > 0) {
            this.countdownEndsAt = new Date(countdownEndsAt);
        }

        this.targetValue = targetValue;
        this.senderId = senderId;
        this.senderName = senderName;
    }

    public Date getCountdownEndsAt() {
        return countdownEndsAt;
    }

    public byte[] getTargetValue() {
        return targetValue;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }
}

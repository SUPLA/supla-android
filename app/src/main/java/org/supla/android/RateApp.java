package org.supla.android;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.Window;
import java.util.Calendar;
import java.util.Date;
import org.supla.android.db.DbHelper;

public class RateApp {

  private final String PN_RATE_TIME = "rate_time";
  private final Context context;

  RateApp(Context context) {
    this.context = context;
  }

  private void moreTime(int days) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DAY_OF_YEAR, days);

    SharedPreferences.Editor editor = prefs.edit();
    editor.putLong(PN_RATE_TIME, cal.getTime().getTime());
    editor.apply();
  }

  private void ShowAlertDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(R.string.rate_msg);
    builder.setPositiveButton(
        R.string.rate_now,
        (dialog, id) -> {
          context.startActivity(
              new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.supla.android")));
          moreTime(3650);
          dialog.cancel();
        });

    builder.setNeutralButton(
        R.string.rate_later,
        (dialog, id) -> {
          moreTime(6);
          dialog.cancel();
        });

    builder.setNegativeButton(
        R.string.rate_no_thanks,
        (dialog, id) -> {
          moreTime(3650);
          dialog.cancel();
        });

    AlertDialog alert = builder.create();
    alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
    alert.show();
  }

  void showDialog(ShowCallback callback) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    long rt = prefs.getLong(PN_RATE_TIME, 0L);

    Date now = new Date();

    if (rt == 0) {

      moreTime(1);

    } else if (now.getTime() >= rt) {

      DbHelper DbH = DbHelper.getInstance(context);
      if (DbH.getChannelCount() > 0) {

        moreTime(1);

        callback.show(this::ShowAlertDialog);
      }
    }
  }

  public interface ShowCallback {
    void show(Runnable runnable);
  }
}

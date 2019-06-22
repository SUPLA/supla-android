package org.supla.android.restapi;

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supla.android.Trace;

import java.util.ArrayList;

public class DownloadUserIcons extends SuplaRestApiClientTask {

    final private int PACKAGE_SIZE = 4;
    final private int DELAY = 5000;
    private int DownloadCount = 0;

    public DownloadUserIcons(Context context) {
        super(context);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SQLiteDatabase db = getDbH().getReadableDatabase();
        ArrayList<Integer>ids = getDbH().iconsToDownload(db);
        db.close();

        String package_ids = "";
        for(int a=0;a<ids.size();a++) {

            if (!package_ids.isEmpty()) {
                package_ids+=",";
            }

            package_ids+=ids.get(a).toString();

            if (a%PACKAGE_SIZE == PACKAGE_SIZE-1 || a == ids.size()-1) {
                ApiRequestResult result = apiRequest(
                        "user-icons?include=images&ids="+package_ids);

                if (result!=null
                        && result.getCode() == 200
                        && result.getJObj() instanceof JSONArray) {

                    JSONArray arr = (JSONArray)result.getJObj();
                    for(int b=0;b<arr.length();b++) {
                        try {
                            JSONObject obj = arr.getJSONObject(b);
                            int imgId = obj.getInt("id");
                            if (imgId > 0) {
                                JSONArray images = obj.getJSONArray("images");

                                byte[] img1 = images.length() > 0 ?
                                        Base64.decode(images.getString(0), Base64.DEFAULT)
                                        : null;


                                byte[] img2 = images.length() > 1 ?
                                        Base64.decode(images.getString(1), Base64.DEFAULT)
                                        : null;

                                byte[] img3 = images.length() > 2 ?
                                        Base64.decode(images.getString(2), Base64.DEFAULT)
                                        : null;

                                byte[] img4 = images.length() > 3 ?
                                        Base64.decode(images.getString(3), Base64.DEFAULT)
                                        : null;

                                db = getDbH().getWritableDatabase();
                                boolean added = getDbH().addUserIcons(db,
                                        imgId, img1, img2, img3, img4);
                                db.close();
                                if (added) {
                                    DownloadCount++;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                package_ids = "";
            }
        }

        return null;
    }

    public int downloadCount() {
        return DownloadCount;
    }
}

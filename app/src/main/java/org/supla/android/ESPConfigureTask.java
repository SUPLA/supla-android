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

import static org.supla.android.core.networking.esp.EspConfigResultKt.RESULT_COMPAT_ERROR;
import static org.supla.android.core.networking.esp.EspConfigResultKt.RESULT_CONN_ERROR;
import static org.supla.android.core.networking.esp.EspConfigResultKt.RESULT_FAILED;
import static org.supla.android.core.networking.esp.EspConfigResultKt.RESULT_PARAM_ERROR;
import static org.supla.android.core.networking.esp.EspConfigResultKt.RESULT_SUCCESS;

import android.os.AsyncTask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.supla.android.core.networking.esp.EspConfigResult;
import org.supla.android.core.networking.esp.EspHtmlParser;

public class ESPConfigureTask extends AsyncTask<String, Integer, EspConfigResult> {

  private AsyncResponse delegate;

  private EspHtmlParser parser;

  public ESPConfigureTask(EspHtmlParser parser) {
    Validate.notNull(parser);
    this.parser = parser;
  }

  public void setDelegate(AsyncResponse delegate) {
    this.delegate = delegate;
  }

  @Override
  protected EspConfigResult doInBackground(String... params) {

    EspConfigResult result = new EspConfigResult();

    if (params.length != 4 || params[0].isEmpty() || params[2].isEmpty() || params[3].isEmpty()) {

      result.setResultCode(RESULT_PARAM_ERROR);
      return result;
    }

    int retryCount = 10;
    Map<String, String> fieldMap = new HashMap<>();

    while (retryCount > 0)
      try {

        Thread.sleep(1500);
        Document doc = Jsoup.connect("http://192.168.4.1").get();

        fieldMap.putAll(parser.findInputs(doc));
        result.merge(parser.prepareResult(doc, fieldMap));

        retryCount = -1;

      } catch (IOException e) {
        e.printStackTrace();
        retryCount--;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    if (retryCount > -1) {

      result.setResultCode(RESULT_CONN_ERROR);
      return result;
    }

    if (fieldMap.get("sid") == null
        || fieldMap.get("wpw") == null
        || fieldMap.get("svr") == null
        || fieldMap.get("eml") == null
        || result.getDeviceFirmwareVersion() == null
        || result.getDeviceFirmwareVersion().isEmpty()) {

      result.setResultCode(RESULT_COMPAT_ERROR);
      return result;
    }

    fieldMap.put("sid", params[0]);
    fieldMap.put("wpw", params[1]);
    fieldMap.put("svr", params[2]);
    fieldMap.put("eml", params[3]);

    if (fieldMap.get("upd") != null) {
      fieldMap.put("upd", "1");
    }

    if (fieldMap.get("pro") != null) {
      // Set protocol to "Supla"
      fieldMap.put("pro", "0");
    }

    retryCount = 3;

    while (retryCount > 0)
      try {
        Thread.sleep(2000);

        Document doc =
            Jsoup.connect("http://192.168.4.1")
                .data(fieldMap)
                .referrer("http://192.168.4.1")
                .method(Connection.Method.POST)
                .execute()
                .parse();

        Element msg = doc.getElementById("msg");
        if (msg != null && msg.html().toLowerCase().contains("data saved")) {

          Trace.d("TRY RBT", "RBT");

          Thread.sleep(1000);

          fieldMap.put("rbt", "1"); // reboot

          try {
            Jsoup.connect("http://192.168.4.1")
                .data(fieldMap)
                .referrer("http://192.168.4.1")
                .method(Connection.Method.POST)
                .execute();

          } catch (IOException e) {
            Trace.d(ESPConfigureTask.class.getSimpleName(), "Could not connect to esp (no. 1)", e);
          }

          result.setResultCode(RESULT_SUCCESS);
          return result;
        }

      } catch (IOException e) {
        Trace.d(ESPConfigureTask.class.getSimpleName(), "Could not connect to esp (no. 2)", e);
        retryCount--;
      } catch (InterruptedException e) {
        Trace.d(ESPConfigureTask.class.getSimpleName(), "Esp connection broken", e);
      }

    result.setResultCode(RESULT_FAILED);
    return result;
  }

  @Override
  protected void onPostExecute(EspConfigResult result) {
    delegate.espConfigFinished(result);
  }

  public interface AsyncResponse {
    void espConfigFinished(EspConfigResult result);
  }
}
